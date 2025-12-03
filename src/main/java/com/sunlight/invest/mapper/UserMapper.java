package com.sunlight.invest.mapper;

import com.sunlight.invest.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
/**
 * 用户数据访问接口（MyBatis 注解版）
 */
public interface UserMapper {
    @Select("SELECT * FROM user")
    List<User> findAll();

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    @Insert("INSERT INTO user(name, email, age, create_time) VALUES(#{name}, #{email}, #{age}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE user SET name=#{name}, email=#{email}, age=#{age} WHERE id=#{id}")
    int update(User user);

    @Delete("DELETE FROM user WHERE id=#{id}")
    int deleteById(Long id);
}