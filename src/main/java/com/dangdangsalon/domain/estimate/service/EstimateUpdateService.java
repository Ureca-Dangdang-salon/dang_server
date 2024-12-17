package com.dangdangsalon.domain.estimate.service;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.service.ChatMessageService;
import com.dangdangsalon.domain.chat.service.ChatRoomService;
import com.dangdangsalon.domain.estimate.dto.EstimateIdResponseDto;
import com.dangdangsalon.domain.estimate.dto.EstimateUpdateRequestDto;
import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestProfilesRepository;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestServiceRepository;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import com.dangdangsalon.domain.groomerservice.repository.GroomerServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EstimateUpdateService {

    private final EstimateRepository estimateRepository;
    private final GroomerServiceRepository groomerServiceRepository;
    private final EstimateRequestProfilesRepository estimateRequestProfilesRepository;
    private final EstimateRequestServiceRepository estimateRequestServiceRepository;

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;

    @Transactional
    public void updateEstimate(EstimateUpdateRequestDto requestDto) {

        // 기존 견적서 조회
        Estimate estimate = estimateRepository.findById(requestDto.getEstimateId())
                .orElseThrow(() -> new IllegalArgumentException("견적서를 찾을 수 없습니다: " + requestDto.getEstimateId()));

        // 강아지 정보 수정 (Optional)
        if (requestDto.getDogPriceList() != null) {
            requestDto.getDogPriceList().forEach(dogPriceDto -> {
                EstimateRequestProfiles estimateRequestProfiles = estimateRequestProfilesRepository
                        .findByDogProfileIdAndEstimateRequestId(
                                dogPriceDto.getDogProfileId(),
                                estimate.getEstimateRequest().getId()
                        )
                        .orElseThrow(() -> new IllegalArgumentException("조건에 맞는 견적 프로필을 찾을 수 없습니다."));

                estimateRequestProfiles.updateCharges(
                        dogPriceDto.getAggressionCharge(),
                        dogPriceDto.getHealthIssueCharge()
                );

                dogPriceDto.getServiceList().forEach(serviceDto -> {
                    GroomerService groomerService = groomerServiceRepository.findById(serviceDto.getServiceId())
                            .orElseThrow(() -> new IllegalArgumentException("미용사 서비스가 존재하지 않습니다 : " + serviceDto.getServiceId()));

                    EstimateRequestService estimateRequestService = estimateRequestServiceRepository.findByEstimateRequestProfilesAndGroomerService(estimateRequestProfiles, groomerService)
                            .orElseThrow(() -> new IllegalArgumentException("해당 프로필과 서비스에 대한 견적 요청 서비스가 존재하지 않습니다: " + "프로필 ID = " + estimateRequestProfiles.getId() + ", 서비스 ID = " + groomerService.getId()));

                    estimateRequestService.updatePrice(serviceDto.getPrice());
                });
            });
        }

        estimate.updateEstimate(requestDto.getDescription(), requestDto.getImageKey(), requestDto.getTotalAmount(), requestDto.getDate());

//        ChatMessageDto updateEstimateMessage = chatRoomService.createUpdateEstimateMessage(estimate);
//        chatMessageService.saveMessageRedis(updateEstimateMessage);
    }

    @Transactional
    public void rejectedEstimate(Long estimateId) {
        Estimate estimate = estimateRepository.findById(estimateId)
                .orElseThrow(() -> new IllegalArgumentException("견적서를 찾을 수 없습니다: " + estimateId));

        estimate.updateStatus(EstimateStatus.REJECTED);
    }
}
