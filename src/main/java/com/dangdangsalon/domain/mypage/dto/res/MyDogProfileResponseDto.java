// MyDogProfileResponseDto.java
package com.dangdangsalon.domain.mypage.dto.res;

import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
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

    public static MyDogProfileResponseDto createMyDogProfileResponseDto(DogProfile dogProfile,
                                                                        List<FeatureResponseDto> featureDtos) {
        return MyDogProfileResponseDto.builder()
                .name(dogProfile.getName())
                .profileImage(dogProfile.getImageKey())
                .species(dogProfile.getSpecies())
                .ageYear(dogProfile.getAge().getYear())
                .ageMonth(dogProfile.getAge().getMonth())
                .gender(dogProfile.getGender())
                .neutering(dogProfile.getNeutering())
                .weight(dogProfile.getWeight())
                .features(featureDtos)
                .build();
    }
}