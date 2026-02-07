package com.example.dms2.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * DMS2项目启动类
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.example.dms2.api.feign"})
@EnableCaching
@ComponentScan(
    basePackages = {
      "com.example.dms2.api",
      "com.example.dms2.domain",
      "com.example.dms2.application",
      "com.example.dms2.infrastructure"
    })
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
