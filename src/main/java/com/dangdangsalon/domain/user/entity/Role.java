package com.dangdangsalon.domain.user.entity;

public enum Role {
    ROLE_ADMIN, ROLE_SALON, ROLE_USER, ROLE_PENDING;

    public static Role from(String s) {
        return Role.valueOf(s.toUpperCase());
    }
}
