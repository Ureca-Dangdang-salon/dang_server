package com.dangdangsalon.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

//@Configuration
//@EnableWebSocket
//@RequiredArgsConstructor
//public class WebSocketConfig implements WebSocketConfigurer {
//
//    private final WebSocketHandler chatSocketHandler;
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(chatSocketHandler, "/ws/chat").setAllowedOrigins("*");
//    }
//}
