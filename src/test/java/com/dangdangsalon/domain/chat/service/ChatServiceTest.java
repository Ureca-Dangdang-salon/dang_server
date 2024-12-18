package com.dangdangsalon.domain.chat.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mockStatic;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.util.UUIDUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ChatServiceTest {

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private ChatService chatService;

    private MockedStatic<UUIDUtil> mockedUUIDUtil;

    @BeforeEach
    void setUp() {
        mockedUUIDUtil = mockStatic(UUIDUtil.class);
    }

    @AfterEach
    void down() {
        mockedUUIDUtil.close();
    }

    @Test
    @DisplayName("메시지 생성 및 저장 테스트")
    void testCreateAndSaveMessage() {
        ChatMessageDto inputMessage = ChatMessageDto.builder()
                .messageText("Test message")
                .imageUrl("test-image.jpg")
                .build();

        Long roomId = 1L;
        Long senderId = 100L;
        String senderRole = "USER";
        String generatedUUID = "test-uuid";

        given(UUIDUtil.generateTimeBasedUUID()).willReturn(generatedUUID);

        ChatMessageDto resultMessage = chatService.createAndSaveMessage(inputMessage, roomId, senderId, senderRole);

        assertThat(resultMessage.getMessageId()).isEqualTo(generatedUUID);
        assertThat(resultMessage.getRoomId()).isEqualTo(roomId);
        assertThat(resultMessage.getSenderId()).isEqualTo(senderId);
        assertThat(resultMessage.getSenderRole()).isEqualTo(senderRole);
        assertThat(resultMessage.getMessageText()).isEqualTo("Test message");
        assertThat(resultMessage.getImageUrl()).isEqualTo("test-image.jpg");
        assertThat(resultMessage.getSendAt()).isNotNull();

        then(chatMessageService).should(times(1)).saveMessageRedis(resultMessage);
        then(chatMessageService).should(times(1)).updateLastReadKey(resultMessage);
    }

    @Test
    @DisplayName("ChatMessageDto로 메시지 생성 및 저장 테스트")
    void testCreateAndSaveMessage_withChatMessageDto() {
        ChatMessageDto inputMessage = ChatMessageDto.builder()
                .roomId(1L)
                .senderId(100L)
                .senderRole("USER")
                .messageText("Test message")
                .imageUrl("test-image.jpg")
                .build();

        String generatedUUID = "test-uuid";

        given(UUIDUtil.generateTimeBasedUUID()).willReturn(generatedUUID);

        ChatMessageDto resultMessage = chatService.createAndSaveMessage(inputMessage);

        assertThat(resultMessage.getMessageId()).isEqualTo(generatedUUID);
        assertThat(resultMessage.getRoomId()).isEqualTo(1L);
        assertThat(resultMessage.getSenderId()).isEqualTo(100L);
        assertThat(resultMessage.getSenderRole()).isEqualTo("USER");
        assertThat(resultMessage.getMessageText()).isEqualTo("Test message");
        assertThat(resultMessage.getImageUrl()).isEqualTo("test-image.jpg");
        assertThat(resultMessage.getSendAt()).isNotNull();

        then(chatMessageService).should(times(1)).saveMessageRedis(resultMessage);
        then(chatMessageService).should(times(1)).updateLastReadKey(resultMessage);
    }
}