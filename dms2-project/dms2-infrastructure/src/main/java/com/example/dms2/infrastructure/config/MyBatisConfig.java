package com.example.dms2.infrastructure.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置类
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@Configuration
@MapperScan("com.example.dms2.infrastructure.repository.mapper")
public class MyBatisConfig {
  // MyBatis 基本配置在 application.yml 中完成
  // 包括：mapper-locations、type-aliases-package、configuration 等
}
