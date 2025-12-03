package com.sunlight.invest.config;

import com.sunlight.invest.common.SqlCostInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 * <p>
 * 用于配置MyBatis相关组件
 * </p>
 *
 * @author System
 * @since 2024-12-03
 */
@Configuration
public class MyBatisConfig {

    @Bean
    public Interceptor sqlCostInterceptor() {
        return new SqlCostInterceptor();
    }
}