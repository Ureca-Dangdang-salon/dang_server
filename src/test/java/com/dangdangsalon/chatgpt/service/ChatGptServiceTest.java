package com.dangdangsalon.chatgpt.service;

import com.dangdangsalon.chatgpt.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatGptServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ChatGptService chatGptService;

    private MultipartFile mockFile;


    @BeforeEach
    void setUp() {
        mockFile = mock(MultipartFile.class);

        ReflectionTestUtils.setField(chatGptService, "gptUrl", "https://api.openai.com/v1/chat/completions");
        ReflectionTestUtils.setField(chatGptService, "apiUrl", "https://api.openai.com/v1/images/generations");
    }

    @Test
    @DisplayName("이미지 생성 성공")
    void generateDogStyleImage_Success() throws IOException {
        // Given
        when(mockFile.getBytes()).thenReturn("mockImageBytes".getBytes());

        TranslateResponseDto translationResponse = TranslateResponseDto.builder()
                .choices(Collections.singletonList(
                        ChoiceResponseDto.builder()
                                .message(MessageResponseDto.builder()
                                        .content("A cute golden retriever")
                                        .build())
                                .build()))
                .build();

        TranslateResponseDto analysisResponse = TranslateResponseDto.builder()
                .choices(Collections.singletonList(
                        ChoiceResponseDto.builder()
                                .message(MessageResponseDto.builder()
                                        .content("Golden retriever with fluffy fur")
                                        .build())
                                .build()))
                .build();

        ImageResponseDto imageResponse = ImageResponseDto.builder()
                .data(Collections.singletonList(
                        ImageDataResponseDto.builder()
                                .url("http://mock-generated-image.com")
                                .build()))
                .build();

        when(restTemplate.postForEntity(eq("https://api.openai.com/v1/chat/completions"), any(HttpEntity.class), eq(TranslateResponseDto.class)))
                .thenReturn(ResponseEntity.ok(translationResponse))
                .thenReturn(ResponseEntity.ok(analysisResponse));

        when(restTemplate.postForEntity(eq("https://api.openai.com/v1/images/generations"), any(HttpEntity.class), eq(ImageResponseDto.class)))
                .thenReturn(ResponseEntity.ok(imageResponse));

        // When
        GenerateImageAnalysisResponseDto result = chatGptService.generateDogStyleImage("강아지 스타일 요청", mockFile);

        // Then
        assertThat(result.getImageUrl()).isEqualTo("http://mock-generated-image.com");
    }

    @Test
    @DisplayName("이미지 생성 실패 - 분석 오류")
    void generateDogStyleImage_AnalysisError() throws IOException {
        // Given
        when(mockFile.getBytes()).thenReturn("mockImageBytes".getBytes());
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TranslateResponseDto.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> chatGptService.generateDogStyleImage("강아지 스타일 요청", mockFile));
    }

    @Test
    @DisplayName("이미지 생성 실패 - 생성 오류")
    void generateDogStyleImage_GenerationError() throws IOException {
        // Given
        when(mockFile.getBytes()).thenReturn("mockImageBytes".getBytes());

        TranslateResponseDto translationResponse = TranslateResponseDto.builder()
                .choices(Collections.singletonList(
                        ChoiceResponseDto.builder()
                                .message(MessageResponseDto.builder()
                                        .content("A cute golden retriever")
                                        .build())
                                .build()))
                .build();

        TranslateResponseDto analysisResponse = TranslateResponseDto.builder()
                .choices(Collections.singletonList(
                        ChoiceResponseDto.builder()
                                .message(MessageResponseDto.builder()
                                        .content("Golden retriever with fluffy fur")
                                        .build())
                                .build()))
                .build();

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TranslateResponseDto.class)))
                .thenReturn(ResponseEntity.ok(translationResponse))
                .thenReturn(ResponseEntity.ok(analysisResponse));

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(ImageResponseDto.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> chatGptService.generateDogStyleImage("강아지 스타일 요청", mockFile));
    }

    @Test
    @DisplayName("Base64 인코딩 테스트")
    void encodeImageToBase64() throws Exception {
        when(mockFile.getBytes()).thenReturn("mockImageBytes".getBytes());

        Method method = ChatGptService.class.getDeclaredMethod("encodeImageToBase64", MultipartFile.class);
        method.setAccessible(true);

        String result = (String) method.invoke(chatGptService, mockFile);

        assertThat(result).isEqualTo("bW9ja0ltYWdlQnl0ZXM=");
    }
}
