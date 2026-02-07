package com.example.dms2.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.dms2.api.dto.PageRequest;
import com.example.dms2.api.dto.PageResponse;
import com.example.dms2.api.dto.request.CreateUserRequest;
import com.example.dms2.api.dto.response.UserDTO;
import com.example.dms2.application.assembler.UserAssembler;
import com.example.dms2.application.service.UserAppService;
import com.example.dms2.domain.exception.BusinessException;
import com.example.dms2.domain.model.entity.User;
import com.example.dms2.domain.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserAppService集成测试
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAppService集成测试")
class UserAppServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private UserAssembler userAssembler;

  @InjectMocks private UserAppService userAppService;

  private User testUser;
  private UserDTO testUserDTO;
  private CreateUserRequest createRequest;

  @BeforeEach
  void setUp() {
    // 创建测试数据
    testUser =
        User.builder()
            .id(1L)
            .username("alice")
            .email("alice@example.com")
            .phone("13800138000")
            .realName("Alice")
            .createTime(LocalDateTime.now())
            .updateTime(LocalDateTime.now())
            .build();

    testUserDTO =
        UserDTO.builder()
            .id(1L)
            .username("alice")
            .email("alice@example.com")
            .phone("13800138000")
            .realName("Alice")
            .createTime(LocalDateTime.now())
            .updateTime(LocalDateTime.now())
            .build();

    createRequest =
        CreateUserRequest.builder()
            .username("alice")
            .email("alice@example.com")
            .phone("13800138000")
            .realName("Alice")
            .build();
  }

  @Test
  @DisplayName("根据ID查询用户 - 成功")
  void testGetUserByIdSuccess() {
    // Given
    Long userId = 1L;
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(userAssembler.toDTO(testUser)).thenReturn(testUserDTO);

    // When
    UserDTO result = userAppService.getUserById(userId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(userId);
    assertThat(result.getUsername()).isEqualTo("alice");

    verify(userRepository, times(1)).findById(userId);
    verify(userAssembler, times(1)).toDTO(testUser);
  }

  @Test
  @DisplayName("根据ID查询用户 - 用户不存在")
  void testGetUserByIdNotFound() {
    // Given
    Long userId = 999L;
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userAppService.getUserById(userId))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("用户不存在");

    verify(userRepository, times(1)).findById(userId);
    verify(userAssembler, never()).toDTO(any());
  }

  @Test
  @DisplayName("创建用户 - 成功")
  void testCreateUserSuccess() {
    // Given
    when(userRepository.existsByUsername(createRequest.getUsername())).thenReturn(false);
    when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
    when(userAssembler.toEntity(createRequest)).thenReturn(testUser);
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(userAssembler.toDTO(testUser)).thenReturn(testUserDTO);

    // When
    UserDTO result = userAppService.createUser(createRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("alice");

    verify(userRepository, times(1)).existsByUsername(createRequest.getUsername());
    verify(userRepository, times(1)).existsByEmail(createRequest.getEmail());
    verify(userAssembler, times(1)).toEntity(createRequest);
    verify(userRepository, times(1)).save(any(User.class));
    verify(userAssembler, times(1)).toDTO(testUser);
  }

  @Test
  @DisplayName("创建用户 - 用户名已存在")
  void testCreateUserUsernameExists() {
    // Given
    when(userRepository.existsByUsername(createRequest.getUsername())).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> userAppService.createUser(createRequest))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("用户名已存在");

    verify(userRepository, times(1)).existsByUsername(createRequest.getUsername());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("创建用户 - 邮箱已存在")
  void testCreateUserEmailExists() {
    // Given
    when(userRepository.existsByUsername(createRequest.getUsername())).thenReturn(false);
    when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> userAppService.createUser(createRequest))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("邮箱已存在");

    verify(userRepository, times(1)).existsByUsername(createRequest.getUsername());
    verify(userRepository, times(1)).existsByEmail(createRequest.getEmail());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("批量查询用户 - 成功")
  void testGetUsersByIdsSuccess() {
    // Given
    List<Long> ids = Arrays.asList(1L, 2L, 3L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findById(2L)).thenReturn(Optional.empty());
    when(userRepository.findById(3L)).thenReturn(Optional.of(testUser));
    when(userAssembler.toDTO(testUser)).thenReturn(testUserDTO);

    // When
    List<UserDTO> result = userAppService.getUsersByIds(ids);

    // Then
    assertThat(result).hasSize(2); // 只有1和3存在

    verify(userRepository, times(1)).findById(1L);
    verify(userRepository, times(1)).findById(2L);
    verify(userRepository, times(1)).findById(3L);
  }

  @Test
  @DisplayName("分页查询用户 - 成功")
  void testQueryUsersSuccess() {
    // Given
    PageRequest pageRequest = new PageRequest();
    pageRequest.setPageNum(1);
    pageRequest.setPageSize(10);

    List<User> users = Arrays.asList(testUser, testUser);
    when(userRepository.findAll()).thenReturn(users);
    when(userAssembler.toDTO(any(User.class))).thenReturn(testUserDTO);

    // When
    PageResponse<UserDTO> result = userAppService.queryUsers(pageRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getPageNum()).isEqualTo(1);
    assertThat(result.getPageSize()).isEqualTo(10);
    assertThat(result.getTotal()).isEqualTo(2);
    assertThat(result.getRecords()).hasSize(2);

    verify(userRepository, times(1)).findAll();
    verify(userAssembler, times(2)).toDTO(any(User.class));
  }

  @Test
  @DisplayName("删除用户 - 成功")
  void testDeleteUserSuccess() {
    // Given
    Long userId = 1L;
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    doNothing().when(userRepository).deleteById(userId);

    // When
    assertThatCode(() -> userAppService.deleteUser(userId)).doesNotThrowAnyException();

    // Then
    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(1)).deleteById(userId);
  }

  @Test
  @DisplayName("删除用户 - 用户不存在")
  void testDeleteUserNotFound() {
    // Given
    Long userId = 999L;
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userAppService.deleteUser(userId))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("用户不存在");

    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, never()).deleteById(any());
  }
}
