package com.dangdangsalon.domain.estimate.service;

import com.dangdangsalon.domain.estimate.dto.EstimateWriteRequestDto;
import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.entity.EstimateGroomerServicePrice;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.repository.EstimateGroomerServicePriceRepository;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerProfileRepository;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import com.dangdangsalon.domain.groomerservice.repository.GroomerServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class EstimateService {

    private final EstimateRequestRepository estimateRequestRepository;
    private final GroomerProfileRepository groomerProfileRepository;
    private final EstimateRepository estimateRepository;
    private final EstimateGroomerServicePriceRepository estimateGroomerServicePriceRepository;
    private final GroomerServiceRepository groomerServiceRepository;

    // 견적서 등록
    @Transactional
    public void insertEstimate(EstimateWriteRequestDto requestDto) {

        EstimateRequest estimateRequest = estimateRequestRepository.findById(requestDto.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("견적 요청을 찾을 수 없습니다 : " + requestDto.getRequestId()));

        GroomerProfile groomerProfile = groomerProfileRepository.findById(requestDto.getGroomerProfileId())
                .orElseThrow(() -> new IllegalArgumentException("그루머 프로필을 찾을 수 없습니다 : " + requestDto.getGroomerProfileId()));

        Estimate estimate = Estimate.builder()
                .aggressionCharge(requestDto.getAggressionCharge())
                .healthIssueCharge(requestDto.getHealthIssueCharge())
                .status(EstimateStatus.SEND)
                .description(requestDto.getDescription())
                .imageKey(requestDto.getImageKey())
                .groomerProfile(groomerProfile)
                .estimateRequest(estimateRequest)
                .totalAmount(requestDto.getTotalAmount())
                .date(requestDto.getDate())
                .build();

        estimateRepository.save(estimate);

        requestDto.getServiceList().forEach(serviceDto -> {
            GroomerService groomerService = groomerServiceRepository.findById(serviceDto.getServiceId())
                    .orElseThrow(() -> new IllegalArgumentException("그루머 서비스가 존재하지 않습니다 : " + serviceDto.getServiceId()));

            EstimateGroomerServicePrice servicePrice = EstimateGroomerServicePrice.builder()
                    .price(serviceDto.getPrice())
                    .estimate(estimate)
                    .groomerService(groomerService)
                    .build();

            estimateGroomerServicePriceRepository.save(servicePrice);
        });
    }

}
