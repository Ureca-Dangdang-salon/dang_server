package com.dangdangsalon.exception;

public class TokenExpiredException extends RuntimeException {

    public TokenExpiredException() {
        super("토큰이 만료되었습니다. 로그인이 필요합니다.");
    }
}
