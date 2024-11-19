package com.dangdangsalon.domain.estimate.request.service;

import com.dangdangsalon.domain.estimate.request.dto.EstimateRequestDto;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerCanService;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerServiceArea;
import com.dangdangsalon.domain.groomerprofile.entity.ServiceType;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerCanServiceRepository;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerServiceAreaRepository;
import com.dangdangsalon.domain.groomerprofile.request.entity.GroomerEstimateRequest;
import com.dangdangsalon.domain.groomerprofile.request.entity.GroomerRequestStatus;
import com.dangdangsalon.domain.groomerprofile.request.repository.GroomerEstimateRequestRepository;
import com.dangdangsalon.domain.region.entity.District;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// groomer 관련 서비스
@Service
@RequiredArgsConstructor
public class GroomerEstimateRequestService {

    private final GroomerServiceAreaRepository groomerServiceAreaRepository;
    private final GroomerCanServiceRepository groomerCanServiceRepository;
    private final GroomerEstimateRequestRepository groomerEstimateRequestRepository;

    @Transactional
    public void insertGroomerEstimateRequests(EstimateRequest estimateRequest, District district, EstimateRequestDto estimateRequestDto) {
        List<GroomerServiceArea> groomerServiceAreas = groomerServiceAreaRepository.findByDistrict(district);

        for (GroomerServiceArea groomerServiceArea : groomerServiceAreas) {
            GroomerProfile groomerProfile = groomerServiceArea.getGroomerProfile();

            if (canGroomerHandleRequest(estimateRequestDto, estimateRequest, groomerProfile)) {
                saveGroomerEstimateRequest(estimateRequest, groomerProfile);
            }
        }
    }

    // 두가지 조건이 만족해야 요청이 간다.
    private boolean canGroomerHandleRequest(EstimateRequestDto estimateRequestDto, EstimateRequest estimateRequest, GroomerProfile groomerProfile) {
        boolean canProvideAllServices = isGroomerAllServices(estimateRequestDto, groomerProfile);
        boolean matchesServiceType = matchesServiceType(estimateRequest, groomerProfile);

        return canProvideAllServices && matchesServiceType;
    }

    // 미용사가 가능한 서비스 안에 견적 신청한 서비스들이 모두 포함 되어야 견적 요청이 간다.
    private boolean isGroomerAllServices(EstimateRequestDto estimateRequestDto, GroomerProfile groomerProfile) {
        List<GroomerCanService> groomerCanServices = groomerCanServiceRepository.findByGroomerProfile(groomerProfile);

        return estimateRequestDto.getDogEstimateRequestList().stream()
                .flatMap(dogEstimateRequestDto -> dogEstimateRequestDto.getServicesOffered().stream())
                .allMatch(serviceId -> groomerCanServices.stream()
                        .anyMatch(canService -> canService.getGroomerService().getId().equals(serviceId)));
    }

    // 서비스 타입이 일치해야만 미용이 가능
    private boolean matchesServiceType(EstimateRequest estimateRequest, GroomerProfile groomerProfile) {
        return groomerProfile.getServiceType() == ServiceType.ANY ||
                groomerProfile.getServiceType() == estimateRequest.getServiceType();
    }

    // 저장
    private void saveGroomerEstimateRequest(EstimateRequest estimateRequest, GroomerProfile groomerProfile) {
        GroomerEstimateRequest groomerEstimateRequest = GroomerEstimateRequest.builder()
                .groomerRequestStatus(GroomerRequestStatus.PENDING)
                .estimateRequest(estimateRequest)
                .groomerProfile(groomerProfile)
                .build();

        groomerEstimateRequestRepository.save(groomerEstimateRequest);
    }
}
