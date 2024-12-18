package com.dangdangsalon.domain.chat.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.auth.dto.UserDto;
import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.dto.ChatRoomDetailDto;
import com.dangdangsalon.domain.chat.dto.ChatRoomListDto;
import com.dangdangsalon.domain.chat.dto.CreateChatRoomRequestDto;
import com.dangdangsalon.domain.chat.dto.CreateChatRoomResponseDto;
import com.dangdangsalon.domain.chat.service.ChatMessageService;
import com.dangdangsalon.domain.chat.service.ChatRoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(ChatRoomController.class)
@ActiveProfiles("test")
@MockBean(JpaMetamodelMappingContext.class)
class ChatRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatRoomService chatRoomService;

    @MockBean
    private ChatMessageService chatMessageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("채팅방 생성 요청 성공 테스트")
    @WithMockUser
    void createChatRoomTest() throws Exception {
        CreateChatRoomRequestDto requestDto = new CreateChatRoomRequestDto(1L);
        CreateChatRoomResponseDto responseDto = CreateChatRoomResponseDto.builder().roomId(1L).build();

        given(chatRoomService.createChatRoom(any(CreateChatRoomRequestDto.class))).willReturn(responseDto);

        mockMvc.perform(post("/api/chatrooms")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.roomId").value(1L));

        then(chatRoomService).should(times(1)).createChatRoom(any(CreateChatRoomRequestDto.class));
    }

    @Test
    @DisplayName("채팅방 목록 조회 성공 테스트")
    @WithMockUser
    void getChatRoomListTest() throws Exception {
        Long userId = 1L;
        String role = "ROLE_USER";
        ChatRoomListDto chatRoomListDto = ChatRoomListDto.builder().roomId(1L).lastMessage("Last message")
                .unreadCount(5).build();
        given(chatRoomService.getChatRoomList(userId, role)).willReturn(List.of(chatRoomListDto));

        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        given(mockLoginUser.getUserId()).willReturn(1L);
        given(mockLoginUser.getRole()).willReturn("ROLE_USER");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        mockMvc.perform(get("/api/chatrooms")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response[0].roomId").value(1L))
                .andExpect(jsonPath("$.response[0].lastMessage").value("Last message"))
                .andExpect(jsonPath("$.response[0].unreadCount").value(5));

        then(chatRoomService).should(times(1)).getChatRoomList(userId, role);
    }

    @Test
    @DisplayName("채팅방 상세 정보 조회 성공 테스트")
    @WithMockUser
    void getChatRoomDetailTest() throws Exception {
        Long roomId = 1L;
        String role = "ROLE_USER";
        ChatRoomDetailDto chatRoomDetailDto = ChatRoomDetailDto.builder().roomId(1L).build();

        given(chatRoomService.getChatRoomDetail(roomId, role)).willReturn(chatRoomDetailDto);

        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        given(mockLoginUser.getUserId()).willReturn(1L);
        given(mockLoginUser.getRole()).willReturn("ROLE_USER");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        mockMvc.perform(get("/api/chatrooms/{roomId}/enter", roomId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.roomId").value(1L));

        then(chatRoomService).should(times(1)).getChatRoomDetail(roomId, role);
    }

    @Test
    @DisplayName("이전 메시지 조회 성공 테스트")
    @WithMockUser
    void getPreviousMessagesTest() throws Exception {
        Long roomId = 1L;
        ChatMessageDto messageDto = ChatMessageDto.builder().messageId("1L").messageText("Hello").build();

        given(chatMessageService.getPreviousMessages(roomId, 1L)).willReturn(List.of(messageDto));

        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        given(mockLoginUser.getUserId()).willReturn(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        mockMvc.perform(get("/api/chatrooms/{roomId}/messages/previous", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response[0].messageId").value("1L"))
                .andExpect(jsonPath("$.response[0].messageText").value("Hello"));

        then(chatMessageService).should(times(1)).getPreviousMessages(roomId, 1L);
    }

    @Test
    @DisplayName("채팅방 종료 성공 테스트")
    @WithMockUser
    void exitChatRoomTest() throws Exception {
        Long roomId = 1L;
        String role = "ROLE_USER";

        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        given(mockLoginUser.getUserId()).willReturn(1L);
        given(mockLoginUser.getRole()).willReturn("ROLE_USER");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        mockMvc.perform(post("/api/chatrooms/{roomId}/exit", roomId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("채팅방을 나갔습니다."));

        then(chatRoomService).should(times(1)).exitChatRoom(roomId, role);
    }
}