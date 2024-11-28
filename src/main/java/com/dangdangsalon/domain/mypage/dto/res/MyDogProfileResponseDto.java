// MyDogProfileResponseDto.java
package com.dangdangsalon.domain.mypage.dto.res;

import com.dangdangsalon.domain.dogprofile.entity.Gender;
import com.dangdangsalon.domain.dogprofile.entity.Neutering;
import com.dangdangsalon.domain.estimate.request.dto.FeatureResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
// 반려견 상세 조회 (응답)
public class MyDogProfileResponseDto {
    private String name;
    private String profileImage;
    private String species;
    private int ageYear;
    private int ageMonth;
    private Gender gender;
    private Neutering neutering;
    private int weight;
    private List<FeatureResponseDto> features;
}