package com.dangdangsalon.domain.payment.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.payment.dto.*;
import com.dangdangsalon.domain.payment.service.PaymentGetService;
import com.dangdangsalon.domain.payment.service.PaymentService;
import com.dangdangsalon.util.ApiUtil;
import lombok.RequiredArgsConstructor;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentApproveService;
    private final PaymentGetService paymentGetService;

    /**
     * 결제 승인 요청
     * @param paymentApproveRequestDto 결제 요청 데이터
     * @return 결제 승인 결과
     */
    @PostMapping("/approve")
    public ApiSuccess<?> approvePayment(@RequestBody PaymentApproveRequestDto paymentApproveRequestDto, @AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        PaymentApproveResponseDto paymentResponse = paymentApproveService.approvePayment(paymentApproveRequestDto, userId);
        return ApiUtil.success(paymentResponse);
    }

    /**
     * 결제 취소 요청
     * @param paymentCancelRequestDto 결제 취소 요청 데이터
     * @return 결제 취소 결과
     */
    @PostMapping("/cancel")
    public ApiSuccess<?> cancelPayment(@RequestBody PaymentCancelRequestDto paymentCancelRequestDto, @AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        PaymentCancelResponseDto cancelResponse = paymentApproveService.cancelPayment(paymentCancelRequestDto, userId);
        return ApiUtil.success(cancelResponse);
    }

    @GetMapping
    public ApiSuccess<?> getPayment(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        List<PaymentResponseDto> paymentResponseList = paymentGetService.getPayments(userId);
        return ApiUtil.success(paymentResponseList);
    }
}
