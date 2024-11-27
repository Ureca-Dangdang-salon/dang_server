package com.dangdangsalon.domain.chat.service;

import com.dangdangsalon.domain.chat.dto.ChatCustomerDto;
import com.dangdangsalon.domain.chat.dto.ChatGroomerProfileDto;
import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.dto.ChatRoomListDto;
import com.dangdangsalon.domain.chat.dto.CreateChatRoomRequestDto;
import com.dangdangsalon.domain.chat.dto.CreateChatRoomResponseDto;
import com.dangdangsalon.domain.chat.entity.ChatRoom;
import com.dangdangsalon.domain.chat.entity.SenderRole;
import com.dangdangsalon.domain.chat.repository.ChatRoomRepository;
import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerProfileRepository;
import com.dangdangsalon.domain.user.entity.Role;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final EstimateRepository estimateRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final GroomerProfileRepository groomerProfileRepository;

    private final ChatMessageService chatMessageService;

    @Transactional
    public CreateChatRoomResponseDto createChatRoom(CreateChatRoomRequestDto createChatRoomRequestDto) {
        chatRoomRepository.findByEstimateId(createChatRoomRequestDto.getEstimateId())
                .ifPresent(chatRoom -> {
                    throw new IllegalStateException("이미 해당 견적서에 대한 채팅방이 존재합니다. Id: "
                            + createChatRoomRequestDto.getEstimateId());
                });


        Estimate estimate = estimateRepository.findWithGroomerProfileAndCustomerById(createChatRoomRequestDto.getEstimateId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당하는 견적서가 존재하지 않습니다. Id: " + createChatRoomRequestDto.getEstimateId()));

        GroomerProfile groomerProfile = estimate.getGroomerProfile();
        User customer = estimate.getEstimateRequest().getUser();

        ChatRoom chatRoom = ChatRoom.builder()
                .estimate(estimate)
                .groomerProfile(groomerProfile)
                .user(customer)
                .customerLeft(false)
                .groomerLeft(false)
                .build();

        ChatRoom createdChatRoom = chatRoomRepository.save(chatRoom);

        saveFirstChatToRedis(createdChatRoom, groomerProfile.getId(), groomerProfile);

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
            chatRooms = chatRoomRepository.findByGroomerProfileIdOrCustomerId(null, userId);
        } else if (userRole.equals(Role.ROLE_SALON)) {
            GroomerProfile groomerProfile = groomerProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 미용사 프로필이 존재하지 않습니다. Id: " + userId));
            chatRooms = chatRoomRepository.findByGroomerProfileIdOrCustomerId(groomerProfile.getId(), null);
        }

        return chatRooms.stream()
                .map(this::convertToChatRoomListDto)
                .toList();
    }

    private ChatRoomListDto convertToChatRoomListDto(ChatRoom chatRoom) {
        String lastMessage = chatMessageService.getLastMessage(chatRoom.getId());
        int unreadCount = chatMessageService.getUnreadCount(chatRoom.getId());

        return ChatRoomListDto.builder()
                .roomId(chatRoom.getId())
                .groomerProfile(ChatGroomerProfileDto.create(chatRoom))
                .customer(ChatCustomerDto.create(chatRoom))
                .lastMessage(lastMessage)
                .unreadCount(unreadCount)
                .totalAmount(chatRoom.getEstimate().getTotalAmount())
                .build();
    }

    private void saveFirstChatToRedis(ChatRoom createdChatRoom, Long groomerProfileId, GroomerProfile groomerProfile) {
        ChatMessageDto firstMessage = ChatMessageDto.builder()
                .roomId(createdChatRoom.getId())
                .senderId(groomerProfileId)
                .senderRole(SenderRole.GROOMER.name())
                .messageText(groomerProfile.getDetails().getStartChat())
                .sendAt(LocalDateTime.now())
                .build();

        chatMessageService.saveMessageRedis(firstMessage);
    }
}
