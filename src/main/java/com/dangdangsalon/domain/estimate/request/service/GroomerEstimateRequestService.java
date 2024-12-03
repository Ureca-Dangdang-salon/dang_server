package com.dangdangsalon.domain.estimate.request.service;

import com.dangdangsalon.domain.estimate.request.dto.EstimateRequestDto;
import com.dangdangsalon.domain.estimate.request.dto.EstimateRequestResponseDto;
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
import com.dangdangsalon.domain.notification.service.NotificationService;
import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// groomer 관련 서비스
@Slf4j
@Service
@RequiredArgsConstructor
public class GroomerEstimateRequestService {

    private final GroomerServiceAreaRepository groomerServiceAreaRepository;
    private final GroomerCanServiceRepository groomerCanServiceRepository;
    private final GroomerEstimateRequestRepository groomerEstimateRequestRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

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
                .map(EstimateRequestResponseDto::toDto)
                .toList();
    }

    /**
     *  견적 요청 삭제(미용사)
     */
    @Transactional
    public void deleteGroomerEstimateRequest(Long estimateRequestId) {

        GroomerEstimateRequest request = groomerEstimateRequestRepository.findByEstimateRequestId(estimateRequestId)
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
        sendNotificationToGroomer(estimateRequest, groomerProfile);
    }

    private void sendNotificationToGroomer(EstimateRequest estimateRequest, GroomerProfile groomerProfile) {

        Long userId = groomerProfile.getUser().getId();

        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId)
        );

        if (Boolean.FALSE.equals(user.getNotificationEnabled())) {
            log.info("알림 비활성화: " + user.getId());
        }else{
            Optional<String> optionalFcmToken = notificationService.getFcmToken(userId);

            if (optionalFcmToken.isPresent()) {
                String fcmToken = optionalFcmToken.get();

                String title = "새로운 견적 요청";
                String body = "새로운 견적 요청이 도착했습니다. 확인하세요.";
                // 알림 전송
                notificationService.sendNotificationWithData(fcmToken, title, body, "견적 요청", estimateRequest.getId());
                // redis 에 알림 내용 저장
                notificationService.saveNotificationToRedis(userId, title, body, "견적 요청", estimateRequest.getId());
            }
        }
    }
}
