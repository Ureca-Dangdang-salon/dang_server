//package com.dangdangsalon.domain.chat.handler;
//
//import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
//import com.dangdangsalon.domain.chat.service.ChatMessageService;
//import com.dangdangsalon.domain.user.entity.Role;
//import com.dangdangsalon.util.JwtUtil;
//import com.dangdangsalon.util.UUIDUtil;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.persistence.SecondaryTable;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Set;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class ChatSocketHandler extends TextWebSocketHandler {
//
//    private final ObjectMapper objectMapper; // JSON 파싱 용도
//    private final ChatMessageService chatMessageService;
//    private final JwtUtil jwtUtil;
//    private final Map<Long, Set<WebSocketSession>> chatRoomSessions = new ConcurrentHashMap<>();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
////        String query = Objects.requireNonNull(session.getUri()).getQuery();
////        String token = null;
////
////        List<String> cookies = session.getHandshakeHeaders().get("Cookie");
////
////        if (cookies != null && !cookies.isEmpty()) {
////            token = cookies.stream()
////                    .map(this::extractTokenFromCookie)
////                    .filter(Objects::nonNull)
////                    .findFirst()
////                    .orElse(null);
////        }
////
////        if (token != null) {
////            Long senderId = jwtUtil.getUserId(token);
////            String senderRole = jwtUtil.getRole(token);
////
////            session.getAttributes().put("senderId", senderId);
////            session.getAttributes().put("senderRole", senderRole);
////        }
////
////
////        String roomIdParam = query.split("=")[1];
////        Long roomId = Long.parseLong(roomIdParam);
////        log.info(String.valueOf(roomId));
////        String sessionId = session.getId();
////
////        chatRoomSessions.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
////        chatRoomSessions.get(roomId).add(session);
////
////        log.info("WebSocket 연결 roomId: " + roomId + " sessionId: " + sessionId);
//
//        long startTime = System.currentTimeMillis();
//        log.info("WebSocket 연결 시작: sessionId={}, uri={}", session.getId(), session.getUri());
//
//        // 기존 연결 처리 로직
//        super.afterConnectionEstablished(session);
//
//        long endTime = System.currentTimeMillis();
//        log.info("WebSocket 연결 완료: sessionId={}, 소요 시간={}ms", session.getId(), (endTime - startTime));
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        long startTime = System.currentTimeMillis();
//        log.info("메시지 처리 시작: sessionId={}, payload={}", session.getId(), message.getPayload());
//        String payload = message.getPayload();
//        ChatMessageDto chatMessageDto = objectMapper.readValue(payload, ChatMessageDto.class);
//
////        Long senderId = (Long) session.getAttributes().get("senderId");
////        String senderRole = (String) session.getAttributes().get("senderRole");
//
//        Long senderId = 1L;
//        String senderRole = Role.ROLE_USER.name();
//
//        chatMessageDto = ChatMessageDto.builder()
//                .messageId(UUIDUtil.generateTimeBasedUUID())
//                .roomId(chatMessageDto.getRoomId())
//                .senderId(senderId)
//                .senderRole(senderRole)
//                .messageText(chatMessageDto.getMessageText())
//                .imageUrl(chatMessageDto.getImageUrl())
//                .sendAt(LocalDateTime.now())
//                .build();
//
//        chatMessageService.saveMessageRedis(chatMessageDto);
//        chatMessageService.updateLastReadKey(chatMessageDto);
//
//        broadcastToRoom(chatMessageDto.getRoomId(), chatMessageDto);
//        long endTime = System.currentTimeMillis();
//        log.info("메시지 처리 완료: sessionId={}, 소요 시간={}ms", session.getId(), (endTime - startTime));
//    }
//
//    @Override
//    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
//        log.error("WebSocket 에러: " + exception.getMessage());
//        session.close();
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        String sessionId = session.getId();
//        chatRoomSessions.forEach((roomId, sessions) -> sessions.remove(session));
//
//        log.info("WebSocket 연결 종료 sessionId: " + sessionId);
//    }
//
//    @Override
//    public boolean supportsPartialMessages() {
//        return false;
//    }
//
//    private void broadcastToRoom(Long roomId, ChatMessageDto message) throws Exception {
//        Set<WebSocketSession> roomSessionsSet = chatRoomSessions.getOrDefault(roomId, Collections.emptySet());
//
//        for (WebSocketSession session : roomSessionsSet) {
//            if (session.isOpen()) {
//                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
//            }
//        }
//    }
//
//    private String extractTokenFromCookie(String cookieHeader) {
//        return Arrays.stream(cookieHeader.split(";"))
//                .map(String::trim)
//                .filter(cookie -> cookie.startsWith("Authorization="))
//                .map(cookie -> cookie.substring("Authorization=".length()))
//                .findFirst()
//                .orElse(null);
//    }
//}
