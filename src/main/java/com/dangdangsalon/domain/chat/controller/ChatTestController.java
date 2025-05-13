package com.dangdangsalon.domain.chat.controller;

import brave.Tracer;
import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.service.ChatMessageService;
import com.dangdangsalon.domain.chat.util.KafkaChatMessageProducer;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import com.dangdangsalon.util.KafkaTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/chat-test")
public class ChatTestController {

    private final ChatMessageService chatMessageService;
    private final KafkaChatMessageProducer kafkaProducer;
    private final Tracer tracer;

    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }

    @PostMapping("/chat/save")
    @ResponseBody
    public ApiSuccess<?> saveChat() {
        chatMessageService.saveAllMessagesMongo();

        return ApiUtil.success( "Redis -> Mongo 저장 완료");
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendTestMessage(@RequestBody ChatMessageDto message) {
        kafkaProducer.sendMessage(KafkaTopic.CHAT_TOPIC.getTopic(), message);
        return ResponseEntity.ok("메시지 전송 완료");
    }

    @GetMapping("/trace-test")
    public String testTrace() {
        Long traceId = tracer.currentSpan().context().traceId();
        log.info("테스트용 Trace ID = {}", traceId);
        return "Trace ID: " + traceId;
    }
}
