package com.dangdangsalon.domain.estimate.service;

import com.dangdangsalon.domain.estimate.dto.EstimateWriteRequestDto;
import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestProfilesRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestServiceRepository;
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
    private final GroomerServiceRepository groomerServiceRepository;
    private final EstimateRequestProfilesRepository estimateRequestProfilesRepository;
    private final EstimateRequestServiceRepository estimateRequestServiceRepository;

    // 견적서 등록
    @Transactional
    public void insertEstimate(EstimateWriteRequestDto requestDto) {

        EstimateRequest estimateRequest = estimateRequestRepository.findById(requestDto.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("견적 요청을 찾을 수 없습니다 : " + requestDto.getRequestId()));

        GroomerProfile groomerProfile = groomerProfileRepository.findById(requestDto.getGroomerProfileId())
                .orElseThrow(() -> new IllegalArgumentException("미용사 프로필을 찾을 수 없습니다 : " + requestDto.getGroomerProfileId()));

        Estimate estimate = Estimate.builder()
                .status(EstimateStatus.SEND)
                .description(requestDto.getDescription())
                .imageKey(requestDto.getImageKey())
                .groomerProfile(groomerProfile)
                .estimateRequest(estimateRequest)
                .totalAmount(requestDto.getTotalAmount())
                .date(requestDto.getDate())
                .build();

        estimateRepository.save(estimate);

        // 강아지별 특이사항, 서비스 다 따로 저장하기는 로직....
        requestDto.getDogPriceList().forEach(dogPriceDto -> {
            EstimateRequestProfiles estimateRequestProfiles = estimateRequestProfilesRepository.findByDogProfileIdAndEstimateRequestId(
                    dogPriceDto.getDogProfileId(),
                    requestDto.getRequestId()
            ).orElseThrow(() -> new IllegalArgumentException("조건에 맞는 견적 프로필을 찾을 수 없습니다."));

            estimateRequestProfiles.updateCharges(
                    dogPriceDto.getAggressionCharge(),
                    dogPriceDto.getHealthIssueCharge()
            );

            estimateRequestProfilesRepository.save(estimateRequestProfiles);

            dogPriceDto.getServiceList().forEach(serviceDto -> {
                // serviceId를 사용해 GroomerService 찾기
                GroomerService groomerService = groomerServiceRepository.findById(serviceDto.getServiceId())
                        .orElseThrow(() -> new IllegalArgumentException("미용사 서비스가 존재하지 않습니다 : " + serviceDto.getServiceId()));

                EstimateRequestService estimateRequestService = estimateRequestServiceRepository.findByEstimateRequestProfilesAndGroomerService(estimateRequestProfiles, groomerService)
                        .orElseThrow(() -> new IllegalArgumentException("해당 프로필과 서비스에 대한 견적 요청 서비스가 존재하지 않습니다: " + "프로필 ID = " + estimateRequestProfiles.getId() + ", 서비스 ID = " + groomerService.getId()));

                estimateRequestService.updatePrice(serviceDto.getPrice());

                estimateRequestServiceRepository.save(estimateRequestService);
            });
        });
    }
}
