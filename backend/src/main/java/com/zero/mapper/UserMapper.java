package com.zero.mapper;

import com.zero.domain.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

  @Select("SELECT COUNT(*) FROM users")
  int countUsers();

  @Select(
      "SELECT id, username, password_hash as passwordHash, is_admin as admin, "
          + "must_change_password as mustChangePassword, created_at as createdAt FROM users WHERE id = #{id}")
  User findById(String id);

  @Select(
      "SELECT id, username, password_hash as passwordHash, is_admin as admin, "
          + "must_change_password as mustChangePassword, created_at as createdAt FROM users WHERE username = #{username}")
  User findByUsername(String username);

  @Insert(
      "INSERT INTO users(id, username, password_hash, is_admin, must_change_password) "
          + "VALUES(#{id}, #{username}, #{passwordHash}, #{admin}, #{mustChangePassword})")
  int insert(User user);

  @Update(
      "UPDATE users SET password_hash = #{passwordHash}, must_change_password = #{mustChangePassword} WHERE id = #{id}")
  int updatePassword(
      @Param("id") String id,
      @Param("passwordHash") String passwordHash,
      @Param("mustChangePassword") boolean mustChangePassword);

  @Select(
      "SELECT id, username, password_hash as passwordHash, is_admin as admin, "
          + "must_change_password as mustChangePassword, created_at as createdAt FROM users ORDER BY created_at ASC")
  java.util.List<User> listAll();

  @Delete("DELETE FROM users WHERE id = #{id}")
  int deleteById(String id);
}
