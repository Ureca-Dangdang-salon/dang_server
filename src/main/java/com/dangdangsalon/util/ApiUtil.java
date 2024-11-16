package com.dangdangsalon.util;

import lombok.Getter;

public class ApiUtil {

    public static <T> ApiSuccess<T> success(T response) {
        return new ApiSuccess<>(response);
    }

    public static <T> ApiError<T> error(int statusCode, T message) {
        return new ApiError<>(statusCode, message);
    }

    @Getter
    public static class ApiSuccess<T> {
        private final T response;

        private ApiSuccess(T response) {
            this.response = response;
        }
    }

    @Getter
    public static class ApiError<T> {
        private final int statusCode;
        private final T message;

        private ApiError(int statusCode, T message) {
            this.statusCode = statusCode;
            this.message = message;
        }
    }
}

