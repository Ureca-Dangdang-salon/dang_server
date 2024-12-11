package com.dangdangsalon.chatgpt.controller;

import com.dangdangsalon.chatgpt.dto.GenerateImageResponseDto;
import com.dangdangsalon.chatgpt.service.ChatGptService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gpt")
public class ChatGptController {

    private final ChatGptService chatGptService;

    @PostMapping("/generate")
    public ApiSuccess<?> generateImage(@RequestParam("prompt") String prompt, @RequestParam("file") MultipartFile file) throws IOException{
        GenerateImageResponseDto responseDto = chatGptService.generateDogStyleImage(prompt, file);
        return ApiUtil.success(responseDto);
    }
}
