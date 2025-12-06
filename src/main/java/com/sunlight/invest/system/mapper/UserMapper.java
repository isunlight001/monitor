package com.sunlight.invest.system.mapper;

import com.sunlight.invest.system.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户Mapper接口
 *
 * @author System
 * @since 2024-12-06
 */
@Mapper
public interface UserMapper {

    /**
     * 插入用户记录
     *
     * @param user 用户对象
     * @return 影响行数
     */
    @Insert("INSERT INTO user (username, password, email, real_name, enabled, create_time, update_time) " +
            "VALUES (#{username}, #{password}, #{email}, #{realName}, #{enabled}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 根据ID查询用户
     *
     * @param id 主键ID
     * @return 用户对象
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "email", column = "email"),
            @Result(property = "realName", column = "real_name"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    User selectById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "email", column = "email"),
            @Result(property = "realName", column = "real_name"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱地址
     * @return 用户对象
     */
    @Select("SELECT * FROM user WHERE email = #{email}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "email", column = "email"),
            @Result(property = "realName", column = "real_name"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    User selectByEmail(@Param("email") String email);

    /**
     * 查询所有启用的用户
     *
     * @return 用户列表
     */
    @Select("SELECT * FROM user WHERE enabled = 1 ORDER BY create_time ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "email", column = "email"),
            @Result(property = "realName", column = "real_name"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<User> selectAllEnabled();

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    @Select("SELECT * FROM user ORDER BY create_time ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "email", column = "email"),
            @Result(property = "realName", column = "real_name"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<User> selectAll();

    /**
     * 更新用户
     *
     * @param user 用户对象
     * @return 影响行数
     */
    @Update("UPDATE user SET username = #{username}, password = #{password}, email = #{email}, " +
            "real_name = #{realName}, enabled = #{enabled}, update_time = NOW() " +
            "WHERE id = #{id}")
    int update(User user);

    /**
     * 根据ID删除用户
     *
     * @param id 主键ID
     * @return 影响行数
     */
    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 根据用户名删除用户
     *
     * @param username 用户名
     * @return 影响行数
     */
    @Delete("DELETE FROM user WHERE username = #{username}")
    int deleteByUsername(@Param("username") String username);
}