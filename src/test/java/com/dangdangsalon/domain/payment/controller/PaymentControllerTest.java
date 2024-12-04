package com.dangdangsalon.domain.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.estimate.request.dto.ServicePriceResponseDto;
import com.dangdangsalon.domain.payment.dto.*;
import com.dangdangsalon.domain.payment.service.PaymentGetService;
import com.dangdangsalon.domain.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@WebMvcTest(PaymentController.class)
@ActiveProfiles("test")
@MockBean(JpaMetamodelMappingContext.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PaymentGetService paymentGetService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("결제 승인 요청 테스트")
    void testApprovePayment() throws Exception {
        // Mock 사용자 설정
        CustomOAuth2User mockUser = Mockito.mock(CustomOAuth2User.class);
        when(mockUser.getUserId()).thenReturn(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        // 요청 및 응답 DTO
        PaymentApproveRequestDto requestDto = PaymentApproveRequestDto.builder()
                .paymentKey("pay_123")
                .orderId("order_456")
                .amount(10000)
                .build();

        PaymentApproveResponseDto responseDto = PaymentApproveResponseDto.builder()
                .paymentKey("pay_123")
                .totalAmount(10000)
                .status("ACCEPTED")
                .approvedAt(OffsetDateTime.now())
                .method("간편 결제")
                .build();

        // Mock 설정
        given(paymentService.approvePayment(any(PaymentApproveRequestDto.class), eq(1L)))
                .willReturn(responseDto);

        // API 호출 및 검증
        mockMvc.perform(post("/api/payments/approve")
                        .contentType("application/json")
                        .header("Idempotency-Key", "idempotency-key")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.paymentKey").value("pay_123"))
                .andExpect(jsonPath("$.response.totalAmount").value(10000))
                .andExpect(jsonPath("$.response.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.response.method").value("간편 결제"));

        verify(paymentService, times(1)).approvePayment(any(PaymentApproveRequestDto.class), eq(1L));
    }



    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("결제 취소 요청 테스트")
    void testCancelPayment() throws Exception {
        CustomOAuth2User mockUser = Mockito.mock(CustomOAuth2User.class);
        when(mockUser.getUserId()).thenReturn(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        PaymentCancelRequestDto requestDto = PaymentCancelRequestDto.builder()
                .paymentKey("pay_123")
                .cancelReason("User request")
                .build();

        PaymentCancelResponseDto responseDto = PaymentCancelResponseDto.builder()
                .paymentKey("pay_123")
                .orderId("order_456")
                .status("CANCELED")
                .build();

        given(paymentService.cancelPayment(any(PaymentCancelRequestDto.class), eq(1L)))
                .willReturn(responseDto);

        mockMvc.perform(post("/api/payments/cancel")
                        .contentType("application/json")
                        .header("Idempotency-Key", "idempotency-key")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.paymentKey").value("pay_123"))
                .andExpect(jsonPath("$.response.orderId").value("order_456"))
                .andExpect(jsonPath("$.response.status").value("CANCELED"));

        verify(paymentService, times(1)).cancelPayment(any(PaymentCancelRequestDto.class), eq(1L));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("결제 내역 조회 테스트")
    void testGetPayments() throws Exception {
        CustomOAuth2User mockLoginUser = Mockito.mock(CustomOAuth2User.class);
        when(mockLoginUser.getUserId()).thenReturn(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        List<PaymentResponseDto> responseDtos = List.of(
                PaymentResponseDto.builder()
                        .paymentDate(LocalDateTime.of(2024, 11, 26, 10, 0))
                        .totalAmount(10000)
                        .status("ACCEPTED")
                        .dogProfileList(List.of(
                                PaymentDogProfileResponseDto.builder()
                                        .profileId(1L)
                                        .dogName("구름이")
                                        .servicePriceList(List.of(
                                                new ServicePriceResponseDto(101L, "발톱 정리", 5000),
                                                new ServicePriceResponseDto(102L, "목욕", 3000)
                                        ))
                                        .aggressionCharge(500)
                                        .healthIssueCharge(500)
                                        .build()
                        ))
                        .build(),
                PaymentResponseDto.builder()
                        .paymentDate(LocalDateTime.of(2024, 11, 25, 15, 0))
                        .totalAmount(5000)
                        .status("ACCEPTED")
                        .dogProfileList(List.of(
                                PaymentDogProfileResponseDto.builder()
                                        .profileId(2L)
                                        .dogName("둥둥이")
                                        .servicePriceList(List.of(
                                                new ServicePriceResponseDto(201L, "피부 미용", 4000)
                                        ))
                                        .aggressionCharge(500)
                                        .healthIssueCharge(0)
                                        .build()
                        ))
                        .build()
        );

        given(paymentGetService.getPayments(1L)).willReturn(responseDtos);

        mockMvc.perform(get("/api/payments")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response[0].paymentDate").value("2024-11-26T10:00:00"))
                .andExpect(jsonPath("$.response[0].totalAmount").value(10000))
                .andExpect(jsonPath("$.response[0].status").value("ACCEPTED"))
                .andExpect(jsonPath("$.response[0].dogProfileList[0].profileId").value(1))
                .andExpect(jsonPath("$.response[0].dogProfileList[0].dogName").value("구름이"))
                .andExpect(jsonPath("$.response[0].dogProfileList[0].servicePriceList[0].description").value("발톱 정리"))
                .andExpect(jsonPath("$.response[0].dogProfileList[0].servicePriceList[0].price").value(5000))
                .andExpect(jsonPath("$.response[1].paymentDate").value("2024-11-25T15:00:00"))
                .andExpect(jsonPath("$.response[1].totalAmount").value(5000))
                .andExpect(jsonPath("$.response[1].status").value("ACCEPTED"))
                .andExpect(jsonPath("$.response[1].dogProfileList[0].profileId").value(2))
                .andExpect(jsonPath("$.response[1].dogProfileList[0].dogName").value("둥둥이"))
                .andExpect(jsonPath("$.response[1].dogProfileList[0].servicePriceList[0].description").value("피부 미용"))
                .andExpect(jsonPath("$.response[1].dogProfileList[0].servicePriceList[0].price").value(4000));

        verify(paymentGetService, times(1)).getPayments(1L);
    }

}
