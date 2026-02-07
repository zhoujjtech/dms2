package com.example.dms2.application.service;

import com.example.dms2.api.dto.PageRequest;
import com.example.dms2.api.dto.PageResponse;
import com.example.dms2.api.dto.request.CreateUserRequest;
import com.example.dms2.api.dto.response.UserDTO;
import com.example.dms2.application.assembler.UserAssembler;
import com.example.dms2.domain.exception.BusinessException;
import com.example.dms2.domain.model.entity.User;
import com.example.dms2.domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户应用服务 编排用例，协调领域对象和仓储
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAppService {

  private final UserRepository userRepository;
  private final UserAssembler userAssembler;

  /** 根据ID查询用户 */
  public UserDTO getUserById(Long id) {
    log.info("查询用户: id={}", id);
    User user =
        userRepository.findById(id).orElseThrow(() -> new BusinessException("用户不存在: id=" + id));
    return userAssembler.toDTO(user);
  }

  /** 创建用户 */
  @Transactional
  public UserDTO createUser(CreateUserRequest request) {
    log.info("创建用户: request={}", request);

    // 业务校验
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new BusinessException("用户名已存在: " + request.getUsername());
    }
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException("邮箱已存在: " + request.getEmail());
    }

    // 转换为领域实体
    User user = userAssembler.toEntity(request);
    user.validateForCreate();

    // 保存
    User savedUser = userRepository.save(user);

    log.info("用户创建成功: id={}", savedUser.getId());
    return userAssembler.toDTO(savedUser);
  }

  /** 批量查询用户 */
  public List<UserDTO> getUsersByIds(List<Long> ids) {
    log.info("批量查询用户: ids={}", ids);
    return ids.stream()
        .map(userRepository::findById)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(userAssembler::toDTO)
        .collect(Collectors.toList());
  }

  /** 分页查询用户 */
  public PageResponse<UserDTO> queryUsers(PageRequest pageRequest) {
    log.info("分页查询用户: pageRequest={}", pageRequest);
    pageRequest.validate();

    List<User> allUsers = userRepository.findAll();
    int total = allUsers.size();

    // 简单内存分页（实际项目应使用数据库分页）
    int from = pageRequest.getOffset();
    int to = Math.min(from + pageRequest.getPageSize(), allUsers.size());

    List<UserDTO> userDTOs =
        allUsers.subList(from, to).stream().map(userAssembler::toDTO).collect(Collectors.toList());

    return PageResponse.of(pageRequest, userDTOs, total);
  }

  /** 删除用户 */
  @Transactional
  public void deleteUser(Long id) {
    log.info("删除用户: id={}", id);
    if (!userRepository.findById(id).isPresent()) {
      throw new BusinessException("用户不存在: id=" + id);
    }
    userRepository.deleteById(id);
    log.info("用户删除成功: id={}", id);
  }
}
