package com.dangdangsalon.domain.estimate.request.service;


import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.dto.DogNameResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.EstimateRequestDto;
import com.dangdangsalon.domain.estimate.request.dto.MyEstimateRequestResponseDto;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.RequestStatus;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerProfileRepository;
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

    private final EstimateRequestInsertService estimateRequestInsertService;
    private final GroomerEstimateRequestService groomerEstimateRequestService;
    private final DistrictRepository districtRepository;
    private final EstimateRequestRepository estimateRequestRepository;
    private final UserRepository userRepository;
    private final GroomerProfileRepository groomerProfileRepository;
    private final EstimateRepository estimateRepository;

    // 견적 요청 등록
    @Transactional
    public void insertEstimateRequest(EstimateRequestDto estimateRequestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다: " + userId));

        District district = districtRepository.findById(estimateRequestDto.getDistrictId())
                .orElseThrow(() -> new IllegalArgumentException("해당 구를 찾을 수 없습니다: " +estimateRequestDto.getDistrictId()));

        EstimateRequest estimateRequest = estimateRequestInsertService.insertEstimateRequest(estimateRequestDto, user, district);

        if (estimateRequestDto.getGroomerProfileId() != null) {
            // groomerProfileId가 있는 경우 1대1 요청
            GroomerProfile groomerProfile = groomerProfileRepository.findById(estimateRequestDto.getGroomerProfileId())
                    .orElseThrow(() -> new IllegalArgumentException("미용사를 찾을 수 없습니다: " + estimateRequestDto.getGroomerProfileId()));

            groomerEstimateRequestService.insertGroomerEstimateRequestForSpecificGroomer(estimateRequest, groomerProfile, estimateRequestDto);

        } else {
            // groomerProfileId가 없는 경우
            groomerEstimateRequestService.insertGroomerEstimateRequests(estimateRequest, district, estimateRequestDto);
        }
    }

    // 내 견적 요청 조회 (채팅)
    @Transactional(readOnly = true)
    public List<MyEstimateRequestResponseDto> getMyEstimateRequest(Long userId) {
        // 내 견적 요청 다 가져오기
        List<EstimateRequest> estimateRequestList = estimateRequestRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원의 견적 요청을 찾을 수 없습니다: " + userId));

        return estimateRequestList.stream()
                .map(estimateRequest -> {
                    // 견적 요청에 연결된 강아지 정보 가져오기
                    List<DogNameResponseDto> dogList = estimateRequest.getEstimateRequestProfiles().stream()
                            .map(estimateRequestProfile -> {
                                DogProfile dogProfile = estimateRequestProfile.getDogProfile();
                                return DogNameResponseDto.builder()
                                        .dogName(dogProfile.getName())
                                        .build();
                            })
                            .toList();

                    // ACCEPTED 상태인 견적만 조회
                    Estimate estimate = estimateRepository.findByEstimateRequestIdAndStatus(
                            estimateRequest.getId(), EstimateStatus.ACCEPTED).orElse(null);

                    EstimateStatus estimateStatus = (estimate != null) ? estimate.getStatus() : null;

                    return MyEstimateRequestResponseDto.builder()
                            .dogList(dogList)
                            .requestId(estimateRequest.getId())
                            .date(estimateRequest.getRequestDate())
                            .status(estimateRequest.getRequestStatus())
                            .estimateStatus(estimateStatus)
                            .build();
                })
                .toList();
    }

    // 견적 그만 받기( 견적 요청을 CANCEL 로 바꾸면 미용사들이 견적서를 보내지 못한다. )
    @Transactional
    public void stopEstimate(Long requestId) {

        EstimateRequest estimateRequest = estimateRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청을 찾을 수 없습니다: " + requestId));

        estimateRequest.updateRequestStatus(RequestStatus.CANCEL);
    }
}