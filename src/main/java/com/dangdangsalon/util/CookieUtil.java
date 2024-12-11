package com.dangdangsalon.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public String createCookie(String key, String value) {
//        Cookie cookie = new Cookie(key, value);
//        cookie.setHttpOnly(true);
//        cookie.setPath("/");
//        cookie.setMaxAge(60 * 60 * 24 * 30);
//        cookie.setSecure(true);
//        cookie.setDomain("dangdangsalon.netlify.app");

        return ResponseCookie.from(key, value)
                .path("/")
//                .domain(".dangdang-salon.com")
                .maxAge(60 * 60 * 24 * 30)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build()
                .toString();
    }

    public String getCookieValue(String cookieName, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
