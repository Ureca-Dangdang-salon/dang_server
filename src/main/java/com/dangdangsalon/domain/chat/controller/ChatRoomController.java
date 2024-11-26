package com.dangdangsalon.domain.chat.controller;

import com.dangdangsalon.domain.chat.dto.CreateChatRoomRequestDto;
import com.dangdangsalon.domain.chat.dto.CreateChatRoomResponseDto;
import com.dangdangsalon.domain.chat.service.ChatRoomService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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
}
