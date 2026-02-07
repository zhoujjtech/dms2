package com.example.dms2.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

  // 成功
  SUCCESS(200, "操作成功"),

  // 客户端错误 (4xx)
  BAD_REQUEST(400, "请求参数错误"),
  UNAUTHORIZED(401, "未授权，请先登录"),
  FORBIDDEN(403, "无权限访问"),
  NOT_FOUND(404, "资源不存在"),
  METHOD_NOT_ALLOWED(405, "请求方法不支持"),
  VALIDATION_ERROR(422, "参数校验失败"),

  // 服务端错误 (5xx)
  INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
  SERVICE_UNAVAILABLE(503, "服务暂不可用"),
  GATEWAY_TIMEOUT(504, "网关超时"),

  // 业务错误码 (1xxx)
  USER_NOT_FOUND(1001, "用户不存在"),
  USER_ALREADY_EXISTS(1002, "用户已存在"),
  INVALID_PASSWORD(1003, "密码错误"),
  TOKEN_EXPIRED(1004, "令牌已过期"),
  TOKEN_INVALID(1005, "令牌无效"),

  // 外部服务错误 (2xxx)
  EXTERNAL_SERVICE_ERROR(2001, "外部服务调用失败"),
  FEIGN_CALL_ERROR(2002, "Feign调用失败");

  private final Integer code;
  private final String message;
}
