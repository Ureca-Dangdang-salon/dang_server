package com.dangdangsalon.domain.mypage.dto.req;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommonProfileRequestDto {
    private String imageKey;
    private String email;
    private Long districtId;
}
