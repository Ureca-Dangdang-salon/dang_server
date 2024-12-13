package com.dangdangsalon.chatgpt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TranslateGptRequestDto {
    private String model;
    private List<ChatGPTMessage> messages;
}
