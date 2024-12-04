package com.dangdangsalon.domain.payment.api;

import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.estimate.request.dto.ServicePriceResponseDto;
import com.dangdangsalon.domain.payment.controller.PaymentController;
import com.dangdangsalon.domain.payment.dto.*;
import com.dangdangsalon.domain.payment.service.PaymentGetService;
import com.dangdangsalon.domain.payment.service.PaymentService;
import com.dangdangsalon.util.JwtUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PaymentApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PaymentGetService paymentGetService;

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
    @DisplayName("결제 승인 테스트")
    @WithMockUser(username = "testUser", roles = {"USER"})
    void approvePayment() {

        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        when(mockLoginUser.getUserId()).thenReturn(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        // 요청 및 응답 DTO 생성
        PaymentApproveRequestDto requestDto = PaymentApproveRequestDto.builder()
                .paymentKey("paykey123")
                .orderId("order123")
                .amount(10000)
                .build();

        PaymentApproveResponseDto responseDto = PaymentApproveResponseDto.builder()
                .paymentKey("paykey123")
                .totalAmount(10000)
                .status("ACCEPTED")
                .approvedAt(OffsetDateTime.now())
                .method("간편 결제")
                .build();

        // Mock 서비스 설정
        given(paymentService.approvePayment(any(PaymentApproveRequestDto.class), eq(1L))).willReturn(responseDto);

        // API 테스트 실행
        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .header("Idempotency-Key", "test-idempotency-key")
                .body(requestDto)
                .when()
                .post("/api/payments/approve")
                .then()
                .statusCode(200)
                .body("response.paymentKey", equalTo("paykey123"))
                .body("response.totalAmount", equalTo(10000))
                .body("response.status", equalTo("ACCEPTED"))
                .body("response.method", equalTo("간편 결제"));
    }


    @Test
    @DisplayName("결제 취소 테스트")
    @WithMockUser(username = "testUser", roles = {"USER"})
    void cancelPayment() {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        when(mockLoginUser.getUserId()).thenReturn(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        PaymentCancelRequestDto requestDto = PaymentCancelRequestDto.builder()
                .paymentKey("paykey123")
                .cancelReason("맘에 안들어요")
                .build();

        PaymentCancelResponseDto responseDto = PaymentCancelResponseDto.builder()
                .paymentKey("paykey123")
                .orderId("order123")
                .status("CANCELED")
                .build();

        given(paymentService.cancelPayment(any(PaymentCancelRequestDto.class), eq(1L))).willReturn(responseDto);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .header("Idempotency-Key", "test-idempotency-key")
                .body(requestDto)
                .when()
                .post("/api/payments/cancel")
                .then()
                .statusCode(200)
                .body("response.paymentKey", equalTo("paykey123"))
                .body("response.orderId", equalTo("order123"))
                .body("response.status", equalTo("CANCELED"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("결제 내역 조회 테스트")
    void getPayments() {
        CustomOAuth2User mockUser = Mockito.mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        List<PaymentResponseDto> payments = List.of(
                PaymentResponseDto.builder()
                        .paymentDate(LocalDateTime.now())
                        .totalAmount(50000)
                        .status("ACCEPTED")
                        .dogProfileList(List.of(
                                PaymentDogProfileResponseDto.builder()
                                        .profileId(1L)
                                        .dogName("멍멍이")
                                        .aggressionCharge(1000)
                                        .healthIssueCharge(500)
                                        .servicePriceList(List.of(
                                                new ServicePriceResponseDto(101L, "목욕", 20000),
                                                new ServicePriceResponseDto(102L, "미용", 15000)
                                        ))
                                        .build(),
                                PaymentDogProfileResponseDto.builder()
                                        .profileId(2L)
                                        .dogName("댕댕이")
                                        .aggressionCharge(1500)
                                        .healthIssueCharge(700)
                                        .servicePriceList(List.of(
                                                new ServicePriceResponseDto(103L, "손톱 관리", 5000)
                                        ))
                                        .build()
                        ))
                        .build()
        );

        given(mockUser.getUserId()).willReturn(1L);
        given(paymentGetService.getPayments(1L)).willReturn(payments);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/payments")
                .then()
                .statusCode(200)
                .body("response", hasSize(1))
                .body("response[0].totalAmount", equalTo(50000))
                .body("response[0].status", equalTo("ACCEPTED"))
                .body("response[0].dogProfileList", hasSize(2))
                .body("response[0].dogProfileList[0].profileId", equalTo(1))
                .body("response[0].dogProfileList[0].dogName", equalTo("멍멍이"))
                .body("response[0].dogProfileList[0].aggressionCharge", equalTo(1000))
                .body("response[0].dogProfileList[0].healthIssueCharge", equalTo(500))
                .body("response[0].dogProfileList[0].servicePriceList", hasSize(2))
                .body("response[0].dogProfileList[0].servicePriceList[0].serviceId", equalTo(101))
                .body("response[0].dogProfileList[0].servicePriceList[0].description", equalTo("목욕"))
                .body("response[0].dogProfileList[0].servicePriceList[0].price", equalTo(20000))
                .body("response[0].dogProfileList[1].profileId", equalTo(2))
                .body("response[0].dogProfileList[1].dogName", equalTo("댕댕이"))
                .body("response[0].dogProfileList[1].servicePriceList[0].serviceId", equalTo(103))
                .body("response[0].dogProfileList[1].servicePriceList[0].description", equalTo("손톱 관리"))
                .body("response[0].dogProfileList[1].servicePriceList[0].price", equalTo(5000));
    }
}
