package com.zero.mapper;

import com.zero.domain.Account;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AccountMapper {

  @Insert(
      "INSERT INTO accounts(id, user_id, name, type, owner, sort_order, is_active) "
          + "VALUES(#{id}, #{userId}, #{name}, #{type}, #{owner}, #{sortOrder}, #{active})")
  int insert(Account account);

  @Select(
      "SELECT id, user_id as userId, name, type, owner, sort_order as sortOrder, "
          + "is_active as active, created_at as createdAt FROM accounts WHERE id = #{id}")
  Account findById(String id);

  @Select(
      "SELECT id, user_id as userId, name, type, owner, sort_order as sortOrder, "
          + "is_active as active, created_at as createdAt FROM accounts WHERE id = #{id} AND user_id = #{userId}")
  Account findByIdAndUser(@Param("id") String id, @Param("userId") String userId);

  @Select(
      "SELECT id, user_id as userId, name, type, owner, sort_order as sortOrder, "
          + "is_active as active, created_at as createdAt FROM accounts WHERE user_id = #{userId} AND is_active = 1 "
          + "ORDER BY sort_order ASC")
  List<Account> listActiveByUser(String userId);

  @Select(
      "SELECT id, user_id as userId, name, type, owner, sort_order as sortOrder, "
          + "is_active as active, created_at as createdAt FROM accounts WHERE user_id = #{userId} ORDER BY sort_order ASC")
  List<Account> listAllByUser(String userId);

  @Update(
      "UPDATE accounts SET name = #{name}, type = #{type}, owner = #{owner}, sort_order = #{sortOrder} WHERE id = #{id}")
  int update(Account account);

  @Update("UPDATE accounts SET is_active = 0 WHERE id = #{id}")
  int deactivate(String id);

  @Update("UPDATE accounts SET sort_order = #{order} WHERE id = #{id} AND user_id = #{userId}")
  int updateSort(@Param("id") String id, @Param("userId") String userId, @Param("order") int order);

  @Select("SELECT COUNT(*) FROM accounts WHERE user_id = #{userId} AND type = #{type}")
  int countByUserAndType(@Param("userId") String userId, @Param("type") String type);

  @Select("SELECT COUNT(*) FROM accounts WHERE user_id = #{userId} AND owner = #{owner}")
  int countByUserAndOwner(@Param("userId") String userId, @Param("owner") String owner);
}
