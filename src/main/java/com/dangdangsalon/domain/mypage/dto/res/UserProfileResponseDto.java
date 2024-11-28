package com.dangdangsalon.domain.mypage.dto.res;

import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
// 유저 마이페이지 조회 (응답)
public class UserProfileResponseDto {
    private String role;
    private String name;
    private String email;
    private String profileImage;
    private String city;
    private String district;
    private List<DogProfileResponseDto> dogProfiles;
    private long couponCount;
    private long reviewCount;
    private long paymentCount;
}
