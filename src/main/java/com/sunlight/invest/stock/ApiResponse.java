package com.sunlight.invest.stock;

import com.sunlight.invest.common.BaseResponse;

public class ApiResponse<T> extends BaseResponse<T> {
    
    public ApiResponse() {
        super();
    }
    
    public ApiResponse(boolean success, String message, T data) {
        super(success, message, data);
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "操作成功", data);
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}