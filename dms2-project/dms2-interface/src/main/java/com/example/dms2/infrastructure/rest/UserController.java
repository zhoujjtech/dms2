package com.example.dms2.infrastructure.rest;

import com.example.dms2.api.dto.ApiResponse;
import com.example.dms2.api.dto.PageRequest;
import com.example.dms2.api.dto.PageResponse;
import com.example.dms2.api.dto.request.CreateUserRequest;
import com.example.dms2.api.dto.response.UserDTO;
import com.example.dms2.api.feign.UserService;
import com.example.dms2.application.service.UserAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户REST控制器 实现UserService接口，提供HTTP REST API
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController implements UserService {

  private final UserAppService userAppService;

  @Override
  @GetMapping("/{id}")
  @Operation(summary = "根据ID查询用户")
  public ApiResponse<UserDTO> getUserById(@PathVariable("id") Long id) {
    log.info("REST请求: 查询用户, id={}", id);
    UserDTO userDTO = userAppService.getUserById(id);
    return ApiResponse.success(userDTO);
  }

  @Override
  @PostMapping
  @Operation(summary = "创建用户")
  public ApiResponse<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
    log.info("REST请求: 创建用户, request={}", request);
    UserDTO userDTO = userAppService.createUser(request);
    return ApiResponse.success("用户创建成功", userDTO);
  }

  @Override
  @PostMapping("/batch")
  @Operation(summary = "批量查询用户")
  public ApiResponse<List<UserDTO>> getUsersByIds(@RequestBody List<Long> ids) {
    log.info("REST请求: 批量查询用户, ids={}", ids);
    List<UserDTO> userDTOs = userAppService.getUsersByIds(ids);
    return ApiResponse.success(userDTOs);
  }

  @Override
  @PostMapping("/page")
  @Operation(summary = "分页查询用户")
  public ApiResponse<PageResponse<UserDTO>> queryUsers(@RequestBody PageRequest pageRequest) {
    log.info("REST请求: 分页查询用户, pageRequest={}", pageRequest);
    PageResponse<UserDTO> pageResponse = userAppService.queryUsers(pageRequest);
    return ApiResponse.success(pageResponse);
  }

  @Override
  @DeleteMapping("/{id}")
  @Operation(summary = "删除用户")
  public ApiResponse<Void> deleteUser(@PathVariable("id") Long id) {
    log.info("REST请求: 删除用户, id={}", id);
    userAppService.deleteUser(id);
    return ApiResponse.success("用户删除成功", null);
  }
}
