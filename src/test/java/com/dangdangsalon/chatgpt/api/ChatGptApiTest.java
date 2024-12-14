package com.dangdangsalon.chatgpt.api;

import com.dangdangsalon.chatgpt.dto.GenerateImageAnalysisResponseDto;
import com.dangdangsalon.chatgpt.service.ChatGptService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ChatGptApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatGptService chatGptService;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    @DisplayName("이미지 생성 요청 테스트")
    @WithMockUser(username = "testUser", roles = {"USER"})
    void generateImageTest() throws Exception {
        GenerateImageAnalysisResponseDto mockResponse = new GenerateImageAnalysisResponseDto("generated-image-url","강아지는 행복해보여요","카리나","imageUrl");
        when(chatGptService.generateDogStyleImage(anyString(), any())).thenReturn(mockResponse);

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/gpt/generate")
                        .file(mockFile)
                        .param("prompt", "곰돌이 컷")
                        .contentType(MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.imageUrl").value("generated-image-url"));
    }
}
