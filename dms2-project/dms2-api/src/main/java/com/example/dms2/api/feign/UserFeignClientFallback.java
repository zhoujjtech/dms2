package com.example.dms2.api.feign;

import com.example.dms2.api.dto.ApiResponse;
import com.example.dms2.api.dto.ErrorCode;
import com.example.dms2.api.dto.PageRequest;
import com.example.dms2.api.dto.PageResponse;
import com.example.dms2.api.dto.request.CreateUserRequest;
import com.example.dms2.api.dto.response.UserDTO;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户Feign Client降级实现 当服务调用失败时执行
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class UserFeignClientFallback implements UserFeignClient {

  @Override
  public ApiResponse<UserDTO> getUserById(Long id) {
    log.error("Feign调用失败: getUserById, id={}", id);
    return ApiResponse.error(ErrorCode.FEIGN_CALL_ERROR);
  }

  @Override
  public ApiResponse<UserDTO> createUser(CreateUserRequest request) {
    log.error("Feign调用失败: createUser, request={}", request);
    return ApiResponse.error(ErrorCode.FEIGN_CALL_ERROR);
  }

  @Override
  public ApiResponse<List<UserDTO>> getUsersByIds(List<Long> ids) {
    log.error("Feign调用失败: getUsersByIds, ids={}", ids);
    return ApiResponse.error(ErrorCode.FEIGN_CALL_ERROR);
  }

  @Override
  public ApiResponse<PageResponse<UserDTO>> queryUsers(PageRequest pageRequest) {
    log.error("Feign调用失败: queryUsers, pageRequest={}", pageRequest);
    return ApiResponse.error(ErrorCode.FEIGN_CALL_ERROR);
  }

  @Override
  public ApiResponse<Void> deleteUser(Long id) {
    log.error("Feign调用失败: deleteUser, id={}", id);
    return ApiResponse.error(ErrorCode.FEIGN_CALL_ERROR);
  }
}
