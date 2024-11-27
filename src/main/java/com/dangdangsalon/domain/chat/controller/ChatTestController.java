package com.dangdangsalon.domain.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class ChatTestController {

    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }
}
