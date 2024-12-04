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
// 미용사 회원가입 설정 2 (요청)
public class GroomerProfileDetailsRequestDto {
    private String imageKey;
    private String businessNumber;
    private String address;
    private String experience;
    private String description;
    private String startMessage;
    private String faq;
    private List<String> certifications;
}
