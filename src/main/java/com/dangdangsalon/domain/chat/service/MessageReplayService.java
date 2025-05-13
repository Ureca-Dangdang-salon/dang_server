package com.dangdangsalon.domain.chat.service;

import com.dangdangsalon.domain.chat.entity.FailedChatMessage;
import com.dangdangsalon.domain.chat.repository.FailedChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageReplayService {

    private final FailedChatMessageRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void replayById(String id) {
        FailedChatMessage failedChatMessage = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Replay 대상 메시지를 찾을 수 없습니다."));

        kafkaTemplate.send(failedChatMessage.getTopic().replace(".DLT", ""),
                failedChatMessage.getMessage());

        failedChatMessage.updateReplayed(true);
        repository.save(failedChatMessage);
    }
}
