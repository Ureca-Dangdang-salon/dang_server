package com.dangdangsalon.domain.estimate.request.service;


import com.dangdangsalon.domain.estimate.request.dto.EstimateRequestDto;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.region.repository.DistrictRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EstimateRequestServices {

    private final EstimateRequestInsertService estimateRequestInsertService;
    private final GroomerEstimateRequestService groomerEstimateRequestService;
    private final DistrictRepository districtRepository;
    private final UserRepository userRepository;

    @Transactional
    public void insertEstimateRequest(EstimateRequestDto estimateRequestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 아이디를 찾을 수 없습니다: " + userId));

        District district = districtRepository.findByName(estimateRequestDto.getDistrict());
        EstimateRequest estimateRequest = estimateRequestInsertService.insertEstimateRequest(estimateRequestDto, user, district);

        groomerEstimateRequestService.insertGroomerEstimateRequests(estimateRequest, district, estimateRequestDto);
    }
}