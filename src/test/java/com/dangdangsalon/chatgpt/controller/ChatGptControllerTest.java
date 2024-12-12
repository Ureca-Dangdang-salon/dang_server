package com.dangdangsalon.chatgpt.controller;

import com.dangdangsalon.chatgpt.dto.GenerateImageAnalysisResponseDto;
import com.dangdangsalon.chatgpt.service.ChatGptService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatGptController.class)
@ActiveProfiles("test")
@MockBean(JpaMetamodelMappingContext.class)
class ChatGptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatGptService chatGptService;

    private static final String GENERATE_IMAGE_URL = "/api/gpt/generate";
    private static final String PROMPT = "하이바컷";

    @Test
    @DisplayName("이미지 생성 성공 테스트")
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testGenerateImageSuccess() throws Exception {
        // Given
        String expectedImageUrl = "http://mock-generated-image.com";
        GenerateImageAnalysisResponseDto responseDto = GenerateImageAnalysisResponseDto.builder()
                .imageUrl(expectedImageUrl)
                .build();

        // Mock 파일 생성
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // 서비스 메소드 모킹
        when(chatGptService.generateDogStyleImage(any(String.class), any()))
                .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(multipart(GENERATE_IMAGE_URL)
                        .file(mockFile)
                        .param("prompt", PROMPT)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.imageUrl").value(expectedImageUrl))
                .andExpect(handler().handlerType(ChatGptController.class))
                .andExpect(handler().methodName("generateImage"));

        verify(chatGptService, times(1)).generateDogStyleImage(eq(PROMPT), any());
    }

    @Test
    @DisplayName("파일 없이 요청 시 400 Bad Request 테스트")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGenerateImageWithoutFile() throws Exception {
        mockMvc.perform(multipart(GENERATE_IMAGE_URL)
                        .param("prompt", PROMPT)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}