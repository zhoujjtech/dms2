package com.example.dms2.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户DTO
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息")
public class UserDTO {

  @Schema(description = "用户ID", example = "1")
  private Long id;

  @Schema(description = "用户名", example = "alice")
  private String username;

  @Schema(description = "邮箱", example = "alice@example.com")
  private String email;

  @Schema(description = "手机号", example = "13800138000")
  private String phone;

  @Schema(description = "真实姓名", example = "Alice")
  private String realName;

  @Schema(description = "创建时间", example = "2024-01-01T12:00:00")
  private LocalDateTime createTime;

  @Schema(description = "更新时间", example = "2024-01-01T12:00:00")
  private LocalDateTime updateTime;
}
