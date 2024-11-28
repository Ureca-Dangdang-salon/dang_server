package com.dangdangsalon.domain.mypage.dto.req;

import com.dangdangsalon.domain.dogprofile.entity.Gender;
import com.dangdangsalon.domain.dogprofile.entity.Neutering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 반려견 등록 (요청)
public class DogProfileRequestDto {
    private String name;
    private String profileImage;
    private String species;
    private int ageYear;
    private int ageMonth;
    private Gender gender;
    private Neutering neutering;
    private int weight;
    private List<Long> featureIds;
    private String additionalFeature;
}