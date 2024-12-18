package com.dangdangsalon.domain.chat.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.entity.ChatMessageMongo;
import com.dangdangsalon.domain.chat.repository.ChatMessageRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;


@DataMongoTest
@ActiveProfiles("test")
class ChatMessageMongoServiceTest {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private ChatMessageMongoService chatMessageMongoService;

    @BeforeEach
    void setUp() {
        chatMessageMongoService = new ChatMessageMongoService(chatMessageRepository, mongoTemplate);
    }

    @AfterEach
    void cleanup() {
        chatMessageRepository.deleteAll();
    }

    @Test
    @DisplayName("채팅 메시지 저장 및 조회 테스트")
    void testSaveAndRetrieveChatMessage() {
        ChatMessageDto message = ChatMessageDto.builder()
                .roomId(1L)
                .sequence(1L)
                .messageText("메시지1")
                .sendAt(LocalDateTime.now())
                .build();

        chatMessageMongoService.saveChatMessage(message);

        List<ChatMessageDto> messages = chatMessageMongoService.getChatMessagesInMongo(1L, 0, 10);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getMessageText()).isEqualTo("메시지1");
        assertThat(messages.get(0).getRoomId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("특정 Sequence 이후 메시지 조회 테스트")
    void testGetMessagesFromSequence() {
        saveSampleMessages(1L);

        List<ChatMessageDto> messages = chatMessageMongoService.getMessagesFromSequence(1L, 1L, 5);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getSequence()).isEqualTo(2L);
    }

    @Test
    @DisplayName("특정 Sequence 이전 메시지 조회 테스트")
    void testGetMessagesBeforeSequence() {
        saveSampleMessages(1L);

        List<ChatMessageDto> messages = chatMessageMongoService.getMessagesBeforeSequence(1L, 2L, 5);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getSequence()).isEqualTo(1L);
    }

    @Test
    @DisplayName("채팅 메시지 삭제 테스트")
    void testDeleteChatMessages() {
        saveSampleMessages(1L);

        chatMessageMongoService.deleteChatMessages(1L);

        List<ChatMessageDto> messages = chatMessageMongoService.getChatMessagesInMongo(1L, 0, 10);

        assertThat(messages).isEmpty();
    }

    @Test
    @DisplayName("가장 큰 Sequence 조회 테스트")
    void testGetMaxSequence() {
        saveSampleMessages(1L);

        Long maxSequence = chatMessageMongoService.getMaxSequence(1L);

        assertThat(maxSequence).isEqualTo(2L);
    }

    private void saveSampleMessages(Long roomId) {
        ChatMessageMongo message1 = ChatMessageMongo.builder()
                .roomId(roomId)
                .sequence(1L)
                .messageText("메시지1")
                .sendAt(LocalDateTime.now().minusMinutes(10))
                .build();

        ChatMessageMongo message2 = ChatMessageMongo.builder()
                .roomId(roomId)
                .sequence(2L)
                .messageText("메시지2")
                .sendAt(LocalDateTime.now())
                .build();

        chatMessageRepository.save(message1);
        chatMessageRepository.save(message2);
    }
}