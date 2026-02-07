package com.example.dms2.infrastructure.repository;

import com.example.dms2.domain.model.entity.User;
import com.example.dms2.domain.repository.UserRepository;
import com.example.dms2.infrastructure.mapper.UserMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * 用户仓储实现 使用 MyBatis 进行数据库操作
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

  private static final Logger log = LoggerFactory.getLogger(UserRepositoryImpl.class);

  private final UserMapper userMapper;

  @Override
  public Optional<User> findById(Long id) {
    return Optional.ofNullable(userMapper.selectById(id));
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return Optional.ofNullable(userMapper.selectByUsername(username));
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return Optional.ofNullable(userMapper.selectByEmail(email));
  }

  @Override
  public User save(User user) {
    if (user.getId() == null) {
      // 新增用户
      user.initCreateTime();
      userMapper.insert(user);
      log.info("保存新用户: id={}, username={}", user.getId(), user.getUsername());
    } else {
      // 更新用户
      user.updateUpdateTime();
      userMapper.updateById(user);
      log.info("更新用户: id={}, username={}", user.getId(), user.getUsername());
    }
    return user;
  }

  @Override
  public void deleteById(Long id) {
    log.info("删除用户: id={}", id);
    userMapper.deleteById(id);
  }

  @Override
  public List<User> findAll() {
    return userMapper.selectAll();
  }

  @Override
  public boolean existsByUsername(String username) {
    return userMapper.countByUsername(username) > 0;
  }

  @Override
  public boolean existsByEmail(String email) {
    return userMapper.countByEmail(email) > 0;
  }
}
