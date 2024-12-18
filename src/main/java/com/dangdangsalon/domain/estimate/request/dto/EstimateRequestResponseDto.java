package com.dangdangsalon.domain.estimate.request.dto;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.groomerprofile.request.entity.GroomerEstimateRequest;
import com.dangdangsalon.domain.region.entity.City;
import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class EstimateRequestResponseDto {
    private Long requestId;
    private String name;
    private LocalDateTime date;
    private String serviceType;
    private String region;
    private String imageKey;
    private String estimateStatus;
    private String groomerEstimateRequestStatus;

    @Builder
    public EstimateRequestResponseDto(Long requestId, String name, LocalDateTime date, String serviceType, String region, String imageKey, String estimateStatus, String groomerEstimateRequestStatus) {
        this.requestId = requestId;
        this.name = name;
        this.date = date;
        this.serviceType = serviceType;
        this.region = region;
        this.imageKey = imageKey;
        this.estimateStatus = estimateStatus;
        this.groomerEstimateRequestStatus = groomerEstimateRequestStatus;
    }

    public static EstimateRequestResponseDto toDto(GroomerEstimateRequest groomerEstimateRequest, Estimate estimate) {

        EstimateRequest estimateRequest = groomerEstimateRequest.getEstimateRequest();
        User user = estimateRequest.getUser();
        District district = estimateRequest.getDistrict();
        City city = district.getCity();

        String fullRegion = String.format("%s %s", city.getName(), district.getName());

        return EstimateRequestResponseDto.builder()
                .requestId(estimateRequest.getId())
                .name(user.getName())
                .date(estimateRequest.getRequestDate())
                .serviceType(estimateRequest.getServiceType().name())
                .region(fullRegion)
                .imageKey(user.getImageKey())
                .estimateStatus(estimate != null ? estimate.getStatus().name() : null)
                .groomerEstimateRequestStatus(String.valueOf(groomerEstimateRequest.getGroomerRequestStatus()))
                .build();
    }
}
