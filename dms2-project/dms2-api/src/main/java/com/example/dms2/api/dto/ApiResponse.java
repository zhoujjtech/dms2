package com.example.dms2.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应包装类
 *
 * @param <T> 数据类型
 * @author DMS2 Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一API响应")
public class ApiResponse<T> {

  @Schema(description = "响应码", example = "200")
  private Integer code;

  @Schema(description = "响应消息", example = "操作成功")
  private String message;

  @Schema(description = "响应数据")
  private T data;

  /** 成功响应（带数据） */
  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder().code(200).message("操作成功").data(data).build();
  }

  /** 成功响应（无数据） */
  public static <T> ApiResponse<T> success() {
    return success(null);
  }

  /** 成功响应（自定义消息） */
  public static <T> ApiResponse<T> success(String message, T data) {
    return ApiResponse.<T>builder().code(200).message(message).data(data).build();
  }

  /** 失败响应 */
  public static <T> ApiResponse<T> error(Integer code, String message) {
    return ApiResponse.<T>builder().code(code).message(message).build();
  }

  /** 失败响应（使用错误码枚举） */
  public static <T> ApiResponse<T> error(ErrorCode errorCode) {
    return error(errorCode.getCode(), errorCode.getMessage());
  }
}
