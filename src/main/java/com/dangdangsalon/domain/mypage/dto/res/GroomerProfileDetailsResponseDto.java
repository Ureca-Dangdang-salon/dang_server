package com.dangdangsalon.domain.mypage.dto.res;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.entity.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
// 미용사 상세 조회 (모두 공개) (응답) 1
public class GroomerProfileDetailsResponseDto {
    private Long profileId;
    private String name;
    private String imageKey;
    private String phone;
    private String businessNumber;
    private String contactHours;
    private ServiceType serviceType;
    private String address;
    private String experience;
    private String description;
    private String startMessage;
    private String faq;
    private GroomerProfileDetailsInfoResponseDto groomerProfileDetailsInfoResponseDto;

    public static GroomerProfileDetailsResponseDto create(
            GroomerProfile groomerProfile,
            GroomerProfileDetailsInfoResponseDto groomerProfileDetailsInfoResponseDto) {
        if (groomerProfile.getDetails() == null) {
            return GroomerProfileDetailsResponseDto.builder()
                    .profileId(groomerProfile.getId())
                    .name(groomerProfile.getName())
                    .imageKey(groomerProfile.getImageKey())
                    .businessNumber(null)
                    .phone(groomerProfile.getPhone())
                    .contactHours(groomerProfile.getContactHours())
                    .serviceType(groomerProfile.getServiceType())
                    .address(null)
                    .experience(null)
                    .description(null)
                    .startMessage(null)
                    .faq(null)
                    .groomerProfileDetailsInfoResponseDto(groomerProfileDetailsInfoResponseDto)
                    .build();
        }
        return GroomerProfileDetailsResponseDto.builder()
                .profileId(groomerProfile.getId())
                .name(groomerProfile.getName())
                .imageKey(groomerProfile.getImageKey())
                .businessNumber(groomerProfile.getDetails().getBusinessNumber())
                .phone(groomerProfile.getPhone())
                .contactHours(groomerProfile.getContactHours())
                .serviceType(groomerProfile.getServiceType())
                .address(groomerProfile.getDetails().getAddress())
                .experience(groomerProfile.getDetails().getExperience())
                .description(groomerProfile.getDetails().getDescription())
                .startMessage(groomerProfile.getDetails().getStartChat())
                .faq(groomerProfile.getDetails().getFaq())
                .groomerProfileDetailsInfoResponseDto(groomerProfileDetailsInfoResponseDto)
                .build();
    }
}
