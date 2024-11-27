package com.dangdangsalon.domain.chat.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.chat.dto.ChatRoomDetailDto;
import com.dangdangsalon.domain.chat.dto.ChatRoomListDto;
import com.dangdangsalon.domain.chat.dto.CreateChatRoomRequestDto;
import com.dangdangsalon.domain.chat.dto.CreateChatRoomResponseDto;
import com.dangdangsalon.domain.chat.service.ChatRoomService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatrooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public ApiSuccess<?> createChatRoom(@RequestBody CreateChatRoomRequestDto createChatRoomRequestDto) {

        CreateChatRoomResponseDto createChatRoomResponse = chatRoomService.createChatRoom(createChatRoomRequestDto);

        return ApiUtil.success(createChatRoomResponse);
    }

    @GetMapping
    public ApiSuccess<?> getChatRoomList(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        String role = user.getRole();

        List<ChatRoomListDto> chatRoomList = chatRoomService.getChatRoomList(userId, role);

        return ApiUtil.success(chatRoomList);
    }

    @GetMapping("/{roomId}/enter")
    public ApiSuccess<?> enterChatRoom(@PathVariable Long roomId, @AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();

        ChatRoomDetailDto chatRoomDetail = chatRoomService.getChatRoomDetail(roomId, userId);

        return ApiUtil.success(chatRoomDetail);
    }
}
