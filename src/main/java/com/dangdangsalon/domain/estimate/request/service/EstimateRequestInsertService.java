package com.dangdangsalon.domain.estimate.request.service;

import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.dogprofile.repository.DogProfileRepository;
import com.dangdangsalon.domain.estimate.request.dto.DogEstimateRequestDto;
import com.dangdangsalon.domain.estimate.request.dto.EstimateRequestDto;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import com.dangdangsalon.domain.estimate.request.entity.RequestStatus;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestProfilesRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestServiceRepository;
import com.dangdangsalon.domain.groomerprofile.entity.ServiceType;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import com.dangdangsalon.domain.groomerservice.repository.GroomerServiceRepository;
import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// EstimateRequest 관련 서비스
@Service
@RequiredArgsConstructor
public class EstimateRequestInsertService {

    private final EstimateRequestRepository estimateRequestRepository;
    private final EstimateRequestProfilesRepository estimateRequestProfilesRepository;
    private final EstimateRequestServiceRepository estimateRequestServiceRepository;
    private final DogProfileRepository dogProfileRepository;
    private final GroomerServiceRepository groomerServiceRepository;

    @Transactional
    public EstimateRequest insertEstimateRequest(EstimateRequestDto estimateRequestDto, User user, District district) {
        EstimateRequest estimateRequest = EstimateRequest.builder()
                .requestDate(estimateRequestDto.getDate())
                .requestStatus(RequestStatus.COMPLETED)
                .serviceType(ServiceType.valueOf(estimateRequestDto.getServiceType()))
                .user(user)
                .district(district)
                .build();

        estimateRequestRepository.save(estimateRequest);
        insertEstimateRequestProfiles(estimateRequestDto, estimateRequest);

        return estimateRequest;
    }

    // 강아지별 견적 요청 강아지 정보 저장
    private void insertEstimateRequestProfiles(EstimateRequestDto estimateRequestDto, EstimateRequest estimateRequest) {
        for (DogEstimateRequestDto dogDto : estimateRequestDto.getDogEstimateRequestList()) {
            DogProfile dogProfile = dogProfileRepository.findById(dogDto.getDogProfileId())
                    .orElseThrow(() -> new IllegalArgumentException("강아지 프로필을 찾을 수 없습니다: " + dogDto.getDogProfileId()));

            EstimateRequestProfiles estimateRequestProfiles = EstimateRequestProfiles.builder()
                    .estimateRequest(estimateRequest)
                    .dogProfile(dogProfile)
                    .currentImageKey(dogDto.getCurrentImageKey())
                    .styleRefImageKey(dogDto.getStyleRefImageKey())
                    .aggression(dogDto.isAggression())
                    .healthIssue(dogDto.isHealthIssue())
                    .description(dogDto.getDescription())
                    .build();

            estimateRequestProfilesRepository.save(estimateRequestProfiles);
            insertEstimateRequestServices(dogDto, estimateRequestProfiles);
        }
    }

    // 강아지별 견적 요청 서비스 저장
    private void insertEstimateRequestServices(DogEstimateRequestDto dogDto, EstimateRequestProfiles estimateRequestProfiles) {
        for (Long serviceId : dogDto.getServicesOffered()) {
            GroomerService groomerService = groomerServiceRepository.findById(serviceId)
                    .orElseThrow(() -> new IllegalArgumentException("서비스를 찾을 수 없습니다: " + serviceId));

            EstimateRequestService estimateRequestService = EstimateRequestService.builder()
                    .estimateRequestProfiles(estimateRequestProfiles)
                    .groomerService(groomerService)
                    .build();

            estimateRequestServiceRepository.save(estimateRequestService);
        }
    }
}
