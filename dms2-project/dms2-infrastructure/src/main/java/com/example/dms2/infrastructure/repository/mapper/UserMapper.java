package com.example.dms2.infrastructure.repository.mapper;

import com.example.dms2.domain.model.entity.User;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper 接口
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@Mapper
public interface UserMapper {

  /**
   * 根据 ID 查询用户
   *
   * @param id 用户 ID
   * @return 用户实体
   */
  User selectById(@Param("id") Long id);

  /**
   * 根据用户名查询用户
   *
   * @param username 用户名
   * @return 用户实体
   */
  User selectByUsername(@Param("username") String username);

  /**
   * 根据邮箱查询用户
   *
   * @param email 邮箱
   * @return 用户实体
   */
  User selectByEmail(@Param("email") String email);

  /**
   * 查询所有用户
   *
   * @return 用户列表
   */
  List<User> selectAll();

  /**
   * 根据 ID 列表查询用户
   *
   * @param ids 用户 ID 列表
   * @return 用户列表
   */
  List<User> selectByIds(@Param("ids") List<Long> ids);

  /**
   * 插入用户
   *
   * @param user 用户实体
   * @return 影响行数
   */
  int insert(User user);

  /**
   * 根据 ID 更新用户
   *
   * @param user 用户实体
   * @return 影响行数
   */
  int updateById(User user);

  /**
   * 根据 ID 删除用户
   *
   * @param id 用户 ID
   * @return 影响行数
   */
  int deleteById(@Param("id") Long id);

  /**
   * 统计用户名数量
   *
   * @param username 用户名
   * @return 数量
   */
  int countByUsername(@Param("username") String username);

  /**
   * 统计邮箱数量
   *
   * @param email 邮箱
   * @return 数量
   */
  int countByEmail(@Param("email") String email);

  /**
   * 分页查询用户
   *
   * @param offset 偏移量
   * @param limit 限制数量
   * @return 用户列表
   */
  List<User> selectByPage(@Param("offset") int offset, @Param("limit") int limit);
}
