package com.dangdangsalon.domain.estimate.service;

import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.dogprofile.entity.DogProfileFeature;
import com.dangdangsalon.domain.dogprofile.repository.DogProfileFeatureRepository;
import com.dangdangsalon.domain.estimate.dto.EstimateWriteDetailResponseDto;
import com.dangdangsalon.domain.estimate.dto.EstimateWriteResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.FeatureResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.ServiceResponseDto;
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
public class EstimateWriteService {

    private final EstimateRequestRepository estimateRequestRepository;
    private final EstimateRequestProfilesRepository estimateRequestProfilesRepository;
    private final EstimateRequestServiceRepository estimateRequestServiceRepository;
    private final DogProfileFeatureRepository dogProfileFeatureRepository;

    // 견적서 작성 반려견 요청 목록 조회
    @Transactional(readOnly = true)
    public List<EstimateWriteResponseDto> getEstimateRequestDog(Long requestId) {

        EstimateRequest estimateRequest = estimateRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청을 찾을 수 없습니다 : " + requestId));

        // 해당 요청의 프로필 리스트 가져오기
        List<EstimateRequestProfiles> profileList = estimateRequestProfilesRepository.findByEstimateRequest(estimateRequest)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청 프로필 정보를 찾을 수 없습니다"));

        return profileList.stream()
                .map(profile -> EstimateWriteResponseDto.builder()
                        .dogProfileResponseDto(new DogProfileResponseDto(
                                profile.getDogProfile().getId(),
                                profile.getDogProfile().getImageKey(),
                                profile.getDogProfile().getName()))
                        .description(profile.getDescription())
                        .isAggression(profile.isAggression())
                        .isHealthIssue(profile.isHealthIssue())
                        .serviceList(getServiceList(profile))
                        .build())
                .toList();

    }

    // 견적서 작성 반려견 요청 상세 보기
    @Transactional(readOnly = true)
    public EstimateWriteDetailResponseDto getEstimateRequestDogDetail(Long requestId, Long dogProfileId) {

        EstimateRequest estimateRequest = estimateRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청을 찾을 수 없습니다 : " + requestId));

        // 강아지 아이디로 특정 강아지 프로필 조회
        EstimateRequestProfiles profile = estimateRequestProfilesRepository.findByEstimateRequestAndDogProfileId(estimateRequest, dogProfileId)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청에 해당하는 강아지 프로필을 찾을 수 없습니다."));

        DogProfile dogProfile = profile.getDogProfile();

        List<ServiceResponseDto> serviceList = getServiceList(profile);

        List<FeatureResponseDto> featureList = getFeatureList(dogProfile);

        return EstimateWriteDetailResponseDto.toDto(dogProfile, profile, serviceList, featureList);
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
