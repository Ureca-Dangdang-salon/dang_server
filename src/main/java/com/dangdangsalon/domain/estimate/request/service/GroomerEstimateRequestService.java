package com.dangdangsalon.domain.estimate.request.service;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.dto.EstimateRequestDto;
import com.dangdangsalon.domain.estimate.request.dto.EstimateRequestResponseDto;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.RequestStatus;
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
    private final GroomerEstimateRequestNotificationService groomerEstimateRequestNotificationService;
    private final EstimateRepository estimateRepository;

    @Transactional
    public void insertGroomerEstimateRequests(EstimateRequest estimateRequest, District district, EstimateRequestDto estimateRequestDto) {
        List<GroomerServiceArea> groomerServiceAreaList = groomerServiceAreaRepository.findByDistrict(district)
                .orElseThrow(() -> new IllegalArgumentException("미용사"));

        for (GroomerServiceArea groomerServiceArea : groomerServiceAreaList) {
            GroomerProfile groomerProfile = groomerServiceArea.getGroomerProfile();

            if (canGroomerHandleRequest(estimateRequestDto, estimateRequest, groomerProfile)) {
                saveGroomerEstimateRequest(estimateRequest, groomerProfile);
            }
        }
    }

    // 1대1요청 지역은 달라도 미용사가 가능한 서비스 타입이랑 할 수 있는 서비스는 일치해야 요청이 간다.
    @Transactional
    public void insertGroomerEstimateRequestForSpecificGroomer(EstimateRequest estimateRequest, GroomerProfile groomerProfile, EstimateRequestDto estimateRequestDto) {
        if (canGroomerHandleRequest(estimateRequestDto, estimateRequest, groomerProfile)) {
            saveGroomerEstimateRequest(estimateRequest, groomerProfile);
        } else {
            throw new IllegalArgumentException("미용사가 제공하는 서비스 타입과 필요한 서비스를 모두 제공할 수 없습니다.");
        }
    }

    // 미용사에게 온 견적 요청들 조회
    @Transactional(readOnly = true)
    public List<EstimateRequestResponseDto> getEstimateRequest(Long groomerProfileId) {

        List<GroomerEstimateRequest> groomerEstimateRequestList = groomerEstimateRequestRepository.findByGroomerProfileId(groomerProfileId)
                .orElseThrow(() -> new IllegalArgumentException("해당 요청에 대한 미용사 정보를 찾을 수 없습니다"));

        return groomerEstimateRequestList.stream()
                .filter(groomerEstimateRequest -> !groomerEstimateRequest.getEstimateRequest().getRequestStatus().equals(RequestStatus.CANCEL))
                .map(groomerEstimateRequest -> {
                    Estimate estimate = estimateRepository.findByEstimateRequestId(groomerEstimateRequest.getEstimateRequest().getId())
                            .orElse(null);
                    return EstimateRequestResponseDto.toDto(groomerEstimateRequest, estimate);
                })
                .toList();
    }

    /**
     *  견적 요청 삭제(미용사)
     */
    @Transactional
    public void deleteGroomerEstimateRequest(Long estimateRequestId, Long groomerProfileId) {

        GroomerEstimateRequest request = groomerEstimateRequestRepository.findByEstimateRequestIdAndGroomerProfileId(estimateRequestId, groomerProfileId)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청을 찾을 수 없습니다: " + estimateRequestId));

        groomerEstimateRequestRepository.delete(request);
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
                .groomerRequestStatus(GroomerRequestStatus.COMPLETED)
                .estimateRequest(estimateRequest)
                .groomerProfile(groomerProfile)
                .build();

        groomerEstimateRequestRepository.save(groomerEstimateRequest);
        groomerEstimateRequestNotificationService.sendNotificationToGroomer(estimateRequest, groomerProfile);
    }
}
