package com.sunlight.invest.notification.service;

import com.sunlight.invest.notification.entity.EmailRecipient;
import com.sunlight.invest.notification.mapper.EmailRecipientMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 邮件接收人服务类
 *
 * @author System
 * @since 2024-12-04
 */
@Service
public class EmailRecipientService {

    private static final Logger log = LoggerFactory.getLogger(EmailRecipientService.class);

    @Autowired
    private EmailRecipientMapper emailRecipientMapper;

    /**
     * 添加邮件接收人
     *
     * @param emailRecipient 邮件接收人对象
     * @return 添加的记录数
     */
    public int addEmailRecipient(EmailRecipient emailRecipient) {
        try {
            // 检查邮箱是否已存在
            EmailRecipient existing = emailRecipientMapper.selectByEmail(emailRecipient.getEmail());
            if (existing != null) {
                throw new RuntimeException("邮箱地址已存在: " + emailRecipient.getEmail());
            }
            
            int result = emailRecipientMapper.insert(emailRecipient);
            log.info("添加邮件接收人成功: {}", emailRecipient.getEmail());
            return result;
        } catch (Exception e) {
            log.error("添加邮件接收人失败: {}", emailRecipient.getEmail(), e);
            throw new RuntimeException("添加邮件接收人失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID获取邮件接收人
     *
     * @param id 邮件接收人ID
     * @return 邮件接收人对象
     */
    public EmailRecipient getEmailRecipientById(Long id) {
        try {
            EmailRecipient emailRecipient = emailRecipientMapper.selectById(id);
            log.debug("查询邮件接收人: ID={}", id);
            return emailRecipient;
        } catch (Exception e) {
            log.error("查询邮件接收人失败: ID={}", id, e);
            throw new RuntimeException("查询邮件接收人失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据邮箱获取邮件接收人
     *
     * @param email 邮箱地址
     * @return 邮件接收人对象
     */
    public EmailRecipient getEmailRecipientByEmail(String email) {
        try {
            EmailRecipient emailRecipient = emailRecipientMapper.selectByEmail(email);
            log.debug("查询邮件接收人: email={}", email);
            return emailRecipient;
        } catch (Exception e) {
            log.error("查询邮件接收人失败: email={}", email, e);
            throw new RuntimeException("查询邮件接收人失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据用户ID获取邮件接收人列表
     *
     * @param userId 用户ID
     * @return 邮件接收人列表
     */
    public List<EmailRecipient> getEmailRecipientsByUserId(Long userId) {
        try {
            List<EmailRecipient> emailRecipients = emailRecipientMapper.selectByUserId(userId);
            log.debug("查询邮件接收人: userId={}, 数量: {}", userId, emailRecipients.size());
            return emailRecipients;
        } catch (Exception e) {
            log.error("查询邮件接收人失败: userId={}", userId, e);
            throw new RuntimeException("查询邮件接收人失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有启用的邮件接收人
     *
     * @return 邮件接收人列表
     */
    public List<EmailRecipient> getAllEnabledEmailRecipients() {
        try {
            List<EmailRecipient> emailRecipients = emailRecipientMapper.selectAllEnabled();
            log.debug("查询所有启用的邮件接收人，数量: {}", emailRecipients.size());
            return emailRecipients;
        } catch (Exception e) {
            log.error("查询所有启用的邮件接收人失败", e);
            throw new RuntimeException("查询所有启用的邮件接收人失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有邮件接收人
     *
     * @return 邮件接收人列表
     */
    public List<EmailRecipient> getAllEmailRecipients() {
        try {
            List<EmailRecipient> result = emailRecipientMapper.selectAll();
            log.debug("查询所有邮件接收人，数量: {}", result.size());
            return result;
        } catch (Exception e) {
            log.error("查询所有邮件接收人失败", e);
            throw new RuntimeException("查询所有邮件接收人失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新邮件接收人
     *
     * @param emailRecipient 邮件接收人对象
     * @return 更新的记录数
     */
    public int updateEmailRecipient(EmailRecipient emailRecipient) {
        try {
            // 检查邮箱是否已存在于其他记录中
            EmailRecipient existing = emailRecipientMapper.selectByEmail(emailRecipient.getEmail());
            if (existing != null && !existing.getId().equals(emailRecipient.getId())) {
                throw new RuntimeException("邮箱地址已存在: " + emailRecipient.getEmail());
            }
            
            int result = emailRecipientMapper.update(emailRecipient);
            log.info("更新邮件接收人成功: ID={}", emailRecipient.getId());
            return result;
        } catch (Exception e) {
            log.error("更新邮件接收人失败: ID={}", emailRecipient.getId(), e);
            throw new RuntimeException("更新邮件接收人失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID删除邮件接收人
     *
     * @param id 主键ID
     * @return 删除的记录数
     */
    public int deleteEmailRecipientById(Long id) {
        try {
            int result = emailRecipientMapper.deleteById(id);
            log.info("删除邮件接收人成功: ID={}", id);
            return result;
        } catch (Exception e) {
            log.error("删除邮件接收人失败: ID={}", id, e);
            throw new RuntimeException("删除邮件接收人失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据邮箱删除邮件接收人
     *
     * @param email 邮箱地址
     * @return 删除的记录数
     */
    public int deleteEmailRecipientByEmail(String email) {
        try {
            int result = emailRecipientMapper.deleteByEmail(email);
            log.info("删除邮件接收人成功: 邮箱={}", email);
            return result;
        } catch (Exception e) {
            log.error("删除邮件接收人失败: 邮箱={}", email, e);
            throw new RuntimeException("删除邮件接收人失败: " + e.getMessage(), e);
        }
    }
}