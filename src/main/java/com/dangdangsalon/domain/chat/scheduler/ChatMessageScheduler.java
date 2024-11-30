package com.dangdangsalon.domain.chat.scheduler;

import com.dangdangsalon.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public class ChatMessageScheduler {

    private final ChatMessageService chatMessageService;

    @Scheduled(cron = "0 32 11 * * *")
    public void saveMessageMongo() {
        log.info("Redis -> Mongo save()");

        try {
            chatMessageService.saveAllMessagesMongo();
        } catch (Exception e) {
            log.error("Redis -> Mongo 저장 실패");
        }

        log.info("Redis -> Mongo 저장 완료");
    }
}
