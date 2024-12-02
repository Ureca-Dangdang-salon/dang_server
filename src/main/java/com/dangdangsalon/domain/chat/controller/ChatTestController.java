package com.dangdangsalon.domain.chat.controller;

import com.dangdangsalon.domain.chat.service.ChatMessageService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequiredArgsConstructor
public class ChatTestController {

    private final ChatMessageService chatMessageService;

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
}
