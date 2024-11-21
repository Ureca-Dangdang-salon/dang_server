package com.dangdangsalon.domain.auth.dto;

public interface OAuth2Response {

    String getProvider(); //제공자 (naver, google, kakao)
    String getProviderId();
    String getEmail();
    String getName();
    String getProfileImage();
}
