package com.sunlight.invest.system.mapper;

import com.sunlight.invest.system.entity.SystemConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 系统配置Mapper接口
 *
 * @author System
 * @since 2024-12-04
 */
@Mapper
public interface SystemConfigMapper {

    /**
     * 插入系统配置记录
     *
     * @param systemConfig 系统配置对象
     * @return 影响行数
     */
    @Insert("INSERT INTO system_config (config_key, config_value, description, enabled, create_time, update_time) " +
            "VALUES (#{configKey}, #{configValue}, #{description}, #{enabled}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SystemConfig systemConfig);

    /**
     * 根据ID查询系统配置
     *
     * @param id 主键ID
     * @return 系统配置对象
     */
    @Select("SELECT * FROM system_config WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "configKey", column = "config_key"),
            @Result(property = "configValue", column = "config_value"),
            @Result(property = "description", column = "description"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    SystemConfig selectById(@Param("id") Long id);

    /**
     * 根据配置键查询系统配置
     *
     * @param configKey 配置键
     * @return 系统配置对象
     */
    @Select("SELECT * FROM system_config WHERE config_key = #{configKey}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "configKey", column = "config_key"),
            @Result(property = "configValue", column = "config_value"),
            @Result(property = "description", column = "description"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    SystemConfig selectByConfigKey(@Param("configKey") String configKey);

    /**
     * 查询所有启用的系统配置
     *
     * @return 系统配置列表
     */
    @Select("SELECT * FROM system_config WHERE enabled = 1 ORDER BY create_time ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "configKey", column = "config_key"),
            @Result(property = "configValue", column = "config_value"),
            @Result(property = "description", column = "description"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<SystemConfig> selectAllEnabled();

    /**
     * 查询所有系统配置
     *
     * @return 系统配置列表
     */
    @Select("SELECT * FROM system_config ORDER BY create_time ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "configKey", column = "config_key"),
            @Result(property = "configValue", column = "config_value"),
            @Result(property = "description", column = "description"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<SystemConfig> selectAll();

    /**
     * 更新系统配置
     *
     * @param systemConfig 系统配置对象
     * @return 影响行数
     */
    @Update("UPDATE system_config SET config_value = #{configValue}, " +
            "description = #{description}, enabled = #{enabled}, update_time = NOW() " +
            "WHERE id = #{id}")
    int update(SystemConfig systemConfig);

    /**
     * 根据ID删除系统配置
     *
     * @param id 主键ID
     * @return 影响行数
     */
    @Delete("DELETE FROM system_config WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 根据配置键删除系统配置
     *
     * @param configKey 配置键
     * @return 影响行数
     */
    @Delete("DELETE FROM system_config WHERE config_key = #{configKey}")
    int deleteByConfigKey(@Param("configKey") String configKey);
}