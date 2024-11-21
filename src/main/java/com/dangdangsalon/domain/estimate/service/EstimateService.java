package com.dangdangsalon.domain.estimate.service;

import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.dogprofile.entity.DogProfileFeature;
import com.dangdangsalon.domain.dogprofile.repository.DogProfileFeatureRepository;
import com.dangdangsalon.domain.estimate.dto.*;
import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.dto.FeatureResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.ServicePriceResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.ServiceResponseDto;
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

import java.util.List;


@Service
@RequiredArgsConstructor
public class EstimateService {

    private final EstimateRequestRepository estimateRequestRepository;
    private final GroomerProfileRepository groomerProfileRepository;
    private final EstimateRepository estimateRepository;
    private final GroomerServiceRepository groomerServiceRepository;
    private final EstimateRequestProfilesRepository estimateRequestProfilesRepository;
    private final EstimateRequestServiceRepository estimateRequestServiceRepository;
    private final DogProfileFeatureRepository dogProfileFeatureRepository;

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

    // 견적서 수정 조회
    @Transactional(readOnly = true)
    public EstimateResponseDto getEstimateGroomer(Long estimateId) {
        // Estimate 조회
        Estimate estimate = estimateRepository.findById(estimateId)
                .orElseThrow(() -> new IllegalArgumentException("견적서를 찾을 수 없습니다: " + estimateId));

        List<EstimateRequestProfiles> profiles = estimateRequestProfilesRepository.findByEstimateRequestId(estimate.getEstimateRequest().getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 견적서에 대한 견적 요청을 찾을 수 없습니다: " + estimate.getEstimateRequest().getId() ));

        List<EstimateDogResponseDto> estimateDogResponseDtoList = profiles.stream()
                .map(profile -> {
                    DogProfileResponseDto dogProfileResponseDto = new DogProfileResponseDto(
                            profile.getDogProfile().getId(),
                            profile.getDogProfile().getImageKey(),
                            profile.getDogProfile().getName()
                    );

                    List<ServicePriceResponseDto> serviceList = getServiceList(profile);

                    int totalServicePrice = serviceList.stream()
                            .mapToInt(ServicePriceResponseDto::getPrice)
                            .sum();

                    return EstimateDogResponseDto.builder()
                            .dogProfileResponseDto(dogProfileResponseDto)
                            .description(profile.getDescription())
                            .serviceList(serviceList)
                            .isAggression(profile.isAggression())
                            .isHealthIssue(profile.isHealthIssue())
                            .dogPrice(totalServicePrice)
                            .build();
                })
                .toList();

        return EstimateResponseDto.builder()
                .comment(estimate.getDescription())
                .totalAmount(estimate.getTotalAmount())
                .date(estimate.getDate())
                .estimateList(estimateDogResponseDtoList)
                .build();
    }

    // 견적서 수정 반려견 요청 상세 보기
    @Transactional(readOnly = true)
    public EstimateDogDetailResponseDto getEstimateDogDetail(Long requestId, Long dogProfileId) {

        EstimateRequest estimateRequest = estimateRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청을 찾을 수 없습니다 : " + requestId));

        // 강아지 아이디로 특정 강아지 프로필 조회
        EstimateRequestProfiles profile = estimateRequestProfilesRepository.findByEstimateRequestAndDogProfileId(estimateRequest, dogProfileId)
                .orElseThrow(() -> new IllegalArgumentException("해당 강아지 프로필 정보를 찾을 수 없습니다 : " + dogProfileId));

        DogProfile dogProfile = profile.getDogProfile();

        List<ServicePriceResponseDto> serviceList = getServiceList(profile);

        List<FeatureResponseDto> featureList = getFeatureList(dogProfile);

        return EstimateDogDetailResponseDto.toDto(dogProfile, profile, serviceList, featureList);
    }

    // 견적 요청한 서비스 정보 가져오기
    private List<ServicePriceResponseDto> getServiceList(EstimateRequestProfiles profile) {

        List<EstimateRequestService> serviceList = estimateRequestServiceRepository.findByEstimateRequestProfiles(profile)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청 서비스가 없습니다 : " + profile.getId()));

        return serviceList.stream()
                .map(service -> new ServicePriceResponseDto(
                        service.getGroomerService().getId(),
                        service.getGroomerService().getDescription(),
                        service.getPrice()))
                .toList();
    }

    // 회원가입 할 때 입력한 강아지 특징
    private List<FeatureResponseDto> getFeatureList(DogProfile dogProfile) {

        // 강아지 특징이 없을 수도 있다??
        List<DogProfileFeature> featureList = dogProfileFeatureRepository.findByDogProfile(dogProfile)
                .orElse(List.of());

        return featureList.stream()
                .map(feature -> new FeatureResponseDto(
                        feature.getFeature().getDescription()))
                .toList();
    }
}
