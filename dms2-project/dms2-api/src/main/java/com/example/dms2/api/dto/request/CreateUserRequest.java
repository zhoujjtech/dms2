package com.example.dms2.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建用户请求DTO
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建用户请求")
public class CreateUserRequest {

  @Schema(description = "用户名", example = "alice", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "用户名不能为空")
  @Size(min = 2, max = 50, message = "用户名长度必须在2-50之间")
  private String username;

  @Schema(
      description = "邮箱",
      example = "alice@example.com",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "邮箱不能为空")
  @Email(message = "邮箱格式不正确")
  private String email;

  @Schema(description = "手机号", example = "13800138000")
  private String phone;

  @Schema(description = "真实姓名", example = "Alice")
  private String realName;
}
