package com.sunlight.invest.notification.dto;

/**
 * 通知请求DTO
 */
public class NotificationRequest {
    
    private String title;
    private String content;
    private String recipient; // 邮件接收者或微信接收者
    private NotificationType type; // 通知类型

    public enum NotificationType {
        EMAIL, WECHAT, ALL
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "NotificationRequest{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", recipient='" + recipient + '\'' +
                ", type=" + type +
                '}';
    }
}
