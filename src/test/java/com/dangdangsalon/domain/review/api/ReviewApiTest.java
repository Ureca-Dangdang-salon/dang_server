package com.dangdangsalon.domain.review.api;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewGroomerResponseDto;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewInsertRequestDto;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewUpdateRequestDto;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewUserResponseDto;
import com.dangdangsalon.domain.groomerprofile.review.service.ReviewService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ReviewApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("내가 쓴 리뷰 조회")
    void getUserReview() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        List<ReviewUserResponseDto> reviews = List.of(
                ReviewUserResponseDto.builder()
                        .reviewId(1L)
                        .profileId(1L)
                        .groomerName("미용사1")
                        .groomerImageKey("imageKey1")
                        .text("더미 리뷰 내용입니다.1")
                        .starScore(4)
                        .address("서울시 강남구")
                        .reviewImages(List.of("dummy-image1.jpg", "dummy-image2.jpg"))
                        .build()
        );
        given(reviewService.getUserReviews(anyLong())).willReturn(reviews);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/review")
                .then()
                .statusCode(200)
                .body("response.size()", equalTo(1))
                .body("response[0].groomerName", equalTo("미용사1"))
                .body("response[0].starScore", equalTo(4F));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("미용사에게 쓴 리뷰 조회")
    void getGroomerReview() {
        List<ReviewGroomerResponseDto> reviews = List.of(
                ReviewGroomerResponseDto.builder()
                        .reviewId(1L)
                        .userName("유저")
                        .userImageKey("user-image-key")
                        .text("리뷰입니다.")
                        .starScore(4.5)
                        .city("서울")
                        .district("강남구")
                        .reviewImages(List.of("image1.jpg", "image2.jpg"))
                        .build()
        );
        given(reviewService.getGroomerReviews(anyLong())).willReturn(reviews);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/review/1")
                .then()
                .statusCode(200)
                .body("response.size()", equalTo(1))
                .body("response[0].userName", equalTo("유저"))
                .body("response[0].starScore", equalTo(4.5F));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("리뷰 등록")
    void insertReview() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        ReviewInsertRequestDto requestDto = new ReviewInsertRequestDto("좋은 서비스 감사합니다.", 5,
                List.of("image1"));

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/api/review/1")
                .then()
                .statusCode(200)
                .body("response", equalTo("리뷰 등록이 완료되었습니다."));

        Mockito.verify(reviewService).insertReview(anyLong(), anyLong(), any(ReviewInsertRequestDto.class));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("리뷰 수정")
    void updateReview() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        ReviewUpdateRequestDto requestDto = new ReviewUpdateRequestDto("수정된 리뷰 내용입니다.", List.of("image2"));

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .put("/api/review/1")
                .then()
                .statusCode(200)
                .body("response", equalTo("리뷰 수정이 완료되었습니다."));

        Mockito.verify(reviewService).updateReview(anyLong(), anyLong(), any(ReviewUpdateRequestDto.class));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("리뷰 삭제")
    void deleteReview() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/review/1")
                .then()
                .statusCode(200)
                .body("response", equalTo("리뷰 삭제가 완료되었습니다."));

        Mockito.verify(reviewService).deleteReview(anyLong(), anyLong());
    }
}
