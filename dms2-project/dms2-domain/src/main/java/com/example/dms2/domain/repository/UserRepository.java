package com.example.dms2.domain.repository;

import com.example.dms2.domain.model.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口 定义用户数据的持久化操作
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
public interface UserRepository {

  /** 根据ID查询用户 */
  Optional<User> findById(Long id);

  /** 根据用户名查询用户 */
  Optional<User> findByUsername(String username);

  /** 根据邮箱查询用户 */
  Optional<User> findByEmail(String email);

  /** 保存用户 */
  User save(User user);

  /** 删除用户 */
  void deleteById(Long id);

  /** 查询所有用户 */
  List<User> findAll();

  /** 判断用户名是否存在 */
  boolean existsByUsername(String username);

  /** 判断邮箱是否存在 */
  boolean existsByEmail(String email);
}
