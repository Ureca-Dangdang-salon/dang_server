package com.dangdangsalon.domain.chat.service;

import com.dangdangsalon.domain.chat.dto.ChatCustomerDto;
import com.dangdangsalon.domain.chat.dto.ChatEstimateDogProfileDto;
import com.dangdangsalon.domain.chat.dto.ChatEstimateInfo;
import com.dangdangsalon.domain.chat.dto.ChatGroomerProfileDto;
import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.dto.ChatRoomDetailDto;
import com.dangdangsalon.domain.chat.dto.ChatRoomListDto;
import com.dangdangsalon.domain.chat.dto.CreateChatRoomRequestDto;
import com.dangdangsalon.domain.chat.dto.CreateChatRoomResponseDto;
import com.dangdangsalon.domain.chat.entity.ChatRoom;
import com.dangdangsalon.domain.chat.entity.SenderRole;
import com.dangdangsalon.domain.chat.repository.ChatRoomRepository;
import com.dangdangsalon.domain.chat.util.ChatRedisUtil;
import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.dto.ServicePriceResponseDto;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestServiceRepository;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerProfileRepository;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.payment.dto.PaymentDogProfileResponseDto;
import com.dangdangsalon.domain.user.entity.Role;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import com.dangdangsalon.util.RedisUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final EstimateRepository estimateRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final GroomerProfileRepository groomerProfileRepository;
    private final EstimateRequestServiceRepository estimateRequestServiceRepository;

    private final ChatMessageService chatMessageService;
    private final ChatRedisUtil chatRedisUtil;

    @Transactional
    public CreateChatRoomResponseDto createChatRoom(CreateChatRoomRequestDto createChatRoomRequestDto) {
        chatRoomRepository.findByEstimateId(createChatRoomRequestDto.getEstimateId())
                .ifPresent(chatRoom -> {
                    throw new IllegalStateException("이미 해당 견적서에 대한 채팅방이 존재합니다. Id: "
                            + createChatRoomRequestDto.getEstimateId());
                });

        Estimate estimate = estimateRepository.findWithGroomerProfileAndCustomerById(
                        createChatRoomRequestDto.getEstimateId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당하는 견적서가 존재하지 않습니다. Id: " + createChatRoomRequestDto.getEstimateId()));

        User groomer = estimate.getGroomerProfile().getUser();
        User customer = estimate.getEstimateRequest().getUser();

        ChatRoom chatRoom = ChatRoom.builder()
                .estimate(estimate)
                .groomer(groomer)
                .customer(customer)
                .customerLeft(false)
                .groomerLeft(false)
                .build();

        ChatRoom createdChatRoom = chatRoomRepository.save(chatRoom);

        saveFirstChatToRedis(createdChatRoom, groomer, estimate);

        return CreateChatRoomResponseDto.builder()
                .roomId(createdChatRoom.getId())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ChatRoomListDto> getChatRoomList(Long userId, String role) {
        Role userRole = Role.from(role);

        List<ChatRoom> chatRooms = new ArrayList<>();

        if (userRole.equals(Role.ROLE_USER)) {
            chatRooms = chatRoomRepository.findByCustomerId(userId);
        } else if (userRole.equals(Role.ROLE_SALON)) {
            chatRooms = chatRoomRepository.findByGroomerId(userId);
        }

        return chatRooms.stream()
                .map(chatRoom -> convertToChatRoomListDto(chatRoom, userRole))
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatRoomDetailDto getChatRoomDetail(Long roomId, String role) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다. Id: " + roomId));

        GroomerProfile groomerProfile = groomerProfileRepository.findByUserId(chatRoom.getGroomer().getId())
                .orElseThrow(
                        () -> new IllegalArgumentException("미용사 프로필이 존재하지 않습니다. Id: " + chatRoom.getGroomer().getId()));

        Role userRole = Role.from(role);
        Long userId = 0L;

        if (userRole.equals(Role.ROLE_USER)) {
            userId = chatRoom.getCustomer().getId();
        } else if (userRole.equals(Role.ROLE_SALON)) {
            userId = chatRoom.getGroomer().getId();
        }

        List<ChatMessageDto> messages = chatMessageService.getUnreadOrRecentMessages(roomId, userId);

        return ChatRoomDetailDto.create(chatRoom, groomerProfile, messages);
    }

    @Transactional
    public void exitChatRoom(Long roomId, String role) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다. Id: " + roomId));

        Role userRole = Role.from(role);

        chatRoom.updateExitState(userRole, true);

        if (chatRoom.isAllLeft()) {
            chatRoomRepository.delete(chatRoom);
            chatMessageService.deleteChatData(roomId);
        }
    }

    @Transactional
    public ChatMessageDto createUpdateEstimateMessage(Estimate estimate) {
        ChatRoom chatRoom = chatRoomRepository.findByEstimateId(estimate.getId())
                .orElseThrow(
                        () -> new IllegalArgumentException("해당 견적서에 해당되는 채팅방이 없습니다. EstimateId= " + estimate.getId()));

        Long currentSequence = chatRedisUtil.getCurrentSequence(chatRoom.getId());

        return ChatMessageDto.builder()
                .sequence(++currentSequence)
                .messageId(UUID.randomUUID().toString())
                .roomId(chatRoom.getId())
                .senderId(chatRoom.getGroomer().getId())
                .senderRole(Role.ROLE_SALON.name())
                .sendAt(LocalDateTime.now())
                .estimateInfo(createEstimateMessage(estimate))
                .build();
    }

    private ChatRoomListDto convertToChatRoomListDto(ChatRoom chatRoom, Role userRole) {
        String lastMessage = chatMessageService.getLastMessage(chatRoom.getId());
        int unreadCount = 0;

        if (userRole.equals(Role.ROLE_USER)) {
            unreadCount = chatMessageService.getUnreadCount(chatRoom.getId(), chatRoom.getCustomer().getId());
        } else if (userRole.equals(Role.ROLE_SALON)) {
            unreadCount = chatMessageService.getUnreadCount(chatRoom.getId(), chatRoom.getGroomer().getId());
        }

        GroomerProfile groomerProfile = groomerProfileRepository.findByUserId(chatRoom.getGroomer().getId())
                .orElseThrow(
                        () -> new IllegalArgumentException("미용사 프로필이 존재하지 않습니다. Id: " + chatRoom.getGroomer().getId()));

        return ChatRoomListDto.builder()
                .roomId(chatRoom.getId())
                .estimateId(chatRoom.getEstimate().getId())
                .estimateStatus(chatRoom.getEstimate().getStatus())
                .groomerProfile(ChatGroomerProfileDto.create(groomerProfile))
                .customer(ChatCustomerDto.create(chatRoom))
                .lastMessage(lastMessage)
                .unreadCount(unreadCount)
                .totalAmount(chatRoom.getEstimate().getTotalAmount())
                .build();
    }

    private void saveFirstChatToRedis(ChatRoom createdChatRoom, User groomer, Estimate estimate) {
        Long currentSequence = chatRedisUtil.getCurrentSequence(createdChatRoom.getId());

        ChatMessageDto estimateMessage = ChatMessageDto.builder()
                .sequence(++currentSequence)
                .messageId(UUID.randomUUID().toString())
                .roomId(createdChatRoom.getId())
                .senderId(groomer.getId())
                .senderRole(Role.ROLE_SALON.name())
                .sendAt(LocalDateTime.now())
                .estimateInfo(createEstimateMessage(estimate))
                .build();

        GroomerProfile groomerProfile = groomerProfileRepository.findByUserId(groomer.getId())
                .orElseThrow(() -> new IllegalArgumentException("미용사 프로필이 존재하지 않습니다. Id: " + groomer.getId()));

        if (estimate.getImageKey() != null) {
            ChatMessageDto wantSendImageMessage = ChatMessageDto.createImageMessage(++currentSequence,
                    createdChatRoom.getId(),
                    groomer.getId(), Role.ROLE_SALON.name(), estimate.getImageKey());

            chatMessageService.saveMessageRedis(wantSendImageMessage);
        }

        if (estimate.getDescription() != null) {
            ChatMessageDto wantSendDescriptionMessage = ChatMessageDto.createTextMessage(++currentSequence,
                    createdChatRoom.getId(),
                    groomer.getId(), Role.ROLE_SALON.name(), estimate.getDescription());

            chatMessageService.saveMessageRedis(wantSendDescriptionMessage);
        }

        if (groomerProfile.getDetails().getStartChat() != null) {
            ChatMessageDto firstMessage = ChatMessageDto.createTextMessage(++currentSequence, createdChatRoom.getId(),
                    groomer.getId(), Role.ROLE_SALON.name(), groomerProfile.getDetails().getStartChat());

            chatMessageService.saveMessageRedis(firstMessage);
        }

        chatMessageService.saveMessageRedis(estimateMessage);

        chatRedisUtil.updateCurrentSequence(createdChatRoom.getId(), currentSequence);
    }

    private ChatEstimateInfo createEstimateMessage(Estimate estimate) {
        return ChatEstimateInfo.builder()
                .dogProfileList(getDogProfileServices(estimate))
                .totalAmount(estimate.getTotalAmount())
                .build();
    }

    private List<ChatEstimateDogProfileDto> getDogProfileServices(Estimate estimate) {
        List<EstimateRequestProfiles> requestDogProfiles = estimate
                .getEstimateRequest()
                .getEstimateRequestProfiles();

        return requestDogProfiles.stream()
                .map(this::requestProfileToDto)
                .toList();
    }

    private ChatEstimateDogProfileDto requestProfileToDto(EstimateRequestProfiles profile) {
        List<ServicePriceResponseDto> services = getServicePrices(profile.getId());

        return ChatEstimateDogProfileDto.builder()
                .dogName(profile.getDogProfile().getName())
                .servicePriceList(services)
                .aggressionCharge(profile.getAggressionCharge())
                .healthIssueCharge(profile.getHealthIssueCharge())
                .build();
    }

    private List<ServicePriceResponseDto> getServicePrices(Long profileId) {
        return estimateRequestServiceRepository.findByEstimateRequestProfilesId(profileId)
                .stream()
                .map(ServicePriceResponseDto::create)
                .toList();
    }
}
