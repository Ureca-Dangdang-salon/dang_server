package com.dangdangsalon.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinAdditionalInfoDto {
    String role;
    Long districtId;
}
