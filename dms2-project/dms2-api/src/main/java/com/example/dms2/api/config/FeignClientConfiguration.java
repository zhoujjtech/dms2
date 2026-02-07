package com.example.dms2.api.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign Client配置类
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@Configuration
public class FeignClientConfiguration {

  /**
   * Feign日志级别配置 NONE: 无日志 BASIC: 基本日志（请求方法、URL、响应状态码、执行时间） HEADERS: 包含请求头和响应头 FULL:
   * 完整日志（请求头、请求体、响应头、响应体）
   */
  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.BASIC;
  }

  /** Feign超时配置 连接超时：5秒 读取超时：30秒 */
  @Bean
  public Request.Options feignOptions() {
    return new Request.Options(
        5,
        TimeUnit.SECONDS, // 连接超时
        30,
        TimeUnit.SECONDS, // 读取超时
        true // 跟踪重定向
        );
  }

  /** Feign请求拦截器 可用于添加认证头、请求ID等 */
  @Bean
  public RequestInterceptor feignRequestInterceptor() {
    return template -> {
      // 添加请求ID（用于链路追踪）
      String requestId = java.util.UUID.randomUUID().toString();
      template.header("X-Request-Id", requestId);

      // 添加认证头（示例：Bearer Token）
      // String token = getToken();
      // if (token != null) {
      //     template.header("Authorization", "Bearer " + token);
      // }

      // 添加其他公共请求头
      template.header("Content-Type", "application/json");
    };
  }
}
