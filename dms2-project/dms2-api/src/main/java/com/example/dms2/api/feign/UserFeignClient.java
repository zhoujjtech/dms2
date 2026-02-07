package com.example.dms2.api.feign;

import com.example.dms2.api.config.FeignClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 用户Feign Client 继承业务接口，添加@FeignClient注解
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@FeignClient(
    name = "${service.name.dms2-service:dms2-service}",
    path = "/api/users",
    configuration = FeignClientConfiguration.class,
    fallback = UserFeignClientFallback.class)
public interface UserFeignClient extends UserService {
  // 继承UserService接口的所有方法，无需重复定义
}
