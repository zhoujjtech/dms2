package com.example.dms2.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.dms2.domain.model.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * User领域实体单元测试
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@DisplayName("User领域实体测试")
class UserTest {

  @Test
  @DisplayName("创建用户时应该校验通过")
  void testCreateUserWithValidData() {
    // Given
    User user =
        User.builder()
            .id(1L)
            .username("alice")
            .email("alice@example.com")
            .phone("13800138000")
            .realName("Alice")
            .build();

    // When & Then
    assertThatCode(() -> user.validateForCreate()).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("创建用户时用户名为空应该抛出异常")
  void testCreateUserWithNullUsername() {
    // Given
    User user = User.builder().id(1L).username(null).email("alice@example.com").build();

    // When & Then
    assertThatThrownBy(() -> user.validateForCreate())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("用户名不能为空");
  }

  @Test
  @DisplayName("创建用户时用户名为空字符串应该抛出异常")
  void testCreateUserWithEmptyUsername() {
    // Given
    User user = User.builder().id(1L).username("  ").email("alice@example.com").build();

    // When & Then
    assertThatThrownBy(() -> user.validateForCreate())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("用户名不能为空");
  }

  @Test
  @DisplayName("创建用户时邮箱为空应该抛出异常")
  void testCreateUserWithNullEmail() {
    // Given
    User user = User.builder().id(1L).username("alice").email(null).build();

    // When & Then
    assertThatThrownBy(() -> user.validateForCreate())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("邮箱不能为空");
  }

  @Test
  @DisplayName("创建用户时邮箱格式不正确应该抛出异常")
  void testCreateUserWithInvalidEmail() {
    // Given
    User user = User.builder().id(1L).username("alice").email("invalid-email").build();

    // When & Then
    assertThatThrownBy(() -> user.validateForCreate())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("邮箱格式不正确");
  }

  @Test
  @DisplayName("初始化创建时间应该设置创建时间和更新时间")
  void testInitCreateTime() {
    // Given
    User user = User.builder().id(1L).username("alice").email("alice@example.com").build();

    // When
    user.initCreateTime();

    // Then
    assertThat(user.getCreateTime()).isNotNull();
    assertThat(user.getUpdateTime()).isNotNull();
    assertThat(user.getCreateTime()).isEqualTo(user.getUpdateTime());
  }

  @Test
  @DisplayName("更新时间应该修改更新时间字段")
  void testUpdateUpdateTime() throws InterruptedException {
    // Given
    User user =
        User.builder()
            .id(1L)
            .username("alice")
            .email("alice@example.com")
            .createTime(java.time.LocalDateTime.now())
            .updateTime(java.time.LocalDateTime.now())
            .build();

    // When
    Thread.sleep(10); // 确保时间不同
    user.updateUpdateTime();

    // Then
    assertThat(user.getUpdateTime()).isAfter(user.getCreateTime());
  }
}
