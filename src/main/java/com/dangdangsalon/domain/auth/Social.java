package com.dangdangsalon.domain.auth;

import com.dangdangsalon.domain.auth.dto.GoogleResponse;
import com.dangdangsalon.domain.auth.dto.KakaoResponse;
import com.dangdangsalon.domain.auth.dto.NaverResponse;
import com.dangdangsalon.domain.auth.dto.OAuth2Response;
import java.util.Arrays;
import java.util.Map;


public enum Social {

    NAVER("naver") {
        @Override
        public OAuth2Response createResponse(Map<String, Object> attributes) {
            return new NaverResponse(attributes);
        }
    },
    GOOGLE("google") {
        @Override
        public OAuth2Response createResponse(Map<String, Object> attributes) {
            return new GoogleResponse(attributes);
        }
    },
    KAKAO("kakao") {
        @Override
        public OAuth2Response createResponse(Map<String, Object> attributes) {
            return new KakaoResponse(attributes);
        }
    };

    private final String name;

    Social(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract OAuth2Response createResponse(Map<String, Object> attributes);

    public static Social fromName(String name) {
        return Arrays.stream(Social.values())
                .filter(social -> social.name.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 소셜 로그인 타입입니다: " + name));
    }
}
