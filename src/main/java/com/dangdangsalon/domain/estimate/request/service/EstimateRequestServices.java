package com.dangdangsalon.domain.estimate.request.service;


import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.dogprofile.repository.DogProfileRepository;
import com.dangdangsalon.domain.estimate.request.dto.EstimateRequestDto;
import com.dangdangsalon.domain.estimate.request.dto.DogRequestDto;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import com.dangdangsalon.domain.estimate.request.entity.RequestStatus;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestProfilesRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestServiceRepository;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerCanService;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerServiceArea;
import com.dangdangsalon.domain.groomerprofile.entity.ServiceType;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerCanServiceRepository;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerServiceAreaRepository;
import com.dangdangsalon.domain.groomerprofile.request.entity.GroomerEstimateRequest;
import com.dangdangsalon.domain.groomerprofile.request.entity.GroomerRequestStatus;
import com.dangdangsalon.domain.groomerprofile.request.repository.GroomerEstimateRequestRepository;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import com.dangdangsalon.domain.groomerservice.repository.GroomerServiceRepository;
import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.region.repository.DistrictRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstimateRequestServices {

    private final EstimateRequestRepository estimateRequestRepository;
    private final EstimateRequestProfilesRepository estimateRequestProfilesRepository;
    private final EstimateRequestServiceRepository estimateRequestServiceRepository;
    private final DogProfileRepository dogProfileRepository;
    private final GroomerServiceRepository groomerServiceRepository;
    private final DistrictRepository districtRepository;
    private final UserRepository userRepository;
    private final GroomerEstimateRequestRepository groomerEstimateRequestRepository;
    private final GroomerServiceAreaRepository groomerServiceAreaRepository;
    private final GroomerCanServiceRepository groomerCanServiceRepository;

    @Transactional
    public void insertEstimateRequest(EstimateRequestDto estimateRequestDto, Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("유저 아이디를 찾을 수 없습니다 : " + userId));

        // 일단 지역 구만 받기??
        District district = districtRepository.findByName(estimateRequestDto.getDistrict());

        // 견적 요청 저장
        EstimateRequest estimateRequest = EstimateRequest.builder()
                .requestDate(estimateRequestDto.getDate())
                .requestStatus(RequestStatus.PENDING)
                .serviceType(ServiceType.valueOf(estimateRequestDto.getServiceType()))
                .user(user)
                .district(district)
                .build();

        estimateRequestRepository.save(estimateRequest);

        // 강아지 프로필 별로 요청 저장
        for (DogRequestDto dogDto : estimateRequestDto.getDogs()) {
            DogProfile dogProfile = dogProfileRepository.findById(dogDto.getDogProfileId()).orElseThrow(() ->
                    new IllegalArgumentException("강아지 프로필을 찾을 수 없습니다 : " + dogDto.getDogProfileId()));

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

            for (Long serviceId : dogDto.getServicesOffered()) {
                GroomerService groomerService = groomerServiceRepository.findById(serviceId).orElseThrow(() ->
                        new IllegalArgumentException("서비스를 찾을 수 없습니다 : " + serviceId));

                EstimateRequestService estimateRequestService = EstimateRequestService.builder()
                        .estimateRequestProfiles(estimateRequestProfiles)
                        .groomerService(groomerService)
                        .build();

                estimateRequestServiceRepository.save(estimateRequestService);
            }
        }

        // 미용사가 회원가입할 때 선택한 서비스 지역(구)과 요청 지역(구)이 같으면 해당 미용사한테 견적 요청이 간다.
        List<GroomerServiceArea> groomerServiceAreas = groomerServiceAreaRepository.findByDistrict(district);

        for (GroomerServiceArea groomerServiceArea : groomerServiceAreas) {
            GroomerProfile groomerProfile = groomerServiceArea.getGroomerProfile();

            // 미용사가 회원가입할 때 가능하다고 한 서비스 목록
            List<GroomerCanService> groomerCanServices = groomerCanServiceRepository.findByGroomerProfile(groomerProfile);

            // 미용사가 가능한 서비스 안에 견적 요청한 서비스들이 다 있나 확인 작업
            boolean canProvideAllServices = estimateRequestDto.getDogs().stream()
                    .flatMap(dogRequestDto -> dogRequestDto.getServicesOffered().stream())
                    .allMatch(serviceId -> groomerCanServices.stream()
                            .anyMatch(canService -> canService.getGroomerService().getId().equals(serviceId)));

            // 서비스 타입 및 서비스 제공 가능 여부 확인
            if (canProvideAllServices && (groomerProfile.getServiceType() == ServiceType.ANY ||
                            groomerProfile.getServiceType() == estimateRequest.getServiceType())){

                GroomerEstimateRequest groomerEstimateRequest = GroomerEstimateRequest.builder()
                        .groomerRequestStatus(GroomerRequestStatus.PENDING)
                        .estimateRequest(estimateRequest)
                        .groomerProfile(groomerProfile)
                        .build();

                groomerEstimateRequestRepository.save(groomerEstimateRequest);
            }
        }
    }
}