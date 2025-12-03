package com.sunlight.invest.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 通知配置属性类
 * <p>
 * 用于管理系统通知相关的配置信息，包括邮件和微信通知的配置。
 * 通过@ConfigurationProperties注解自动绑定application.yml中notification前缀的配置项。
 * </p>
 *
 * @author System
 * @since 2024-12-02
 */
@Component
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    /**
     * 邮件通知配置
     */
    private MailConfig mail = new MailConfig();
    
    /**
     * 微信通知配置
     */
    private WeChatConfig wechat = new WeChatConfig();

    /**
     * 获取邮件配置
     *
     * @return 邮件配置对象
     */
    public MailConfig getMail() {
        return mail;
    }

    /**
     * 设置邮件配置
     *
     * @param mail 邮件配置对象
     */
    public void setMail(MailConfig mail) {
        this.mail = mail;
    }

    /**
     * 获取微信配置
     *
     * @return 微信配置对象
     */
    public WeChatConfig getWechat() {
        return wechat;
    }

    /**
     * 设置微信配置
     *
     * @param wechat 微信配置对象
     */
    public void setWechat(WeChatConfig wechat) {
        this.wechat = wechat;
    }

    /**
     * 邮件配置类
     * <p>
     * 包含邮件通知的启用状态、发送方邮箱和接收方邮箱等配置信息。
     * </p>
     */
    public static class MailConfig {
        /**
         * 是否启用邮件通知，默认为true
         */
        private boolean enabled = true;
        
        /**
         * 发送方邮箱地址
         */
        private String from;
        
        /**
         * 默认接收方邮箱地址，多个邮箱用逗号分隔
         */
        private String to;

        /**
         * 邮箱密码授权码
         */
        private String password;


        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        /**
         * 判断邮件通知是否启用
         *
         * @return true-启用，false-禁用
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * 设置邮件通知启用状态
         *
         * @param enabled true-启用，false-禁用
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * 获取发送方邮箱地址
         *
         * @return 发送方邮箱地址
         */
        public String getFrom() {
            return from;
        }

        /**
         * 设置发送方邮箱地址
         *
         * @param from 发送方邮箱地址
         */
        public void setFrom(String from) {
            this.from = from;
        }

        /**
         * 获取接收方邮箱地址
         *
         * @return 接收方邮箱地址
         */
        public String getTo() {
            return to;
        }

        /**
         * 设置接收方邮箱地址
         *
         * @param to 接收方邮箱地址，多个邮箱用逗号分隔
         */
        public void setTo(String to) {
            this.to = to;
        }
    }

    /**
     * 微信配置类
     * <p>
     * 支持Server酱和企业微信两种通知方式的配置。
     * Server酱方式简单易用，企业微信方式功能更强大。
     * </p>
     */
    public static class WeChatConfig {
        /**
         * 是否启用微信通知，默认为true
         */
        private boolean enabled = true;
        
        /**
         * Server酱的SendKey，从 https://sct.ftqq.com/ 获取
         */
        private String serverChanKey;
        
        /**
         * 企业微信的企业ID
         */
        private String corpId;
        
        /**
         * 企业微信的应用密钥
         */
        private String corpSecret;
        
        /**
         * 企业微信的应用ID
         */
        private String agentId;

        /**
         * 判断微信通知是否启用
         *
         * @return true-启用，false-禁用
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * 设置微信通知启用状态
         *
         * @param enabled true-启用，false-禁用
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * 获取Server酱的SendKey
         *
         * @return Server酱的SendKey
         */
        public String getServerChanKey() {
            return serverChanKey;
        }

        /**
         * 设置Server酱的SendKey
         *
         * @param serverChanKey Server酱的SendKey，从 https://sct.ftqq.com/ 获取
         */
        public void setServerChanKey(String serverChanKey) {
            this.serverChanKey = serverChanKey;
        }

        /**
         * 获取企业微信的企业ID
         *
         * @return 企业微信的企业ID
         */
        public String getCorpId() {
            return corpId;
        }

        /**
         * 设置企业微信的企业ID
         *
         * @param corpId 企业微信的企业ID
         */
        public void setCorpId(String corpId) {
            this.corpId = corpId;
        }

        /**
         * 获取企业微信的应用密钥
         *
         * @return 企业微信的应用密钥
         */
        public String getCorpSecret() {
            return corpSecret;
        }

        /**
         * 设置企业微信的应用密钥
         *
         * @param corpSecret 企业微信的应用密钥
         */
        public void setCorpSecret(String corpSecret) {
            this.corpSecret = corpSecret;
        }

        /**
         * 获取企业微信的应用ID
         *
         * @return 企业微信的应用ID
         */
        public String getAgentId() {
            return agentId;
        }

        /**
         * 设置企业微信的应用ID
         *
         * @param agentId 企业微信的应用ID
         */
        public void setAgentId(String agentId) {
            this.agentId = agentId;
        }
    }
}
