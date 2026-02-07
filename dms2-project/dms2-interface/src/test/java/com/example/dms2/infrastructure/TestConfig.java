package com.example.dms2.infrastructure;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 测试配置类
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@TestConfiguration
@ComponentScan(
    basePackages = {
      "com.example.dms2.api",
      "com.example.dms2.domain",
      "com.example.dms2.application",
      "com.example.dms2.infrastructure"
    })
public class TestConfig {
  // 测试专用的Bean配置可以在这里添加
}
