package com.sunlight.invest.notification.service;

import com.sunlight.invest.notification.config.NotificationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 邮件通知服务测试类
 * <p>
 * 测试邮件发送的各种场景，包括正常发送、配置未启用、发送器未配置等情况
 * </p>
 *
 * @author System
 * @since 2024-12-02
 */
@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationProperties notificationProperties;

    @Mock
    private NotificationProperties.MailConfig mailConfig;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    @BeforeEach
    void setUp() {
        // 设置默认配置（使用lenient模式避免UnnecessaryStubbing错误）
        lenient().when(notificationProperties.getMail()).thenReturn(mailConfig);
        lenient().when(mailConfig.isEnabled()).thenReturn(true);
        lenient().when(mailConfig.getFrom()).thenReturn("903635811@qq.com");
        lenient().when(mailConfig.getTo()).thenReturn("57954282@qq.com");
        
        // 设置fromEmail字段
        ReflectionTestUtils.setField(emailNotificationService, "fromEmail", "903635811@qq.com");
    }

    @Test
    void testSendSimpleEmail_Success() {
        // 准备测试数据
        String to = "57954282@qq.com";
        String subject = "测试邮件";
        String content = "这是测试内容";

        // 执行测试
        emailNotificationService.sendSimpleEmail(to, subject, content);

        // 验证邮件发送
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(to, sentMessage.getTo()[0]);
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(content, sentMessage.getText());
        assertEquals("903635811@qq.com", sentMessage.getFrom());
    }

    @Test
    void testSendSimpleEmail_UseDefaultRecipient() {
        // 准备测试数据
        String subject = "测试邮件";
        String content = "这是测试内容";

        // 执行测试（to为null，应使用默认收件人）
        emailNotificationService.sendSimpleEmail(null, subject, content);

        // 验证使用默认收件人
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("57954282@qq.com", sentMessage.getTo()[0]);
    }

    @Test
    void testSendSimpleEmail_DisabledService() {
        // 设置邮件服务为禁用
        when(mailConfig.isEnabled()).thenReturn(false);

        // 执行测试
        emailNotificationService.sendSimpleEmail("test@example.com", "主题", "内容");

        // 验证没有发送邮件
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendSimpleEmail_NoMailSender() {
        // 设置mailSender为null
        ReflectionTestUtils.setField(emailNotificationService, "mailSender", null);

        // 执行测试并验证抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailNotificationService.sendSimpleEmail("test@example.com", "主题", "内容");
        });

        assertTrue(exception.getMessage().contains("邮件发送器未配置"));
    }

    @Test
    void testSendSimpleEmail_NoRecipient() {
        // 设置没有默认收件人
        when(mailConfig.getTo()).thenReturn(null);

        // 执行测试（to为null）
        emailNotificationService.sendSimpleEmail(null, "主题", "内容");

        // 验证没有发送邮件
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendSimpleEmail_UseFallbackFromEmail() {
        // 设置from为空，应使用fromEmail
        when(mailConfig.getFrom()).thenReturn("");

        // 执行测试
        emailNotificationService.sendSimpleEmail("903635811@qq.com", "主题", "内容");

        // 验证使用了fromEmail
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("903635811@qq.com", sentMessage.getFrom());
    }

    @Test
    void testSendHtmlEmail_Success() throws MessagingException {
        // 准备测试数据
        String to = "test@example.com";
        String subject = "HTML测试邮件";
        String htmlContent = "<h1>测试</h1><p>这是HTML内容</p>";

        // Mock MimeMessage
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // 执行测试
        emailNotificationService.sendHtmlEmail(to, subject, htmlContent);

        // 验证发送了邮件
        verify(mailSender).send(any(MimeMessage.class));
        verify(mailSender).createMimeMessage();
    }

    @Test
    void testSendHtmlEmail_DisabledService() {
        // 设置邮件服务为禁用
        when(mailConfig.isEnabled()).thenReturn(false);

        // 执行测试
        emailNotificationService.sendHtmlEmail("test@example.com", "主题", "<p>内容</p>");

        // 验证没有发送邮件
        verify(mailSender, never()).createMimeMessage();
    }

    @Test
    void testSendEmail_ToDefaultRecipient() {
        // 执行测试
        emailNotificationService.sendEmail("测试主题", "测试内容");

        // 验证发送给默认收件人
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("57954282@qq.com", sentMessage.getTo()[0]);
        assertEquals("测试主题", sentMessage.getSubject());
    }

    @Test
    void testIsAvailable_True() {
        // 执行测试
        boolean available = emailNotificationService.isAvailable();

        // 验证结果
        assertTrue(available);
    }

    @Test
    void testIsAvailable_ServiceDisabled() {
        // 设置服务禁用
        when(mailConfig.isEnabled()).thenReturn(false);

        // 执行测试
        boolean available = emailNotificationService.isAvailable();

        // 验证结果
        assertFalse(available);
    }

    @Test
    void testIsAvailable_NoMailSender() {
        // 设置mailSender为null
        ReflectionTestUtils.setField(emailNotificationService, "mailSender", null);

        // 执行测试
        boolean available = emailNotificationService.isAvailable();

        // 验证结果
        assertFalse(available);
    }

    @Test
    void testSendSimpleEmail_SendException() {
        // Mock异常
        doThrow(new RuntimeException("发送失败")).when(mailSender).send(any(SimpleMailMessage.class));

        // 执行测试并验证抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailNotificationService.sendSimpleEmail("test@example.com", "主题", "内容");
        });

        assertTrue(exception.getMessage().contains("邮件发送失败"));
    }
}
