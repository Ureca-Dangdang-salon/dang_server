package com.dangdangsalon.domain.estimate.request.controller;

import com.dangdangsalon.domain.estimate.request.dto.EstimateDetailResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.EstimateRequestDto;
import com.dangdangsalon.domain.estimate.request.dto.EstimateResponseDto;
import com.dangdangsalon.domain.estimate.request.service.EstimateRequestDetailService;
import com.dangdangsalon.domain.estimate.request.service.EstimateRequestServices;
import com.dangdangsalon.domain.estimate.request.service.GroomerEstimateRequestService;
import com.dangdangsalon.util.ApiUtil;
import lombok.RequiredArgsConstructor;
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
     * userId 토큰에서 가져오는 걸로 추후 변경
     */
    @PostMapping
    public ApiUtil.ApiSuccess<?> createEstimateRequest(@RequestBody EstimateRequestDto estimateRequestDto, @RequestParam Long userId) {
        estimateRequestServices.insertEstimateRequest(estimateRequestDto, userId);
        return ApiUtil.success("견적 요청 등록에 성공하였습니다.");
    }

    /**
     *  미용사에게 전달된 견적 요청 조회
     */
    @GetMapping("/{groomerProfileId}")
    public ApiUtil.ApiSuccess<?> getEstimateRequests(@PathVariable Long groomerProfileId) {
        List<EstimateResponseDto> estimateRequests = groomerEstimateRequestService.getEstimateRequest(groomerProfileId);
        return ApiUtil.success(estimateRequests);
    }

    /**
     *  반려견별 견적 요청의 상세 정보를 조회
     */
    @GetMapping("/detail/{requestId}")
    public ApiUtil.ApiSuccess<?> getEstimateRequestDetail(@PathVariable Long requestId) {
        List<EstimateDetailResponseDto> estimateRequestDetailList = estimateRequestDetailService.getEstimateRequestDetail(requestId);
        return ApiUtil.success(estimateRequestDetailList);
    }

    /**
     *  미용사 견적 요청 삭제 버튼 클릭 api
     */
    @PutMapping("/{requestId}/cancel")
    public ApiUtil.ApiSuccess<?> cancelEstimateRequest(@PathVariable Long requestId) {
        groomerEstimateRequestService.cancelGroomerEstimateRequest(requestId);
        return ApiUtil.success("견적 요청 삭제에 성공하였습니다.");
    }
}