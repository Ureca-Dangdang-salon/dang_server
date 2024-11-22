package com.dangdangsalon.domain.estimate.controller;

import com.dangdangsalon.domain.estimate.dto.*;
import com.dangdangsalon.domain.estimate.request.dto.EstimateDetailResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.EstimateRequestResponseDto;
import com.dangdangsalon.domain.estimate.service.EstimateService;
import com.dangdangsalon.domain.estimate.service.EstimateWriteService;
import com.dangdangsalon.util.ApiUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/estimate")
public class EstimateController {

    private final EstimateWriteService estimateWriteService;
    private final EstimateService estimateService;

    // 견적서 작성 반려견 요청 목록 조회
    @GetMapping("/dogrequest/{requestId}")
    public ApiSuccess<?> getEstimateRequestDog(@PathVariable Long requestId) {
        List<EstimateWriteResponseDto> estimateWriteResponseDtoList = estimateWriteService.getEstimateRequestDog(requestId);
        return ApiUtil.success(estimateWriteResponseDtoList);
    }

    // 견적서 작성 반려견 요청 상세 보기
    @GetMapping("/dogrequest/{requestId}/detail/{dogProfileId}")
    public ApiSuccess<?> getEstimateRequestDogDetail(@PathVariable Long requestId, @PathVariable Long dogProfileId) {
        EstimateWriteDetailResponseDto detailResponseDto = estimateWriteService.getEstimateRequestDogDetail(requestId, dogProfileId);
        return ApiUtil.success(detailResponseDto);
    }

    // 견적서 등록
    @PostMapping
    public ApiSuccess<?> insertEstimate(@RequestBody EstimateWriteRequestDto requestDto) {
        estimateService.insertEstimate(requestDto);
        return ApiUtil.success("견적서가 성공적으로 등록되었습니다.");
    }

    // 견적서 수정 조회
    @GetMapping("{estimateId}")
    public ApiSuccess<?> getEstimateGroomer(@PathVariable Long estimateId) {
        EstimateResponseDto estimateResponseDtoList = estimateService.getEstimateGroomer(estimateId);
        return ApiUtil.success(estimateResponseDtoList);
    }

    // 견적서 수정 강아지별 상세 조회
    @GetMapping("{requestId}/{dogProfileId}")
    public ApiSuccess<?> getEstimateDogDetail(@PathVariable Long requestId, @PathVariable Long dogProfileId) {
        EstimateDogDetailResponseDto estimateDogDetailResponseDto = estimateService.getEstimateDogDetail(requestId,dogProfileId);
        return ApiUtil.success(estimateDogDetailResponseDto);
    }

    // 견적서 상세 조회
    @GetMapping("/detail/{estimateId}")
    public ApiSuccess<?> getEstimateDetail(@PathVariable Long estimateId) {
        MyEstimateDetailResponseDto myEstimateDetailResponseDto = estimateService.getEstimateDetail(estimateId);
        return ApiUtil.success(myEstimateDetailResponseDto);
    }

    // 내 견적 조회
    @GetMapping("/my/{requestId}")
    public ApiSuccess<?> getMyEstimate(@PathVariable Long requestId) {
        List<MyEstimateResponseDto> myEstimateResponseDtoList = estimateService.getMyEstimate(requestId);
        return ApiUtil.success(myEstimateResponseDtoList);
    }
}
