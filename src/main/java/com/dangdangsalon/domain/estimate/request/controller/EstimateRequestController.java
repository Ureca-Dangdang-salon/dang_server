package com.dangdangsalon.domain.estimate.request.controller;

import com.dangdangsalon.domain.estimate.request.dto.EstimateRequestDto;
import com.dangdangsalon.domain.estimate.request.service.EstimateRequestServices;
import com.dangdangsalon.util.ApiUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EstimateRequestController {

    private final EstimateRequestServices estimateRequestServices;

    /**
     * userId 토큰에서 가져오는 걸로 추후 변경
     */
    @PostMapping("/estimaterequest")
    public ApiUtil.ApiSuccess<String> createEstimateRequest(@RequestBody EstimateRequestDto estimateRequestDto, @RequestParam Long userId) {
        estimateRequestServices.insertEstimateRequest(estimateRequestDto, userId);
        return ApiUtil.success("견적 요청 등록에 성공하였습니다.");
    }
}