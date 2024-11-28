package com.dangdangsalon.domain.mypage.dto.res;

import com.dangdangsalon.domain.groomerprofile.entity.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
// 미용사 상세 조회 (모두 공개) (응답)
public class GroomerProfileDetailsResponseDto {
    private Long profileId;
    private String serviceName;
    private String imageKey;
    private String contact;
    private String businessNumber;
    private String contactHours;
    private ServiceType serviceType;
    private List<DistrictResponseDto> servicesDistricts;
    private long estimateRequestCount;
    private double starScore;
    private long reviewCount;
    private String address;
    private String experience;
    private List<String> certifications;
    private List<GroomerServicesResponseDto> servicesOffered;
    private String description;
    private String startMessage;
    private List<BadgeResponseDto> badges;
    private String faq;
}
