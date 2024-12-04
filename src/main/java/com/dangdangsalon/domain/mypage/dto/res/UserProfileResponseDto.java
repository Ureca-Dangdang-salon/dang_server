package com.dangdangsalon.domain.mypage.dto.res;

import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import com.dangdangsalon.domain.user.entity.Role;
import com.dangdangsalon.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
// 유저 마이페이지 조회 (응답)
public class UserProfileResponseDto {
    private Role role;
    private String name;
    private String email;
    private String profileImage;
    private Long districtId;
    private String district;
    private String city;
    private List<DogProfileResponseDto> dogProfiles;
    private long couponCount;
    private long reviewCount;
    private long paymentCount;

    public static UserProfileResponseDto createUserProfileResponseDto(User user, long notUsedCouponCount, long paymentCount) {
        return UserProfileResponseDto.builder()
                .role(user.getRole())
                .name(user.getName())
                .email(user.getEmail())
                .profileImage(user.getImageKey())
                .districtId(user.getDistrict().getId())
                .city(user.getDistrict().getCity().getName())
                .district(user.getDistrict().getName())
                .dogProfiles(
                        user.getDogProfiles().stream()
                                .map(dog -> new DogProfileResponseDto(dog.getId(), dog.getImageKey(), dog.getName()))
                                .toList()
                )
                .couponCount(notUsedCouponCount)
                .reviewCount(user.getReviews().size())
                .paymentCount(paymentCount)
                .build();
    }
}
