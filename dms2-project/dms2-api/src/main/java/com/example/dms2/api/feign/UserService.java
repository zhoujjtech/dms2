package com.example.dms2.api.feign;

import com.example.dms2.api.dto.ApiResponse;
import com.example.dms2.api.dto.PageRequest;
import com.example.dms2.api.dto.PageResponse;
import com.example.dms2.api.dto.request.CreateUserRequest;
import com.example.dms2.api.dto.response.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户服务接口 定义用户相关的业务能力
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@Tag(name = "用户管理", description = "用户相关接口")
public interface UserService {

  @Operation(summary = "根据ID查询用户")
  @GetMapping("/{id}")
  ApiResponse<UserDTO> getUserById(@PathVariable("id") Long id);

  @Operation(summary = "创建用户")
  @PostMapping
  ApiResponse<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request);

  @Operation(summary = "批量查询用户")
  @PostMapping("/batch")
  ApiResponse<List<UserDTO>> getUsersByIds(@RequestBody List<Long> ids);

  @Operation(summary = "分页查询用户")
  @PostMapping("/page")
  ApiResponse<PageResponse<UserDTO>> queryUsers(@RequestBody PageRequest pageRequest);

  @Operation(summary = "删除用户")
  @DeleteMapping("/{id}")
  ApiResponse<Void> deleteUser(@PathVariable("id") Long id);
}
