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

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class EstimateResponseDto {
    private Long estimateId;
    private String name;
    private LocalDate date;
    private String serviceType;
    private String region;
    private String imageKey;
    private String estimateRequestStatus;
    private String groomerEstimateRequestStatus;

    @Builder
    public EstimateResponseDto(Long estimateId ,String name, LocalDate date, String serviceType, String region, String imageKey, String estimateRequestStatus, String groomerEstimateRequestStatus) {
        this.estimateId = estimateId;
        this.name = name;
        this.date = date;
        this.serviceType = serviceType;
        this.region = region;
        this.imageKey = imageKey;
        this.estimateRequestStatus = estimateRequestStatus;
        this.groomerEstimateRequestStatus = groomerEstimateRequestStatus;
    }

    public static EstimateResponseDto toDto(GroomerEstimateRequest groomerEstimateRequest) {

        EstimateRequest estimateRequest = groomerEstimateRequest.getEstimateRequest();
        User user = estimateRequest.getUser();
        District district = estimateRequest.getDistrict();
        City city = district.getCity();

        String fullRegion = String.format("%s %s", city.getName(), district.getName());

        return EstimateResponseDto.builder()
                .estimateId(estimateRequest.getId())
                .name(user.getName())
                .date(estimateRequest.getRequestDate().toLocalDate())
                .serviceType(estimateRequest.getServiceType().name())
                .region(fullRegion)
                .imageKey(user.getImageKey())
                .estimateRequestStatus(estimateRequest.getRequestStatus().name())
                .groomerEstimateRequestStatus(String.valueOf(groomerEstimateRequest.getGroomerRequestStatus()))
                .build();
    }
}
