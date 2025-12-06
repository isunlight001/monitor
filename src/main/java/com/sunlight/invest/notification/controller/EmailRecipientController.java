package com.sunlight.invest.notification.controller;

import com.sunlight.invest.notification.entity.EmailRecipient;
import com.sunlight.invest.notification.service.EmailRecipientService;
import com.sunlight.invest.system.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
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
     * 获取当前登录用户
     */
    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    /**
     * 添加邮件接收人
     */
    @PostMapping
    public Map<String, Object> addEmailRecipient(@RequestBody EmailRecipient emailRecipient, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            User currentUser = getCurrentUser(session);
            if (currentUser == null) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }

            // 设置用户ID
            emailRecipient.setUserId(currentUser.getId());
            
            int count = emailRecipientService.addEmailRecipient(emailRecipient);
            if (count > 0) {
                result.put("success", true);
                result.put("message", "邮件接收人添加成功");
            } else {
                result.put("success", false);
                result.put("message", "邮件接收人添加失败");
            }
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
    public Map<String, Object> getEmailRecipientById(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            User currentUser = getCurrentUser(session);
            if (currentUser == null) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }

            EmailRecipient emailRecipient = emailRecipientService.getEmailRecipientById(id);
            // 检查是否是当前用户的邮件接收人
            if (emailRecipient != null && 
                (emailRecipient.getUserId() == null || emailRecipient.getUserId().equals(currentUser.getId()))) {
                result.put("success", true);
                result.put("data", emailRecipient);
            } else {
                result.put("success", false);
                result.put("message", "邮件接收人不存在或无权限访问");
            }
        } catch (Exception e) {
            log.error("查询邮件接收人失败", e);
            result.put("success", false);
            result.put("message", "查询邮件接收人失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 查询所有邮件接收人（仅当前用户的）
     */
    @GetMapping
    public Map<String, Object> getAllEmailRecipients(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            User currentUser = getCurrentUser(session);
            if (currentUser == null) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }

            List<EmailRecipient> emailRecipients;
            // 如果是管理员用户，可以查看所有邮件接收人
            if ("admin".equals(currentUser.getUsername())) {
                emailRecipients = emailRecipientService.getAllEmailRecipients();
            } else {
                // 普通用户只能查看自己的邮件接收人
                emailRecipients = emailRecipientService.getEmailRecipientsByUserId(currentUser.getId());
            }
            
            result.put("success", true);
            result.put("data", emailRecipients);
            result.put("count", emailRecipients.size());
        } catch (Exception e) {
            log.error("查询所有邮件接收人失败", e);
            result.put("success", false);
            result.put("message", "查询所有邮件接收人失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 查询所有启用的邮件接收人（仅当前用户的）
     */
    @GetMapping("/enabled")
    public Map<String, Object> getAllEnabledEmailRecipients(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            User currentUser = getCurrentUser(session);
            if (currentUser == null) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }

            List<EmailRecipient> emailRecipients;
            // 如果是管理员用户，可以查看所有启用的邮件接收人
            if ("admin".equals(currentUser.getUsername())) {
                emailRecipients = emailRecipientService.getAllEnabledEmailRecipients();
            } else {
                // 普通用户只能查看自己启用的邮件接收人
                // 这里需要修改服务层来支持这个功能
                emailRecipients = emailRecipientService.getEmailRecipientsByUserId(currentUser.getId());
                // 过滤启用的邮件接收人
                emailRecipients.removeIf(recipient -> !Boolean.TRUE.equals(recipient.getEnabled()));
            }
            
            result.put("success", true);
            result.put("data", emailRecipients);
            result.put("count", emailRecipients.size());
        } catch (Exception e) {
            log.error("查询所有启用的邮件接收人失败", e);
            result.put("success", false);
            result.put("message", "查询所有启用的邮件接收人失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新邮件接收人
     */
    @PutMapping
    public Map<String, Object> updateEmailRecipient(@RequestBody EmailRecipient emailRecipient, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            User currentUser = getCurrentUser(session);
            if (currentUser == null) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }

            // 检查是否是当前用户的邮件接收人
            EmailRecipient existing = emailRecipientService.getEmailRecipientById(emailRecipient.getId());
            if (existing == null) {
                result.put("success", false);
                result.put("message", "邮件接收人不存在");
                return result;
            }
            
            // 普通用户只能更新自己的邮件接收人
            if (!"admin".equals(currentUser.getUsername()) && 
                !existing.getUserId().equals(currentUser.getId())) {
                result.put("success", false);
                result.put("message", "无权限更新此邮件接收人");
                return result;
            }

            // 设置用户ID
            emailRecipient.setUserId(existing.getUserId());
            
            int count = emailRecipientService.updateEmailRecipient(emailRecipient);
            if (count > 0) {
                result.put("success", true);
                result.put("message", "邮件接收人更新成功");
            } else {
                result.put("success", false);
                result.put("message", "邮件接收人更新失败");
            }
        } catch (Exception e) {
            log.error("更新邮件接收人失败", e);
            result.put("success", false);
            result.put("message", "更新邮件接收人失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 根据ID删除邮件接收人
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteEmailRecipientById(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            User currentUser = getCurrentUser(session);
            if (currentUser == null) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }

            // 检查是否是当前用户的邮件接收人
            EmailRecipient existing = emailRecipientService.getEmailRecipientById(id);
            if (existing == null) {
                result.put("success", false);
                result.put("message", "邮件接收人不存在");
                return result;
            }
            
            // 普通用户只能删除自己的邮件接收人
            if (!"admin".equals(currentUser.getUsername()) && 
                !existing.getUserId().equals(currentUser.getId())) {
                result.put("success", false);
                result.put("message", "无权限删除此邮件接收人");
                return result;
            }

            int count = emailRecipientService.deleteEmailRecipientById(id);
            if (count > 0) {
                result.put("success", true);
                result.put("message", "邮件接收人删除成功");
            } else {
                result.put("success", false);
                result.put("message", "邮件接收人删除失败");
            }
        } catch (Exception e) {
            log.error("删除邮件接收人失败", e);
            result.put("success", false);
            result.put("message", "删除邮件接收人失败: " + e.getMessage());
        }
        return result;
    }
}