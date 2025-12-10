package com.sunlight.invest.notification.dto;

import com.sunlight.invest.common.BaseResponse;

/**
 * 通知响应DTO
 */
public class NotificationResponse extends BaseResponse<Object> {
    
    public NotificationResponse() {
        super();
    }

    public NotificationResponse(boolean success, String message) {
        super(success, message);
    }
    
    public static NotificationResponse success(String message) {
        return new NotificationResponse(true, message);
    }

    public static NotificationResponse failure(String message) {
        return new NotificationResponse(false, message);
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
