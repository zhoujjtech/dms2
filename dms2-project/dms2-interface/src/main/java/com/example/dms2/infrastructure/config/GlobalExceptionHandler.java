package com.example.dms2.infrastructure.config;

import com.example.dms2.api.dto.ApiResponse;
import com.example.dms2.domain.exception.BusinessException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器 统一处理应用中的异常，返回标准的API响应
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** 处理业务异常 */
  @ExceptionHandler(BusinessException.class)
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<Void> handleBusinessException(BusinessException e) {
    log.warn("业务异常: {}", e.getMessage());
    return ApiResponse.error(e.getCode(), e.getMessage());
  }

  /** 处理参数校验异常 */
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
    log.warn("参数校验异常: {}", e.getMessage());
    return ApiResponse.error(400, e.getMessage());
  }

  /** 处理参数校验异常 (@Valid) */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    String errorMsg =
        e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));
    log.warn("参数校验失败: {}", errorMsg);
    return ApiResponse.error(400, errorMsg);
  }

  /** 处理参数绑定异常 */
  @ExceptionHandler(BindException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleBindException(BindException e) {
    String errorMsg =
        e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));
    log.warn("参数绑定失败: {}", errorMsg);
    return ApiResponse.error(400, errorMsg);
  }

  /** 处理请求体读取异常 */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e) {
    log.warn("请求体格式错误: {}", e.getMessage());
    return ApiResponse.error(400, "请求体格式错误");
  }

  /** 处理所有未捕获的异常 */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponse<Void> handleException(Exception e) {
    log.error("系统异常", e);
    return ApiResponse.error(500, "系统异常: " + e.getMessage());
  }
}
