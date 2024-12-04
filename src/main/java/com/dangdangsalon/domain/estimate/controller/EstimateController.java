package com.dangdangsalon.domain.estimate.controller;

import com.dangdangsalon.domain.estimate.dto.*;
import com.dangdangsalon.domain.estimate.service.EstimateNotificationService;
import com.dangdangsalon.domain.estimate.service.EstimateService;
import com.dangdangsalon.domain.estimate.service.EstimateUpdateService;
import com.dangdangsalon.domain.estimate.service.EstimateWriteService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/estimate")
public class EstimateController {

    private final EstimateWriteService estimateWriteService;
    private final EstimateService estimateService;
    private final EstimateNotificationService estimateNotificationService;
    private final EstimateUpdateService estimateUpdateService;

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
        EstimateIdResponseDto estimateIdResponseDto = estimateService.insertEstimate(requestDto);
        return ApiUtil.success(estimateIdResponseDto);
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

    // 미용 완료 버튼 클릭시 견적서 상태 변화
    @PutMapping("/{estimateId}")
    public ApiSuccess<?> updateEstimateStatus(@PathVariable Long estimateId) {
        estimateNotificationService.updateEstimateStatus(estimateId);
        return ApiUtil.success("견적서 상태 업데이트 완료");
    }

    @PutMapping("/update")
    public ApiSuccess<?> updateEstimate(@RequestBody EstimateUpdateRequestDto requestDto) {
        estimateUpdateService.updateEstimate(requestDto);
        return ApiUtil.success("견적서 업데이트를 성공하였습니다.");
    }

    @PutMapping("/rejected/{estimateId}")
    public ApiSuccess<?> updateEstimate(@PathVariable Long estimateId) {
        estimateUpdateService.rejectedEstimate(estimateId);
        return ApiUtil.success("견적서 거절에 성공하였습니다.");
    }
}
