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
// 미용사 회원가입 설정 1 (요청)
public class GroomerProfileRequestDto {
    private String name;
    private String phone;
    private String contactHours;
    private ServiceType serviceType;
    private List<Long> servicesOfferedId;
    private List<Long> servicesDistrictIds;
}
