package com.dangdangsalon.domain.review.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerDetails;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerServiceArea;
import com.dangdangsalon.domain.groomerprofile.review.controller.ReviewController;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewGroomerResponseDto;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewInsertRequestDto;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewUpdateRequestDto;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewUserResponseDto;
import com.dangdangsalon.domain.groomerprofile.review.entity.Review;
import com.dangdangsalon.domain.groomerprofile.review.service.ReviewService;
import com.dangdangsalon.domain.region.entity.City;
import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@WebMvcTest(controllers = ReviewController.class)
@MockBean(JpaMetamodelMappingContext.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Test
    @DisplayName("내가 쓴 리뷰 조회 테스트")
    public void getUserReviewSuccess() throws Exception {

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

        City city = City.builder()
                .name("서울시")
                .build();
        District district = District.builder()
                .name("종로구")
                .city(city)
                .build();

        // Mock 데이터 설정
        User user = User.builder()
                .name("user1")
                .district(district)
                .build();

        GroomerProfile profile = GroomerProfile.builder()
                .name("groomer1")
                .build();

        GroomerServiceArea area = GroomerServiceArea.builder()
                .district(district)
                .groomerProfile(profile)
                .build();
        ReflectionTestUtils.setField(profile, "groomerServiceAreas", List.of(area));

        // Review 생성 및 저장
        Review review1 = Review.builder()
                .text("내용1")
                .starScore(4)
                .user(user)
                .groomerProfile(profile)
                .reviewImages(new ArrayList<>())
                .build();

        Review review2 = Review.builder()
                .text("내용2")
                .starScore(5)
                .user(user)
                .groomerProfile(profile)
                .reviewImages(new ArrayList<>())
                .build();

        // 리뷰 데이터 예시
        List<Review> reviews = Arrays.asList(review1, review2);

        List<ReviewUserResponseDto> list = reviews.stream()
                .map(ReviewUserResponseDto::fromEntity)
                .toList();

        // 리뷰 조회 서비스 모킹
        given(reviewService.getUserReviews(mockUserId)).willReturn(list);

        // GET 요청으로 리뷰 조회
        // When && Then
        mockMvc.perform(get("/api/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))  // CSRF 토큰을 포함하여 요청
                .andExpect(status().isOk())  // 상태 코드 200 OK 확인
                .andExpect(jsonPath("$.response[0].text").value("내용1"))  // 첫 번째 리뷰 내용 확인
                .andExpect(jsonPath("$.response[1].text").value("내용2"));  // 두 번째 리뷰 내용 확인
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("미용사에게 쓴 리뷰 조회 테스트")
    public void getGroomerReviewSuccess() throws Exception {
        Long profileId = 1L;

        City city = City.builder()
                .name("city")
                .build();
        District district = District.builder()
                .city(city)
                .name("district")
                .build();
        User user = User.builder()
                .name("user1")
                .district(district)
                .build();


        Review review1 = Review.builder()
                .text("내용1")
                .starScore(4)
                .user(user)
                .reviewImages(new ArrayList<>())
                .build();

        Review review2 = Review.builder()
                .text("내용2")
                .starScore(5)
                .user(user)
                .reviewImages(new ArrayList<>())
                .build();

        List<Review> reviews = Arrays.asList(review1, review2);

        List<ReviewGroomerResponseDto> list = reviews.stream()
                .map(ReviewGroomerResponseDto::fromEntity)
                .toList();

        given(reviewService.getGroomerReviews(profileId)).willReturn(list);

        // When && Then
        mockMvc.perform(get("/api/review/{profileId}", profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response[0].text").value("내용1"))
                .andExpect(jsonPath("$.response[1].text").value("내용2"));
    }

    @Test
    @DisplayName("리뷰 등록 테스트")
    public void insertReviewSuccess() throws Exception {
        Long userId = 1L;
        Long profileId = 2L;

        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUserId()).thenReturn(userId);

        // SecurityContext에 사용자 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ReviewInsertRequestDto requestDto = ReviewInsertRequestDto.builder()
                .text("리뷰 등록")
                .starScore(4)
                .imageKey(List.of("imageKey1", "imageKey2"))
                .build();

        doNothing().when(reviewService).insertReview(userId, profileId, requestDto);

        // When && Then
        mockMvc.perform(post("/api/review/{profileId}", profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto))
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken(customOAuth2User, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("리뷰 등록이 완료되었습니다."));
    }

    @Test
    @DisplayName("리뷰 수정 테스트")
    public void updateReviewSuccess() throws Exception {
        Long userId = 1L;
        Long reviewId = 3L;

        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUserId()).thenReturn(userId);

        // SecurityContext에 사용자 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ReviewUpdateRequestDto requestDto = ReviewUpdateRequestDto.builder()
                .text("리뷰 수정")
                .reviewImages(List.of("imageKey1", "imageKey2"))
                .build();

        doNothing().when(reviewService).updateReview(userId, reviewId, requestDto);

        // When && Then
        mockMvc.perform(put("/api/review/{reviewId}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto))
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken(customOAuth2User, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("리뷰 수정이 완료되었습니다."));
    }

    @Test
    @DisplayName("리뷰 삭제 테스트")
    public void deleteReviewSuccess1() throws Exception {
        Long userId = 1L;
        Long reviewId = 3L;

        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUserId()).thenReturn(userId);

        // SecurityContext에 사용자 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        doNothing().when(reviewService).deleteReview(userId, reviewId);

        // When && Then
        mockMvc.perform(delete("/api/review/{reviewId}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken(customOAuth2User, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("리뷰 삭제가 완료되었습니다."));
    }

}