package com.chat.chat.common.util;

import com.chat.chat.dto.response.ApiResponse;

public class ResponseUtils {
    private ResponseUtils() {}

    public static <T> ApiResponse<T> success(String msg, T data) {
        return new ApiResponse<>(true, msg, data);
    }
    public static <T> ApiResponse<T> fail(String msg) {
        return new ApiResponse<>(false,msg, null);
    }
}
