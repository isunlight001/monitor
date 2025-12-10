package com.sunlight.invest.common;

/**
 * 通用响应基类
 * @param <T> 数据类型
 */
public class BaseResponse<T> {
    protected boolean success;
    protected String message;
    protected T data;
    protected String timestamp;

    public BaseResponse() {
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    public BaseResponse(boolean success, String message) {
        this(success, message, null);
    }

    public BaseResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, "操作成功", data);
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(true, message, data);
    }

    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<>(false, message);
    }

    // Getter和Setter方法
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}