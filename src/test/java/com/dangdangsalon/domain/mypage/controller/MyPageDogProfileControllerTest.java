package com.dangdangsalon.domain.mypage.controller;

import com.dangdangsalon.domain.dogprofile.entity.Gender;
import com.dangdangsalon.domain.dogprofile.entity.Neutering;
import com.dangdangsalon.domain.estimate.request.dto.FeatureResponseDto;
import com.dangdangsalon.domain.mypage.dto.req.DogProfileRequestDto;
import com.dangdangsalon.domain.mypage.dto.res.MyDogProfileResponseDto;
import com.dangdangsalon.domain.mypage.dto.res.UserProfileResponseDto;
import com.dangdangsalon.domain.mypage.service.MyPageDogProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@WebMvcTest(controllers = MyPageDogProfileController.class)
@MockBean(JpaMetamodelMappingContext.class)
class MyPageDogProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MyPageDogProfileService myPageDogProfileService;

    @Test
    @DisplayName("유저 프로필 조회 테스트")
    void getUserProfileTest() throws Exception {
        // Mock CustomOAuth2User 설정
        Long mockUserId = 1L;
        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUserId()).thenReturn(mockUserId);

        // SecurityContext에 사용자 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // UserProfileResponseDto 더미 데이터 생성
        UserProfileResponseDto mockResponse = UserProfileResponseDto.builder()
                .name("John Doe")
                .profileImage("profileImage123")
                .email("john.doe@example.com")
                .build();

        // 서비스의 getUserProfile 메서드 모킹
        when(myPageDogProfileService.getUserProfile(mockUserId)).thenReturn(mockResponse);

        // MockMvc를 사용하여 GET 요청
        mockMvc.perform(get("/api/dogprofile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()) // CSRF 토큰 포함
                        .principal(authentication))
                .andExpect(status().isOk())  // 상태 코드 200 확인
                .andExpect(jsonPath("$.response.name").value("John Doe")) // 이름 검증
                .andExpect(jsonPath("$.response.profileImage").value("profileImage123")) // 프로필 이미지 검증
                .andExpect(jsonPath("$.response.email").value("john.doe@example.com")); // 이메일 검증
    }

    @Test
    @DisplayName("반려견 프로필 등록 테스트")
    void saveDogProfileTest() throws Exception {
        // Mock CustomOAuth2User 설정
        Long mockUserId = 1L;
        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUserId()).thenReturn(mockUserId);

        // SecurityContext에 사용자 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 반려견 프로필 데이터 예시
        DogProfileRequestDto mockRequestDto = new DogProfileRequestDto(
                "Buddy",
                "imageUrl",
                "Golden Retriever",
                3,
                6,
                Gender.MALE,
                Neutering.N,
                20,
                List.of(1L, 2L),
                "Friendly and playful"
        );
        doNothing().when(myPageDogProfileService).saveDogProfile(mockRequestDto, mockUserId);

        // POST 요청으로 반려견 프로필 등록
        mockMvc.perform(post("/api/dogprofile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(mockRequestDto))  // 요청 본문 설정
                        .with(csrf())  // CSRF 토큰을 포함하여 요청
                        .principal(new UsernamePasswordAuthenticationToken(customOAuth2User, null)))  // 인증 정보 추가
                .andExpect(status().isOk())  // 상태 코드 200 OK 확인
                .andExpect(jsonPath("$.response").value("반려견 프로필 등록이 완료되었습니다."));  // 응답 메시지 확인
    }

    @Test
    @DisplayName("반려견 프로필 조회 테스트")
    void getDogProfileTest() throws Exception {
        // Mock CustomOAuth2User 설정
        Long mockUserId = 1L;
        Long dogProfileId = 1L;
        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUserId()).thenReturn(mockUserId);

        // SecurityContext에 사용자 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 반려견 프로필 데이터 예시
        MyDogProfileResponseDto mockResponse = new MyDogProfileResponseDto(
                "Buddy",                     // 이름
                "imageKey123",               // 프로필 이미지 키
                "Golden Retriever",          // 종
                3,                           // 나이 (연)
                6,                           // 나이 (월)
                Gender.MALE,                 // 성별
                Neutering.Y,          // 중성화 여부
                20,                          // 체중
                List.of(new FeatureResponseDto("활기참"), new FeatureResponseDto("물 무서워함"))                     // 특징 목록
        );
        when(myPageDogProfileService.getDogProfile(mockUserId, dogProfileId)).thenReturn(mockResponse);

        // GET 요청으로 반려견 프로필 조회
        mockMvc.perform(get("/api/dogprofile/{dogProfileId}", dogProfileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())  // CSRF 토큰을 포함하여 요청
                        .principal(new UsernamePasswordAuthenticationToken(customOAuth2User, null)))  // 인증 정보 추가
                .andExpect(status().isOk())  // 상태 코드 200 OK 확인
                .andExpect(jsonPath("$.response.name").value("Buddy"))  // 반려견 이름 확인
                .andExpect(jsonPath("$.response.species").value("Golden Retriever"))  // 반려견 종 확인
                .andExpect(jsonPath("$.response.ageYear").value(3));  // 반려견 생일 확인
    }

    @Test
    @DisplayName("반려견 프로필 수정 테스트")
    void updateDogProfileTest() throws Exception {
        // Mock CustomOAuth2User 설정
        Long mockUserId = 1L;
        Long dogProfileId = 1L;
        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUserId()).thenReturn(mockUserId);

        // SecurityContext에 사용자 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 수정할 반려견 프로필 데이터 예시
        DogProfileRequestDto mockRequestDto = new DogProfileRequestDto(
                "Buddy",               // 이름
                "imageKey123",         // 프로필 이미지
                "Golden Retriever",    // 종
                3,                     // 나이 (연)
                6,                     // 나이 (월)
                Gender.MALE,           // 성별
                Neutering.Y,    // 중성화 여부
                20,                    // 체중
                List.of(1L, 2L, 3L),            // 특징 ID 목록
                "Loves playing fetch"  // 추가 특징
        );
        doNothing().when(myPageDogProfileService).updateDogProfile(mockRequestDto, mockUserId, dogProfileId);

        // PUT 요청으로 반려견 프로필 수정
        mockMvc.perform(put("/api/dogprofile/{dogProfileId}", dogProfileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(mockRequestDto))  // 요청 본문 설정
                        .with(csrf())  // CSRF 토큰을 포함하여 요청
                        .principal(new UsernamePasswordAuthenticationToken(customOAuth2User, null)))  // 인증 정보 추가
                .andExpect(status().isOk())  // 상태 코드 200 OK 확인
                .andExpect(jsonPath("$.response").value("반려견 프로필 수정이 완료되었습니다."));  // 응답 메시지 확인
    }

    @Test
    @DisplayName("반려견 프로필 삭제 테스트")
    void deleteDogProfileTest() throws Exception {
        // Mock CustomOAuth2User 설정
        Long mockUserId = 1L;
        Long dogProfileId = 1L;
        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUserId()).thenReturn(mockUserId);

        // SecurityContext에 사용자 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 반려견 프로필 삭제
        doNothing().when(myPageDogProfileService).deleteDogProfile(mockUserId, dogProfileId);

        // DELETE 요청으로 반려견 프로필 삭제
        mockMvc.perform(delete("/api/dogprofile/{dogProfileId}", dogProfileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())  // CSRF 토큰을 포함하여 요청
                        .principal(new UsernamePasswordAuthenticationToken(customOAuth2User, null)))  // 인증 정보 추가
                .andExpect(status().isOk())  // 상태 코드 200 OK 확인
                .andExpect(jsonPath("$.response").value("반려견 프로필 삭제가 완료되었습니다."));  // 응답 메시지 확인
    }

}