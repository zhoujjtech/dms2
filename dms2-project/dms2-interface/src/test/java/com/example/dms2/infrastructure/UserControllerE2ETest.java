package com.example.dms2.infrastructure;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.dms2.api.dto.PageRequest;
import com.example.dms2.api.dto.request.CreateUserRequest;
import com.example.dms2.api.dto.response.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * UserController端到端测试
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("UserController E2E测试")
class UserControllerE2ETest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private CreateUserRequest createUserRequest;

  @BeforeEach
  void setUp() {
    createUserRequest =
        CreateUserRequest.builder()
            .username("testuser")
            .email("test@example.com")
            .phone("13900139000")
            .realName("Test User")
            .build();
  }

  @Test
  @DisplayName("POST /api/users - 创建用户成功")
  void testCreateUserSuccess() throws Exception {
    // When & Then
    mockMvc
        .perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("用户创建成功"))
        .andExpect(jsonPath("$.data.username").value("testuser"))
        .andExpect(jsonPath("$.data.email").value("test@example.com"))
        .andExpect(jsonPath("$.data.id").exists())
        .andExpect(jsonPath("$.data.createTime").exists());
  }

  @Test
  @DisplayName("POST /api/users - 用户名已存在")
  void testCreateUserUsernameExists() throws Exception {
    // Given - 先创建一个用户
    mockMvc
        .perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
        .andReturn();

    // When & Then - 尝试创建相同用户名的用户
    mockMvc
        .perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(not(200)))
        .andExpect(jsonPath("$.message").value(containsString("用户名已存在")));
  }

  @Test
  @DisplayName("POST /api/users - 参数校验失败")
  void testCreateUserValidationFailed() throws Exception {
    // Given - 用户名为空
    createUserRequest.setUsername("");

    // When & Then
    mockMvc
        .perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /api/users/{id} - 查询用户成功")
  void testGetUserByIdSuccess() throws Exception {
    // Given - 先创建一个用户
    String response =
        mockMvc
            .perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createUserRequest)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    UserDTO createdUser =
        objectMapper.readValue(
            objectMapper.readTree(response).get("data").toString(), UserDTO.class);

    // When & Then
    mockMvc
        .perform(get("/api/users/" + createdUser.getId()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.id").value(createdUser.getId()))
        .andExpect(jsonPath("$.data.username").value("testuser"));
  }

  @Test
  @DisplayName("GET /api/users/{id} - 用户不存在")
  void testGetUserByIdNotFound() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/api/users/99999"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(not(200)))
        .andExpect(jsonPath("$.message").value(containsString("用户不存在")));
  }

  @Test
  @DisplayName("POST /api/users/batch - 批量查询用户成功")
  void testGetUsersByIdsSuccess() throws Exception {
    // Given - 先创建两个用户
    CreateUserRequest user1 =
        CreateUserRequest.builder().username("user1").email("user1@example.com").build();
    CreateUserRequest user2 =
        CreateUserRequest.builder().username("user2").email("user2@example.com").build();

    mockMvc.perform(
        post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(user1)));

    mockMvc.perform(
        post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(user2)));

    // When & Then
    mockMvc
        .perform(
            post("/api/users/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(1L, 2L, 3L))))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));
  }

  @Test
  @DisplayName("POST /api/users/page - 分页查询成功")
  void testQueryUsersSuccess() throws Exception {
    // Given - 创建几个用户
    for (int i = 1; i <= 5; i++) {
      CreateUserRequest request =
          CreateUserRequest.builder()
              .username("user" + i)
              .email("user" + i + "@example.com")
              .build();
      mockMvc.perform(
          post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)));
    }

    PageRequest pageRequest = new PageRequest();
    pageRequest.setPageNum(1);
    pageRequest.setPageSize(3);

    // When & Then
    mockMvc
        .perform(
            post("/api/users/page")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageRequest)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.pageNum").value(1))
        .andExpect(jsonPath("$.data.pageSize").value(3))
        .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(5)))
        .andExpect(jsonPath("$.data.records").isArray())
        .andExpect(jsonPath("$.data.records", hasSize(3)));
  }

  @Test
  @DisplayName("DELETE /api/users/{id} - 删除用户成功")
  void testDeleteUserSuccess() throws Exception {
    // Given - 先创建一个用户
    String response =
        mockMvc
            .perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createUserRequest)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    UserDTO createdUser =
        objectMapper.readValue(
            objectMapper.readTree(response).get("data").toString(), UserDTO.class);

    // When & Then
    mockMvc
        .perform(delete("/api/users/" + createdUser.getId()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("用户删除成功"));

    // 验证用户已删除
    mockMvc
        .perform(get("/api/users/" + createdUser.getId()))
        .andExpect(jsonPath("$.message").value(containsString("用户不存在")));
  }

  @Test
  @DisplayName("DELETE /api/users/{id} - 删除不存在的用户")
  void testDeleteUserNotFound() throws Exception {
    // When & Then
    mockMvc
        .perform(delete("/api/users/99999"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(not(200)))
        .andExpect(jsonPath("$.message").value(containsString("用户不存在")));
  }
}
