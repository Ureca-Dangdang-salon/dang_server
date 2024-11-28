package com.dangdangsalon.domain.chat;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.service.ChatMessageService;
import com.dangdangsalon.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.SecondaryTable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper; // JSON 파싱 용도
    private final ChatMessageService chatMessageService;
    private final JwtUtil jwtUtil;
    private final Map<Long, Set<WebSocketSession>> chatRoomSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String cookieHeader = session.getHandshakeHeaders().getFirst("Cookie");

        if (cookieHeader != null) {
            String token = extractTokenFromCookie(cookieHeader);

            if (token != null) {
                Long senderId = jwtUtil.getUserId(token);
                String senderRole = jwtUtil.getRole(token);

                session.getAttributes().put("senderId", senderId);
                session.getAttributes().put("senderRole", senderRole);
            }
        }

        String query = Objects.requireNonNull(session.getUri()).getQuery();
        String roomIdParam = query.split("=")[1];
        Long roomId = Long.parseLong(roomIdParam);
        String sessionId = session.getId();

        chatRoomSessions.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
        chatRoomSessions.get(roomId).add(session);

        log.info("WebSocket 연결 roomId: " + roomId + " sessionId: " + sessionId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        ChatMessageDto chatMessageDto = objectMapper.readValue(payload, ChatMessageDto.class);

        Long senderId = (Long) session.getAttributes().get("senderId");
        String senderRole = (String) session.getAttributes().get("senderRole");

        chatMessageDto = ChatMessageDto.builder()
                .messageId(UUID.randomUUID().toString())
                .roomId(chatMessageDto.getRoomId())
                .senderId(senderId)
                .senderRole(senderRole)
                .messageText(chatMessageDto.getMessageText())
                .imageUrl(chatMessageDto.getImageUrl())
                .sendAt(LocalDateTime.now())
                .build();

        chatMessageService.saveMessageRedis(chatMessageDto);
        chatMessageService.updateLastReadKey(chatMessageDto);

        broadcastToRoom(chatMessageDto.getRoomId(), chatMessageDto);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 에러: " + exception.getMessage());
        session.close();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        chatRoomSessions.forEach((roomId, sessions) -> sessions.remove(session));

        log.info("WebSocket 연결 종료 sessionId: " + sessionId);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void broadcastToRoom(Long roomId, ChatMessageDto message) throws Exception {
        Set<WebSocketSession> roomSessionsSet = chatRoomSessions.getOrDefault(roomId, Collections.emptySet());

        for (WebSocketSession session : roomSessionsSet) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            }
        }
    }

    private String extractTokenFromCookie(String cookieHeader) {
        return Arrays.stream(cookieHeader.split(";"))
                .map(String::trim)
                .filter(cookie -> cookie.startsWith("Authorization="))
                .map(cookie -> cookie.substring("Authorization=".length()))
                .findFirst()
                .orElse(null);
    }
}
