package com.dangdangsalon.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JoinAdditionalInfoDto {
    String role;
    Long districtId;
}
