package com.sunlight.invest.notification.service;

import com.sunlight.invest.common.Constants;
import com.sunlight.invest.notification.config.NotificationProperties;
import com.sunlight.invest.notification.entity.EmailRecipient;
import com.sunlight.invest.notification.mapper.EmailRecipientMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

/**
 * 邮件通知服务
 * <p>
 * 提供邮件发送功能，支持简单文本邮件和HTML格式邮件。
 * 使用Spring Mail进行邮件发送，支持配置启用/禁用功能。
 * </p>
 *
 * @author System
 * @since 2024-12-02
 */
@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private NotificationProperties notificationProperties;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;
    
    @Autowired(required = false)
    private EmailRecipientMapper emailRecipientMapper;

    public static String receiveEmail = Constants.Email.DEFAULT_RECIPIENT_EMAIL;

    /**
     * 发送简单文本邮件
     *
     * @param to      收件人邮箱地址，如果为null则使用配置的默认收件人
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    public void sendSimpleEmail(String to, String subject, String content) {
        // 检查邮件服务是否启用
        if (!notificationProperties.getMail().isEnabled()) {
            log.warn("邮件通知未启用，跳过邮件发送");
            return;
        }

        // 检查邮件发送器是否配置
        if (mailSender == null) {
            log.error("邮件发送器未配置，请检查spring.mail配置");
            throw new RuntimeException("邮件发送器未配置");
        }
        
        // 确定收件人
        String recipient = (to != null && !to.trim().isEmpty()) ? to : notificationProperties.getMail().getTo();
        if (recipient == null || recipient.trim().isEmpty()) {
            log.warn("未配置收件人邮箱，跳过邮件发送");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            
            // 设置发件人，优先使用配置的from，其次使用spring.mail.username
            String sender = notificationProperties.getMail().getFrom();
            if (sender == null || sender.trim().isEmpty()) {
                sender = fromEmail;
            }
            message.setFrom(sender);
            
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("邮件发送成功，收件人: {}, 主题: {}", recipient, subject);
            
        } catch (Exception e) {
            log.error("发送邮件失败，收件人: {}, 主题: {}", to, subject, e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送HTML格式邮件
     *
     * @param to          收件人邮箱地址，如果为null则使用配置的默认收件人
     * @param subject     邮件主题
     * @param htmlContent HTML格式的邮件内容
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        // 检查邮件服务是否启用
        if (!notificationProperties.getMail().isEnabled()) {
            log.warn("邮件通知未启用，跳过HTML邮件发送");
            return;
        }

        // 检查邮件发送器是否配置
        if (mailSender == null) {
            log.error("邮件发送器未配置，请检查spring.mail配置");
            throw new RuntimeException("邮件发送器未配置");
        }
        
        // 确定收件人
        String recipient = (to != null && !to.trim().isEmpty()) ? to : notificationProperties.getMail().getTo();
        if (recipient == null || recipient.trim().isEmpty()) {
            log.warn("未配置收件人邮箱，跳过HTML邮件发送");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // 设置发件人，优先使用配置的from，其次使用spring.mail.username
            String sender = notificationProperties.getMail().getFrom();
            if (sender == null || sender.trim().isEmpty()) {
                sender = fromEmail;
            }
            helper.setFrom(sender);
            
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML邮件发送成功，收件人: {}, 主题: {}", recipient, subject);
            
        } catch (MessagingException e) {
            log.error("发送HTML邮件失败，收件人: {}, 主题: {}", to, subject, e);
            throw new RuntimeException("HTML邮件发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送邮件到默认收件人
     *
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    public void sendEmail(String subject, String content) {
        sendSimpleEmail(receiveEmail, subject, content);
    }

    /**
     * 发送邮件给所有启用的邮件接收人
     *
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    public void sendEmailToAllRecipients(String subject, String content) {
        // 检查邮件服务是否启用
        if (!notificationProperties.getMail().isEnabled()) {
            log.warn("邮件通知未启用，跳过邮件发送");
            return;
        }

        // 检查邮件发送器是否配置
        if (mailSender == null) {
            log.error("邮件发送器未配置，请检查spring.mail配置");
            throw new RuntimeException("邮件发送器未配置");
        }
        
        // 检查邮件接收人Mapper是否配置
        if (emailRecipientMapper == null) {
            log.warn("邮件接收人Mapper未配置，使用默认收件人");
            sendEmail(subject, content);
            return;
        }

        try {
            // 获取所有启用的邮件接收人
            List<EmailRecipient> recipients = emailRecipientMapper.selectAllEnabled();
            
            if (recipients.isEmpty()) {
                log.warn("没有启用的邮件接收人，使用默认收件人");
                sendEmail(subject, content);
                return;
            }
            
            // 提取邮箱地址
            String[] recipientEmails = recipients.stream()
                    .map(EmailRecipient::getEmail)
                    .toArray(String[]::new);
            
            SimpleMailMessage message = new SimpleMailMessage();
            
            // 设置发件人，优先使用配置的from，其次使用spring.mail.username
            String sender = notificationProperties.getMail().getFrom();
            if (sender == null || sender.trim().isEmpty()) {
                sender = fromEmail;
            }
            message.setFrom(sender);
            
            message.setTo(recipientEmails);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("邮件发送成功，收件人数量: {}, 主题: {}", recipientEmails.length, subject);
            
        } catch (Exception e) {
            log.error("发送邮件给所有接收人失败，主题: {}", subject, e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送HTML格式邮件给所有启用的邮件接收人
     *
     * @param subject     邮件主题
     * @param htmlContent HTML格式的邮件内容
     */
    public void sendHtmlEmailToAllRecipients(String subject, String htmlContent) {
        // 检查邮件服务是否启用
        if (!notificationProperties.getMail().isEnabled()) {
            log.warn("邮件通知未启用，跳过HTML邮件发送");
            return;
        }

        // 检查邮件发送器是否配置
        if (mailSender == null) {
            log.error("邮件发送器未配置，请检查spring.mail配置");
            throw new RuntimeException("邮件发送器未配置");
        }
        
        // 检查邮件接收人Mapper是否配置
        if (emailRecipientMapper == null) {
            log.warn("邮件接收人Mapper未配置，使用默认收件人");
            sendHtmlEmail(null, subject, htmlContent);
            return;
        }

        try {
            // 获取所有启用的邮件接收人
            List<EmailRecipient> recipients = emailRecipientMapper.selectAllEnabled();
            
            if (recipients.isEmpty()) {
                log.warn("没有启用的邮件接收人，使用默认收件人");
                sendHtmlEmail(null, subject, htmlContent);
                return;
            }
            
            // 提取邮箱地址
            String[] recipientEmails = recipients.stream()
                    .map(EmailRecipient::getEmail)
                    .toArray(String[]::new);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // 设置发件人，优先使用配置的from，其次使用spring.mail.username
            String sender = notificationProperties.getMail().getFrom();
            if (sender == null || sender.trim().isEmpty()) {
                sender = fromEmail;
            }
            helper.setFrom(sender);
            
            helper.setTo(recipientEmails);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML邮件发送成功，收件人数量: {}, 主题: {}", recipientEmails.length, subject);
            
        } catch (MessagingException e) {
            log.error("发送HTML邮件给所有接收人失败，主题: {}", subject, e);
            throw new RuntimeException("HTML邮件发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查邮件服务是否可用
     *
     * @return true-可用，false-不可用
     */
    public boolean isAvailable() {
        return notificationProperties.getMail().isEnabled() && mailSender != null;
    }
}
