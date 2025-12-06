package com.sunlight.invest.notification.entity;

import java.time.LocalDateTime;

/**
 * 邮件接收人实体类
 * <p>
 * 用于存储邮件接收人的信息
 * </p>
 *
 * @author System
 * @since 2024-12-04
 */
public class EmailRecipient {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 接收人姓名
     */
    private String name;
    
    /**
     * 接收人邮箱地址
     */
    private String email;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 关联用户ID
     */
    private Long userId;

    // Constructors
    public EmailRecipient() {}

    public EmailRecipient(String name, String email) {
        this.name = name;
        this.email = email;
        this.enabled = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "EmailRecipient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", enabled=" + enabled +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", userId=" + userId +
                '}';
    }
}