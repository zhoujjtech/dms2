package com.example.dms2.domain.model.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户领域实体
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

  private Long id;

  private String username;

  private String email;

  private String phone;

  private String realName;

  private LocalDateTime createTime;

  private LocalDateTime updateTime;

  /** 业务校验：创建用户时调用 */
  public void validateForCreate() {
    if (username == null || username.trim().isEmpty()) {
      throw new IllegalArgumentException("用户名不能为空");
    }
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("邮箱不能为空");
    }
    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
      throw new IllegalArgumentException("邮箱格式不正确");
    }
  }

  /** 业务方法：初始化创建时间 */
  public void initCreateTime() {
    LocalDateTime now = LocalDateTime.now();
    this.createTime = now;
    this.updateTime = now;
  }

  /** 业务方法：更新时间 */
  public void updateUpdateTime() {
    this.updateTime = LocalDateTime.now();
  }
}
