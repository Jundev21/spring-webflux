package com.chat.chat.common.util;

import org.springframework.http.HttpStatus;

import com.chat.chat.dto.response.ApiResponse;
import com.chat.chat.dto.response.ErrorResponse;

public class ResponseUtils {
    private ResponseUtils() {}

    public static <T> ApiResponse<T> success(String msg, T data) {
        return new ApiResponse<>(true, msg, data);
    }
    public static <T> ApiResponse<T> fail(String msg) {
        return new ApiResponse<>(false,msg, null);
    }
    public static ErrorResponse failNoBody(String msg, HttpStatus httpStatus) {
        return new ErrorResponse(msg, httpStatus);
    }
}
