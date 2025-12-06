package com.sunlight.invest.notification.controller;

import com.sunlight.invest.notification.entity.EmailRecipient;
import com.sunlight.invest.notification.service.EmailRecipientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件接收人管理控制器
 *
 * @author System
 * @since 2024-12-04
 */
@RestController
@RequestMapping("/api/email-recipients")
public class EmailRecipientController {

    private static final Logger log = LoggerFactory.getLogger(EmailRecipientController.class);

    @Autowired
    private EmailRecipientService emailRecipientService;

    /**
     * 添加邮件接收人
     */
    @PostMapping
    public Map<String, Object> addEmailRecipient(@RequestBody EmailRecipient emailRecipient) {
        Map<String, Object> result = new HashMap<>();
        try {
            emailRecipientService.addEmailRecipient(emailRecipient);
            result.put("success", true);
            result.put("message", "邮件接收人添加成功");
        } catch (Exception e) {
            log.error("添加邮件接收人失败", e);
            result.put("success", false);
            result.put("message", "添加邮件接收人失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 根据ID查询邮件接收人
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmailRecipient> getEmailRecipientById(@PathVariable Long id) {
        try {
            EmailRecipient emailRecipient = emailRecipientService.getEmailRecipientById(id);
            if (emailRecipient != null) {
                return ResponseEntity.ok(emailRecipient);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("查询邮件接收人失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 查询所有邮件接收人
     */
    @GetMapping
    public ResponseEntity<List<EmailRecipient>> getAllEmailRecipients() {
        try {
            List<EmailRecipient> emailRecipients = emailRecipientService.getAllEmailRecipients();
            return ResponseEntity.ok(emailRecipients);
        } catch (Exception e) {
            log.error("查询所有邮件接收人失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 查询所有启用的邮件接收人
     */
    @GetMapping("/enabled")
    public ResponseEntity<List<EmailRecipient>> getAllEnabledEmailRecipients() {
        try {
            List<EmailRecipient> emailRecipients = emailRecipientService.getAllEnabledEmailRecipients();
            return ResponseEntity.ok(emailRecipients);
        } catch (Exception e) {
            log.error("查询所有启用的邮件接收人失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 更新邮件接收人
     */
    @PutMapping
    public ResponseEntity<String> updateEmailRecipient(@RequestBody EmailRecipient emailRecipient) {
        try {
            emailRecipientService.updateEmailRecipient(emailRecipient);
            return ResponseEntity.ok("邮件接收人更新成功");
        } catch (Exception e) {
            log.error("更新邮件接收人失败", e);
            return ResponseEntity.badRequest().body("更新邮件接收人失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID删除邮件接收人
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmailRecipientById(@PathVariable Long id) {
        try {
            emailRecipientService.deleteEmailRecipientById(id);
            return ResponseEntity.ok("邮件接收人删除成功");
        } catch (Exception e) {
            log.error("删除邮件接收人失败", e);
            return ResponseEntity.badRequest().body("删除邮件接收人失败: " + e.getMessage());
        }
    }
}