package com.example.dms2.domain.exception;

/**
 * 业务异常 用于标识业务逻辑错误，如用户不存在、用户名已存在等
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {

  private final int code;

  public BusinessException(String message) {
    super(message);
    this.code = 400;
  }

  public BusinessException(int code, String message) {
    super(message);
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
