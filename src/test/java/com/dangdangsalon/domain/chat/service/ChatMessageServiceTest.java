package com.dangdangsalon.domain.chat.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.util.ChatConst;
import com.dangdangsalon.domain.chat.util.ChatRedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ChatRedisUtil chatRedisUtil;

    @Mock
    private ChatMessageMongoService chatMessageMongoService;

    @InjectMocks
    private ChatMessageService chatMessageService;

    @Test
    @DisplayName("Redis에 메시지 저장 테스트")
    void testSaveMessageRedis() {
        ChatMessageDto message = ChatMessageDto.builder()
                .roomId(1L)
                .messageText("메시지1")
                .sendAt(LocalDateTime.now())
                .build();

        given(chatRedisUtil.getNextSequence(1L)).willReturn(1L);

        chatMessageService.saveMessageRedis(message);

        assertThat(message.getSequence()).isEqualTo(1L);
        then(chatRedisUtil).should(times(1)).saveMessage(message);
    }

    @Test
    @DisplayName("Redis에서 읽지 않은 메시지 수 조회 테스트")
    void testGetUnreadCount() {
        given(chatRedisUtil.getCurrentSequence(1L)).willReturn(10L);
        given(chatRedisUtil.getLastReadSequence(1L, 1L)).willReturn(5L);

        int unreadCount = chatMessageService.getUnreadCount(1L, 1L);

        assertThat(unreadCount).isEqualTo(5);
    }

    @Test
    @DisplayName("읽지 않은 메시지부터 최신 메시지까지 조회 테스트")
    void testGetUnreadOrRecentMessages_WithUnreadMessages() {
        ChatMessageDto message1 = ChatMessageDto.builder()
                .sequence(6L)
                .messageText("메시지 6")
                .build();

        ChatMessageDto message2 = ChatMessageDto.builder()
                .sequence(10L)
                .messageText("메시지 10")
                .build();

        given(chatRedisUtil.getLastReadSequence(1L, 1L)).willReturn(5L);
        given(chatRedisUtil.getCurrentSequence(1L)).willReturn(10L);
        given(chatRedisUtil.getMessagesBySequence(1L, 6L, 10L))
                .willReturn(List.of(message1, message2));

        given(objectMapper.convertValue(message1, ChatMessageDto.class)).willReturn(message1);
        given(objectMapper.convertValue(message2, ChatMessageDto.class)).willReturn(message2);

        List<ChatMessageDto> messages = chatMessageService.getUnreadOrRecentMessages(1L, 1L);

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getMessageText()).isEqualTo("메시지 6");
        then(chatRedisUtil).should(times(1)).updateFirstLoadedSequence(1L, 1L, 6L);
    }

    @Test
    @DisplayName("Redis에 메시지가 없는 경우 MongoDB에서 조회 테스트")
    void testGetUnreadOrRecentMessages_WithoutUnreadMessagesInRedis() {
        ChatMessageDto message = ChatMessageDto.builder()
                .sequence(1L)
                .messageText("Mongo 메시지")
                .build();

        given(chatRedisUtil.getLastReadSequence(1L, 1L)).willReturn(0L);
        given(chatRedisUtil.getCurrentSequence(1L)).willReturn(1L);
        given(chatMessageMongoService.getMessagesFromSequence(1L, 0L, ChatConst.MESSAGE_GET_LIMIT.getCount()))
                .willReturn(List.of(message));

        List<ChatMessageDto> messages = chatMessageService.getUnreadOrRecentMessages(1L, 1L);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getMessageText()).isEqualTo("Mongo 메시지");
    }

    @Test
    @DisplayName("채팅 데이터 삭제 테스트")
    void testDeleteChatData() {
        chatMessageService.deleteChatData(1L);

        then(chatRedisUtil).should(times(1)).deleteRoomData(1L);
        then(chatMessageMongoService).should(times(1)).deleteChatMessages(1L);
    }
}