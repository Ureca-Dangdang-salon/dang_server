package com.dangdangsalon.domain.mypage.dto.req;

import com.dangdangsalon.domain.groomerprofile.entity.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 미용사 프로필 수정 2 (요청)
public class GroomerDetailsUpdateRequestDto {
    private String imageKey;
    private String serviceName;
    private String contact;
    private List<Long> servicesDistrictIds;
    private String contactHours;
    private List<Long> servicesOfferedId;
    private ServiceType serviceType;
    private String businessNumber;
    private String address;
    private String experience;
    private List<String> certifications;
    private String description;
    private String startMessage;
    private String faq;

}
