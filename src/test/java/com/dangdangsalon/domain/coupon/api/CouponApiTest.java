package com.dangdangsalon.domain.coupon.api;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.coupon.dto.CouponInfoResponseDto;
import com.dangdangsalon.domain.coupon.dto.CouponMainResponseDto;
import com.dangdangsalon.domain.coupon.dto.CouponUserResponseDto;
import com.dangdangsalon.domain.coupon.service.CouponIssueService;
import com.dangdangsalon.domain.coupon.service.CouponService;
import com.dangdangsalon.util.JwtUtil;
import io.restassured.RestAssured;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class CouponApiTest {
    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CouponIssueService couponIssueService;

    @MockBean
    private CouponService couponService;

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssuredMockMvc.mockMvc(mockMvc);

        given(jwtUtil.isExpired(anyString())).willReturn(false);
        given(jwtUtil.getUserId(anyString())).willReturn(1L);
        given(jwtUtil.getUsername(anyString())).willReturn("testUser");
        given(jwtUtil.getRole(anyString())).willReturn("ROLE_USER");
    }

    @Test
    @DisplayName("쿠폰 발급 요청 테스트")
    void issueCoupon() {
        // Given
        when(couponIssueService.joinQueue(anyLong(), anyLong())).thenReturn("대기열에 참여했습니다.");

        // When & Then
        RestAssuredMockMvc
                .given()
                .cookie("Authorization", "mock.jwt.token")
                .queryParam("eventId", 2)
                .when()
                .post("/api/coupons/issued")
                .then()
                .statusCode(200)
                .body("response", equalTo("대기열에 참여했습니다."));

        verify(couponIssueService, times(1)).joinQueue(1L, 2L);
    }

    @Test
    @DisplayName("SSE 대기열 업데이트 구독 테스트")
    void subscribeQueueUpdates() {
        // Given
        SseEmitter emitter = new SseEmitter();
        when(couponIssueService.subscribeQueueUpdates(anyLong(), anyLong())).thenReturn(emitter);

        // When & Then
        RestAssuredMockMvc
                .given()
                .cookie("Authorization", "mock.jwt.token")
                .queryParam("eventId", 2)
                .when()
                .get("/api/coupons/queue/updates")
                .then()
                .statusCode(200);

        verify(couponIssueService, times(1)).subscribeQueueUpdates(1L, 2L);
    }

    @Test
    @DisplayName("유효한 쿠폰 메인 페이지 조회 테스트")
    void getCouponValidMainPage() {
        // Given
        List<CouponMainResponseDto> response = List.of(
                CouponMainResponseDto.builder().eventName("이벤트1").build(),
                CouponMainResponseDto.builder().eventName("이벤트2").build()
        );
        when(couponService.getCouponValidMainPage()).thenReturn(response);

        // When & Then
        RestAssuredMockMvc
                .given()
                .cookie("Authorization", "mock.jwt.token")
                .when()
                .get("/api/coupons/main")
                .then()
                .statusCode(200)
                .body("response.size()", equalTo(2))
                .body("response[0].eventName", equalTo("이벤트1"))
                .body("response[1].eventName", equalTo("이벤트2"));

        verify(couponService, times(1)).getCouponValidMainPage();
    }

    @Test
    @DisplayName("특정 이벤트 쿠폰 정보 조회 테스트")
    void getCouponInfo() {
        // Given
        CouponInfoResponseDto response = CouponInfoResponseDto.builder()
                .eventId(1L)
                .name("이벤트1")
                .discountAmount(5000)
                .build();
        when(couponService.getCouponInfo(1L)).thenReturn(response);

        // When & Then
        RestAssuredMockMvc
                .given()
                .cookie("Authorization", "mock.jwt.token")
                .pathParam("eventId", 1)
                .when()
                .get("/api/coupons/{eventId}")
                .then()
                .statusCode(200)
                .body("response.name", equalTo("이벤트1"));

        verify(couponService, times(1)).getCouponInfo(1L);
    }

    @Test
    @DisplayName("사용자 쿠폰 조회 테스트")
    void getUserCoupon() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        // Given
        List<CouponUserResponseDto> response = List.of(
                CouponUserResponseDto.builder()
                        .couponId(1L)
                        .name("쿠폰1")
                        .build()
                ,
                CouponUserResponseDto.builder()
                        .couponId(2L)
                        .name("쿠폰2")
                        .build()
        );
        given(couponService.getUserCoupon(anyLong())).willReturn(response);

        // When & Then
        RestAssuredMockMvc
                .given()
                .when()
                .get("/api/coupons/users")
                .then()
                .statusCode(200)
                .body("response.size()", equalTo(2))
                .body("response[0].name", equalTo("쿠폰1"))
                .body("response[1].name", equalTo("쿠폰2"));

        verify(couponService, times(1)).getUserCoupon(anyLong());
    }
}
