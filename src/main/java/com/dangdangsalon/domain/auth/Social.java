package com.dangdangsalon.domain.auth;

public enum Social {

    NAVER("naver"),
    GOOGLE("google"),
    KAKAO("kakao");

    private String name;

    Social(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
