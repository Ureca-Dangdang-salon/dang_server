package com.dangdangsalon.domain.estimate.request.service;

import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.dogprofile.entity.DogProfileFeature;
import com.dangdangsalon.domain.dogprofile.repository.DogProfileFeatureRepository;
import com.dangdangsalon.domain.estimate.request.dto.*;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestProfilesRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstimateRequestDetailService {

    private final EstimateRequestRepository estimateRequestRepository;
    private final EstimateRequestProfilesRepository estimateRequestProfilesRepository;
    private final EstimateRequestServiceRepository estimateRequestServiceRepository;
    private final DogProfileFeatureRepository dogProfileFeatureRepository;

    @Transactional(readOnly = true)
    public List<EstimateDetailResponseDto> getEstimateRequestDetail(Long requestId) {

        // 견적 요청 조회(미용사) 할 때 견적 요청 아이디 넘겨줬으니까 클릭하면 요청 아이디를 받는다.
        EstimateRequest estimateRequest = estimateRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청을 찾을 수 없습니다 : " + requestId));

        // 해당 요청의 프로필 리스트 가져오기
        List<EstimateRequestProfiles> profileList = estimateRequestProfilesRepository.findByEstimateRequest(estimateRequest)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청 프로필 정보를 찾을 수 없습니다"));

        return profileList.stream()
                .map(profile -> EstimateDetailResponseDto.toDto(
                        profile,
                        new DogProfileResponseDto(
                                profile.getDogProfile().getId(),
                                profile.getDogProfile().getImageKey(),
                                profile.getDogProfile().getName()),
                        getServiceList(profile),
                        getFeatureList(profile.getDogProfile())))
                .toList();
    }

    // 내 견적 요청 상세 조회 (채팅)
    @Transactional(readOnly = true)
    public List<MyEstimateRequestDetailResponseDto> getMyEstimateDetailRequest(Long requestId) {

        // 견적 요청 조회(미용사) 할 때 견적 요청 아이디 넘겨줬으니까 클릭하면 요청 아이디를 받는다.
        EstimateRequest estimateRequest = estimateRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청을 찾을 수 없습니다 : " + requestId));

        // 해당 요청의 프로필 리스트 가져오기
        List<EstimateRequestProfiles> profileList = estimateRequestProfilesRepository.findByEstimateRequest(estimateRequest)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청 프로필 정보를 찾을 수 없습니다"));


        return profileList.stream()
                .map(profile -> MyEstimateRequestDetailResponseDto.builder()
                        .imageKey(profile.getDogProfile().getImageKey())
                        .dogName(profile.getDogProfile().getName())
                        .aggression(profile.isAggression())
                        .healthIssue(profile.isHealthIssue())
                        .description(profile.getDescription())
                        .serviceList(getServiceList(profile))
                        .build())
                .toList();
    }

    // 견적 요청한 서비스 정보 가져오기
    private List<ServiceResponseDto> getServiceList(EstimateRequestProfiles profile) {

        List<EstimateRequestService> serviceList = estimateRequestServiceRepository.findByEstimateRequestProfiles(profile)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청 서비스가 없습니다 : " + profile.getId()));

        return serviceList.stream()
                .map(service -> new ServiceResponseDto(
                        service.getGroomerService().getId(),
                        service.getGroomerService().getDescription()))
                .toList();
    }

    // 강아지 프로필에 연결된 특징 가져오기
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
