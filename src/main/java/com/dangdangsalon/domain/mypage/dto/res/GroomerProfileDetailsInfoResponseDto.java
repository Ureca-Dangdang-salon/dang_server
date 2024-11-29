package com.dangdangsalon.domain.mypage.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class GroomerProfileDetailsInfoResponseDto {
    private double starScore;
    private long estimateRequestCount;
    private long reviewCount;
    private List<BadgeResponseDto> badges;
    private List<GroomerServicesResponseDto> servicesOffered;
    private List<DistrictResponseDto> servicesDistricts;
    private List<String> certifications;

    public static GroomerProfileDetailsInfoResponseDto create(
            double totalScore,
            long reviewCount,
            long estimateRequestCount,
            List<BadgeResponseDto> badges,
            List<GroomerServicesResponseDto> servicesOffered,
            List<DistrictResponseDto> serviceDistricts,
            List<String> certifications) {

        return GroomerProfileDetailsInfoResponseDto.builder()
                .starScore(totalScore)
                .estimateRequestCount(estimateRequestCount)
                .reviewCount(reviewCount)
                .servicesDistricts(serviceDistricts) // 서비스 지역
                .certifications(certifications) // 자격증
                .servicesOffered(servicesOffered) // 서비스 제공
                .badges(badges) // 뱃지
                .build();
    }
}
