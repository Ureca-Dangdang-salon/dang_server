package com.dangdangsalon.domain.chat.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.dangdangsalon.domain.chat.entity.ChatMessageMongo;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@DataMongoTest(excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class})
class ChatMessageRepositoryTest {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @AfterEach
    void cleanup() {
        chatMessageRepository.deleteAll();
    }

    @Test
    @DisplayName("RoomID 메시지 조회 테스트")
    void testFindByRoomID() {
        Long roomId = 1L;

        ChatMessageMongo message1 = ChatMessageMongo.builder()
                .roomId(roomId)
                .messageText("메시지1")
                .sendAt(LocalDateTime.now())
                .sequence(1L)
                .build();

        ChatMessageMongo message2 = ChatMessageMongo.builder()
                .roomId(roomId)
                .messageText("메시지2")
                .sendAt(LocalDateTime.now())
                .sequence(2L)
                .build();

        chatMessageRepository.save(message1);
        chatMessageRepository.save(message2);

        List<ChatMessageMongo> chatMessages = chatMessageRepository.findByRoomIdOrderBySendAtDesc(roomId,
                PageRequest.of(0, 10));

        assertThat(chatMessages).hasSize(2);
        assertThat(chatMessages.get(0).getMessageText()).isEqualTo("메시지1");
        assertThat(chatMessages.get(1).getMessageText()).isEqualTo("메시지2");
    }

    @Test
    @DisplayName("Room ID로 메시지 삭제")
    void testDeleteByRoomId() {
        Long roomId = 1L;
        ChatMessageMongo message = ChatMessageMongo.builder()
                .roomId(roomId)
                .messageText("메시지1")
                .sendAt(LocalDateTime.now())
                .sequence(1L)
                .build();

        chatMessageRepository.save(message);

        chatMessageRepository.deleteByRoomId(roomId);

        List<ChatMessageMongo> messages = chatMessageRepository.findByRoomIdOrderBySendAtDesc(roomId,
                PageRequest.of(0, 10));
        assertThat(messages).isEmpty();
    }

    @Test
    @DisplayName("Sequence가 특정 값보다 큰 메시지 조회")
    void testFindByRoomIdAndSequenceGreaterThanOrderBySequenceAsc() {
        Long roomId = 1L;
        ChatMessageMongo message1 = ChatMessageMongo.builder()
                .roomId(roomId)
                .messageText("메시지1")
                .sendAt(LocalDateTime.now().minusMinutes(10))
                .sequence(1L)
                .build();

        ChatMessageMongo message2 = ChatMessageMongo.builder()
                .roomId(roomId)
                .messageText("메시지2")
                .sendAt(LocalDateTime.now())
                .sequence(2L)
                .build();

        chatMessageRepository.save(message1);
        chatMessageRepository.save(message2);

        List<ChatMessageMongo> messages = chatMessageRepository.findByRoomIdAndSequenceGreaterThanOrderBySequenceAsc(
                roomId, 1L);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getMessageText()).isEqualTo("메시지2");
    }
}