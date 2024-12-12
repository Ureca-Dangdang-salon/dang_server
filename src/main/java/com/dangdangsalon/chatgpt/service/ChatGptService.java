package com.dangdangsalon.chatgpt.service;

import com.dangdangsalon.chatgpt.dto.*;
import com.dangdangsalon.domain.s3image.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatGptService {

    private final RestTemplate restTemplate;
    private final HttpHeaders httpHeaders;

    @Value("${openai.gpt-url}")
    private String gptUrl;

    @Value("${openai.url}")
    private String apiUrl;

    public GenerateImageResponseDto generateDogStyleImage(String userPrompt, MultipartFile file) throws IOException {
        // 1. 사진 인코딩 작업
        String base64Image = encodeImageToBase64(file);
        String imageUrl = "data:image/jpeg;base64," + base64Image;

        log.info("Encoded image to Base64: {}", imageUrl);

        // 2. 이미지 분석
        String analysisResult = analyzeImageWithOpenAI(imageUrl);
        log.info("Image analysis result: {}", analysisResult);

        // 3. 사용자 입력 값 영어로 변경
        String translatedPrompt = translateKoToEng(userPrompt);
        log.info("Translated user prompt: {}", translatedPrompt);

        // 4. 최종 프롬프트 완성
        String detailedPrompt = createFinalPrompt(translatedPrompt, analysisResult);
        log.info("Detailed prompt: {}", detailedPrompt);

        // 5. 이미지 생성
        String generatedImageUrl = createImageFromDescription(detailedPrompt);

        return GenerateImageResponseDto.builder()
                .imageUrl(generatedImageUrl)
                .build();
    }

    private String encodeImageToBase64(MultipartFile file) throws IOException {
        return Base64.encodeBase64String(file.getBytes());
    }

    private String createFinalPrompt(String translatedPrompt, String analysisResult) {
        return String.format(
                "The image shows %s. Please style the dog as follows: %s. Ensure the result emphasizes cleanliness and stylishness while maintaining the dog's natural features.",
                analysisResult,
                translatedPrompt
        );
    }

//    private String createFinalPrompt(String translatedPrompt, String analysisResult) {
//        return String.format(
//                "The uploaded image shows %s. Based on the provided style request: %s. Please ensure the result maintains the dog's natural features and creates a clean, stylish look that matches the given description.",
//                analysisResult,
//                translatedPrompt
//        );
//    }

    private String translateKoToEng(String prompt) {
        TranslateRequestDto requestDto = TranslateRequestDto.builder()
                .model("gpt-4o")
                .messages(List.of(
                        MessageResponseDto.builder()
                                .role("user")
                                .content("Translate the following Korean text to English: " + prompt)
                                .build()
                ))
                .build();

        HttpEntity<TranslateRequestDto> entity = new HttpEntity<>(requestDto, httpHeaders);
        ResponseEntity<TranslateResponseDto> response = restTemplate.postForEntity(gptUrl, entity, TranslateResponseDto.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            ChoiceResponseDto choice = response.getBody().getChoices().get(0);
            return choice.getMessage().getContent().trim();
        }
        throw new RuntimeException("Failed to translate text.");
    }

    private String createImageFromDescription(String prompt) {
        ImageRequestDto requestDto = ImageRequestDto.builder()
                .model("dall-e-3")
                .prompt(prompt)
                .n(1)
                .size("1024x1024")
                .build();

        HttpEntity<ImageRequestDto> entity = new HttpEntity<>(requestDto, httpHeaders);
        ResponseEntity<ImageResponseDto> response = restTemplate.postForEntity(apiUrl, entity, ImageResponseDto.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<ImageDataResponseDto> dataList = response.getBody().getData();
            if (!dataList.isEmpty()) {
                log.info("Generated image URL: {}", dataList.get(0).getUrl());
                return dataList.get(0).getUrl();
            }
            throw new RuntimeException("No image URLs returned.");
        }
        throw new RuntimeException("Failed to generate image.");
    }

    private String analyzeImageWithOpenAI(String imageUrl) {
        TranslateGptRequestDto request = TranslateGptRequestDto.builder()
                .model("gpt-4o")
                .messages(List.of(
                        ChatGPTMessage.builder()
                                .role("user")
                                .content(List.of(
                                        Map.of("type", "text", "text", "Analyze this image."),
                                        Map.of("type", "image_url", "image_url", Map.of("url", imageUrl))
                                ))
                                .build()
                ))
                .build();

        HttpEntity<TranslateGptRequestDto> entity = new HttpEntity<>(request, httpHeaders);
        ResponseEntity<TranslateResponseDto> response = restTemplate.postForEntity(gptUrl, entity, TranslateResponseDto.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            ChoiceResponseDto choice = response.getBody().getChoices().get(0);
            return choice.getMessage().getContent().trim();
        }
        throw new RuntimeException("Failed to analyze image with OpenAI.");
    }
}
