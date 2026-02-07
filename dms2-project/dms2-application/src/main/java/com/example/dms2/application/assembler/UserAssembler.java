package com.example.dms2.application.assembler;

import com.example.dms2.api.dto.request.CreateUserRequest;
import com.example.dms2.api.dto.response.UserDTO;
import com.example.dms2.domain.model.entity.User;
import org.springframework.stereotype.Component;

/**
 * 用户DTO转换器 负责实体和DTO之间的转换
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@Component
public class UserAssembler {

  /** 实体转DTO */
  public UserDTO toDTO(User user) {
    if (user == null) {
      return null;
    }
    return UserDTO.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .phone(user.getPhone())
        .realName(user.getRealName())
        .createTime(user.getCreateTime())
        .updateTime(user.getUpdateTime())
        .build();
  }

  /** 创建请求转实体 */
  public User toEntity(CreateUserRequest request) {
    if (request == null) {
      return null;
    }
    return User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .phone(request.getPhone())
        .realName(request.getRealName())
        .build();
  }
}
