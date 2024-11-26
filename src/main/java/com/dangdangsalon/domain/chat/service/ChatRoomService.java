package com.dangdangsalon.domain.chat.service;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.dto.CreateChatRoomRequestDto;
import com.dangdangsalon.domain.chat.dto.CreateChatRoomResponseDto;
import com.dangdangsalon.domain.chat.entity.ChatRoom;
import com.dangdangsalon.domain.chat.entity.SenderRole;
import com.dangdangsalon.domain.chat.repository.ChatRoomRepository;
import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerProfileRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final EstimateRepository estimateRepository;
    private final ChatRoomRepository chatRoomRepository;

    private final ChatMessageService chatMessageService;

    public CreateChatRoomResponseDto createChatRoom(CreateChatRoomRequestDto createChatRoomRequestDto) {
        Estimate estimate = estimateRepository.findWithGroomerProfileAndCustomerById(createChatRoomRequestDto.getEstimateId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당하는 견적서가 존재하지 않습니다. Id: " + createChatRoomRequestDto.getEstimateId()));

        GroomerProfile groomerProfile = estimate.getGroomerProfile();
        User customer = estimate.getEstimateRequest().getUser();

        ChatRoom chatRoom = ChatRoom.builder()
                .estimate(estimate)
                .groomerProfile(groomerProfile)
                .user(customer)
                .build();

        ChatRoom createdChatRoom = chatRoomRepository.save(chatRoom);

        saveFirstChatToRedis(createdChatRoom, groomerProfile.getId(), groomerProfile);

        return CreateChatRoomResponseDto.builder()
                .roomId(createdChatRoom.getId())
                .createdAt(LocalDateTime.now())
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
