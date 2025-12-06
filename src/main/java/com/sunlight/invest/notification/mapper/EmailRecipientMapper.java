package com.sunlight.invest.notification.mapper;

import com.sunlight.invest.notification.entity.EmailRecipient;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 邮件接收人Mapper接口
 *
 * @author System
 * @since 2024-12-04
 */
@Mapper
public interface EmailRecipientMapper {

    /**
     * 插入邮件接收人记录
     *
     * @param emailRecipient 邮件接收人对象
     * @return 影响行数
     */
    @Insert("INSERT INTO email_recipient (name, email, enabled, user_id, create_time, update_time) " +
            "VALUES (#{name}, #{email}, #{enabled}, #{userId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EmailRecipient emailRecipient);

    /**
     * 根据ID查询邮件接收人
     *
     * @param id 主键ID
     * @return 邮件接收人对象
     */
    @Select("SELECT * FROM email_recipient WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "email", column = "email"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    EmailRecipient selectById(@Param("id") Long id);

    /**
     * 根据邮箱查询邮件接收人
     *
     * @param email 邮箱地址
     * @return 邮件接收人对象
     */
    @Select("SELECT * FROM email_recipient WHERE email = #{email}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "email", column = "email"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    EmailRecipient selectByEmail(@Param("email") String email);

    /**
     * 根据用户ID查询邮件接收人
     *
     * @param userId 用户ID
     * @return 邮件接收人列表
     */
    @Select("SELECT * FROM email_recipient WHERE user_id = #{userId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "email", column = "email"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<EmailRecipient> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询所有启用的邮件接收人
     *
     * @return 邮件接收人列表
     */
    @Select("SELECT * FROM email_recipient WHERE enabled = 1 ORDER BY create_time ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "email", column = "email"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<EmailRecipient> selectAllEnabled();

    /**
     * 查询所有邮件接收人
     *
     * @return 邮件接收人列表
     */
    @Select("SELECT * FROM email_recipient ORDER BY create_time ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "email", column = "email"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<EmailRecipient> selectAll();

    /**
     * 更新邮件接收人
     *
     * @param emailRecipient 邮件接收人对象
     * @return 影响行数
     */
    @Update("UPDATE email_recipient SET name = #{name}, email = #{email}, enabled = #{enabled}, " +
            "user_id = #{userId}, update_time = NOW() " +
            "WHERE id = #{id}")
    int update(EmailRecipient emailRecipient);

    /**
     * 根据ID删除邮件接收人
     *
     * @param id 主键ID
     * @return 影响行数
     */
    @Delete("DELETE FROM email_recipient WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 根据邮箱删除邮件接收人
     *
     * @param email 邮箱地址
     * @return 影响行数
     */
    @Delete("DELETE FROM email_recipient WHERE email = #{email}")
    int deleteByEmail(@Param("email") String email);
    
    /**
     * 统计所有邮件接收人数量
     *
     * @return 邮件接收人数量
     */
    @Select("SELECT COUNT(*) FROM email_recipient")
    int countAll();
}