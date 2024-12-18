package com.dangdangsalon.domain.chat.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import com.dangdangsalon.domain.chat.dto.ChatRoomDetailDto;
import com.dangdangsalon.domain.chat.dto.ChatRoomListDto;
import com.dangdangsalon.domain.chat.dto.CreateChatRoomRequestDto;
import com.dangdangsalon.domain.chat.dto.CreateChatRoomResponseDto;
import com.dangdangsalon.domain.chat.entity.ChatRoom;
import com.dangdangsalon.domain.chat.repository.ChatRoomRepository;
import com.dangdangsalon.domain.chat.util.ChatRedisUtil;
import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestServiceRepository;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerDetails;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerProfileRepository;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import com.dangdangsalon.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private EstimateRepository estimateRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private GroomerProfileRepository groomerProfileRepository;

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private ChatRedisUtil chatRedisUtil;

    @Mock
    private EstimateRequestServiceRepository estimateRequestServiceRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Test
    @DisplayName("채팅방 생성 테스트")
    void testCreateChatRoom_WithMocks() {
        Long estimateId = 1L;
        CreateChatRoomRequestDto requestDto = new CreateChatRoomRequestDto(estimateId);

        User groomer = User.builder().id(1L).build();
        User customer = User.builder().id(2L).build();

        GroomerProfile groomerProfile = GroomerProfile.builder().user(groomer)
                .details(GroomerDetails.builder().startChat("시작채팅").build()).build();

        EstimateRequestProfiles estimateRequestProfiles = mock(EstimateRequestProfiles.class);
        EstimateRequest estimateRequest = EstimateRequest.builder()
                .user(customer)
                .estimateRequestProfiles(List.of(estimateRequestProfiles))
                .build();

        Estimate estimate = Estimate.builder()
                .groomerProfile(groomerProfile)
                .estimateRequest(estimateRequest)
                .build();

        ChatRoom savedChatRoom = ChatRoom.builder()
                .estimate(estimate)
                .groomer(groomer)
                .customer(customer)
                .customerLeft(false)
                .groomerLeft(false)
                .build();

        EstimateRequestService estimateRequestService = mock(EstimateRequestService.class);
        DogProfile dogProfile = mock(DogProfile.class);

        given(estimateRepository.findWithGroomerProfileAndCustomerById(estimateId)).willReturn(Optional.of(estimate));
        given(chatRoomRepository.findByEstimateId(estimateId)).willReturn(Optional.empty());
        given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(savedChatRoom);
        given(estimateRequestServiceRepository.findByEstimateRequestProfilesId(anyLong()))
                .willReturn(List.of(estimateRequestService));
        given(estimateRequestService.getGroomerService()).willReturn(GroomerService.builder().build());
        given(estimateRequestProfiles.getDogProfile()).willReturn(dogProfile);
        given(groomerProfileRepository.findByUserId(1L)).willReturn(Optional.of(groomerProfile));

        CreateChatRoomResponseDto response = chatRoomService.createChatRoom(requestDto);

        assertThat(response).isNotNull();
        assertThat(response.getCreatedAt()).isNotNull();
        then(chatRoomRepository).should(times(1)).findByEstimateId(estimateId);
        then(chatRoomRepository).should(times(1)).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("채팅방 목록 조회 테스트")
    void testGetChatRoomList() {
        Long userId = 1L;
        String role = "ROLE_USER";

        ChatRoom chatRoom = ChatRoom.builder()
                .customer(User.builder().id(userId).build())
                .groomer(User.builder().id(2L).build())
                .estimate(Estimate.builder().totalAmount(80000).build())
                .build();
        GroomerProfile groomerProfile = GroomerProfile.builder().details(GroomerDetails.builder().address("주소").build())
                .build();

        given(chatRoomRepository.findByCustomerId(userId)).willReturn(List.of(chatRoom));
        given(chatMessageService.getLastMessage(chatRoom.getId())).willReturn("Last message");
        given(chatMessageService.getUnreadCount(chatRoom.getId(), userId)).willReturn(5);
        given(groomerProfileRepository.findByUserId(2L)).willReturn(Optional.of(groomerProfile));

        List<ChatRoomListDto> chatRoomList = chatRoomService.getChatRoomList(userId, role);

        assertThat(chatRoomList).hasSize(1);
        assertThat(chatRoomList.get(0).getLastMessage()).isEqualTo("Last message");
        assertThat(chatRoomList.get(0).getUnreadCount()).isEqualTo(5);
        then(chatRoomRepository).should(times(1)).findByCustomerId(userId);
        then(chatMessageService).should(times(1)).getLastMessage(chatRoom.getId());
    }

    @Test
    @DisplayName("채팅방 상세 정보 조회 테스트")
    void testGetChatRoomDetail() {
        Long roomId = 1L;
        String role = "ROLE_USER";

        ChatRoom chatRoom = ChatRoom.builder()
                .customer(User.builder().id(1L).build())
                .groomer(User.builder().id(2L).build())
                .estimate(Estimate.builder().estimateRequest(EstimateRequest.builder().build()).build())
                .build();
        GroomerProfile groomerProfile = GroomerProfile.builder().details(GroomerDetails.builder().address("주소").build())
                .build();

        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(groomerProfileRepository.findByUserId(2L)).willReturn(Optional.of(groomerProfile));
        given(chatMessageService.getUnreadOrRecentMessages(roomId, 1L)).willReturn(List.of());

        ChatRoomDetailDto detailDto = chatRoomService.getChatRoomDetail(roomId, role);

        assertThat(detailDto).isNotNull();
        then(chatRoomRepository).should(times(1)).findById(roomId);
        then(groomerProfileRepository).should(times(1)).findByUserId(2L);
    }

    @Test
    @DisplayName("채팅방 종료 테스트")
    void testExitChatRoom() {
        Long roomId = 1L;
        String role = "ROLE_USER";

        ChatRoom chatRoom = ChatRoom.builder()
                .customerLeft(false)
                .groomerLeft(false)
                .build();
        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));

        chatRoomService.exitChatRoom(roomId, role);

        assertThat(chatRoom.getCustomerLeft()).isTrue();
        then(chatRoomRepository).should(times(1)).findById(roomId);
        then(chatRoomRepository).should(times(0)).delete(chatRoom);
    }
}