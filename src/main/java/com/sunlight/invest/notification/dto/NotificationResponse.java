package com.sunlight.invest.notification.dto;

/**
 * 通知响应DTO
 */
public class NotificationResponse {
    
    private boolean success;
    private String message;
    private String timestamp;

    public NotificationResponse() {
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    public NotificationResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    public static NotificationResponse success(String message) {
        return new NotificationResponse(true, message);
    }

    public static NotificationResponse failure(String message) {
        return new NotificationResponse(false, message);
    }

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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "NotificationResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
