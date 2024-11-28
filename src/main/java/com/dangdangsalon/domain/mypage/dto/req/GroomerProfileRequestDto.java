package com.dangdangsalon.domain.mypage.dto.req;

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
    private String serviceName;
    private String contact;
    private List<Long> servicesOfferedId;
    private String contactHours;
}
