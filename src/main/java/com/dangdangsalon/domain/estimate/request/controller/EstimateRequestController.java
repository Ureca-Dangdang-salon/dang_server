package com.dangdangsalon.domain.estimate.request.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.estimate.request.dto.*;
import com.dangdangsalon.domain.estimate.request.service.EstimateRequestDetailService;
import com.dangdangsalon.domain.estimate.request.service.EstimateRequestServices;
import com.dangdangsalon.domain.estimate.request.service.GroomerEstimateRequestService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/estimaterequest")
public class EstimateRequestController {

    private final EstimateRequestServices estimateRequestServices;
    private final GroomerEstimateRequestService groomerEstimateRequestService;
    private final EstimateRequestDetailService estimateRequestDetailService;

    /**
     *  견적 요청 등록
     */
    @PostMapping
    public ApiSuccess<?> createEstimateRequest(@RequestBody EstimateRequestDto estimateRequestDto, @AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        estimateRequestServices.insertEstimateRequest(estimateRequestDto, userId);
        return ApiUtil.success("견적 요청 등록에 성공하였습니다.");
    }

    /**
     *  미용사에게 전달된 견적 요청 조회
     */
    @GetMapping("/{groomerProfileId}")
    public ApiSuccess<?> getEstimateRequests(@PathVariable Long groomerProfileId) {
        List<EstimateRequestResponseDto> estimateRequests = groomerEstimateRequestService.getEstimateRequest(groomerProfileId);
        return ApiUtil.success(estimateRequests);
    }

    /**
     *  반려견별 견적 요청의 상세 정보를 조회
     */
    @GetMapping("/detail/{requestId}")
    public ApiSuccess<?> getEstimateRequestDetail(@PathVariable Long requestId) {
        List<EstimateDetailResponseDto> estimateRequestDetailList = estimateRequestDetailService.getEstimateRequestDetail(requestId);
        return ApiUtil.success(estimateRequestDetailList);
    }

    /**
     *  미용사 견적 요청 삭제 버튼 클릭 api
     */
    @DeleteMapping("/{requestId}/{groomerProfileId}")
    public ApiSuccess<?> deleteEstimateRequest(@PathVariable Long requestId, @PathVariable Long groomerProfileId) {
        groomerEstimateRequestService.deleteGroomerEstimateRequest(requestId, groomerProfileId);
        return ApiUtil.success("견적 요청 삭제에 성공하였습니다.");
    }

    /**
     * 유저가 본인의 견적 요청 목록을 조회
     */
    @GetMapping("/my")
    public ApiSuccess<?> getMyEstimateRequests(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        List<MyEstimateRequestResponseDto> myEstimateRequests = estimateRequestServices.getMyEstimateRequest(userId);
        return ApiUtil.success(myEstimateRequests);
    }

    /**
     * 유저가 본인의 견적 요청 상세 조회 (채팅)
     */
    @GetMapping("/my/detail/{requestId}")
    public ApiSuccess<?> getMyEstimateRequestDetail(@PathVariable Long requestId) {
        List<MyEstimateRequestDetailResponseDto> myEstimateRequestDetailList = estimateRequestDetailService.getMyEstimateDetailRequest(requestId);
        return ApiUtil.success(myEstimateRequestDetailList);
    }

    /**
     * 견적 요청 상태를 CANCEL 로 변경
     */
    @PutMapping("/{requestId}/stop")
    public ApiSuccess<?> stopEstimate(@PathVariable Long requestId) {
        estimateRequestServices.stopEstimate(requestId);
        return ApiUtil.success("견적 그만 받기에 성공하였습니다.");
    }
}