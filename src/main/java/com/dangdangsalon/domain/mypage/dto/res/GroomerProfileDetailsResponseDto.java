package com.dangdangsalon.domain.mypage.dto.res;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
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

    public static GroomerProfileDetailsResponseDto createGroomerProfileDetailsResponseDto(
            GroomerProfile groomerProfile,
            double totalScore,
            long reviewCount,
            long estimateRequestCount,
            List<BadgeResponseDto> badges,
            List<GroomerServicesResponseDto> servicesOffered,
            List<DistrictResponseDto> serviceDistricts,
            List<String> certifications) {

        return GroomerProfileDetailsResponseDto.builder()
                .profileId(groomerProfile.getId())
                .serviceName(groomerProfile.getName())
                .imageKey(groomerProfile.getImageKey())
                .businessNumber(groomerProfile.getDetails().getBusinessNumber())
                .contact(groomerProfile.getPhone())
                .contactHours(groomerProfile.getContactHours())
                .serviceType(groomerProfile.getServiceType())
                .servicesDistricts(serviceDistricts) // 서비스 지역
                .starScore(totalScore)
                .estimateRequestCount(estimateRequestCount)
                .reviewCount(reviewCount)
                .address(groomerProfile.getDetails().getAddress())
                .experience(groomerProfile.getDetails().getExperience())
                .certifications(certifications) // 자격증
                .servicesOffered(servicesOffered) // 서비스 제공
                .description(groomerProfile.getDetails().getDescription())
                .startMessage(groomerProfile.getDetails().getStartChat())
                .badges(badges) // 뱃지
                .faq(groomerProfile.getDetails().getFaq())
                .build();
    }
}
