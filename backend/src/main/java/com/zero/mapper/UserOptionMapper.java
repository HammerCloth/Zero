package com.zero.mapper;

import com.zero.domain.UserOptionItem;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserOptionMapper {

  @Select("SELECT COUNT(*) FROM user_option_items WHERE user_id = #{userId}")
  int countByUser(String userId);

  @Select(
      "SELECT id, user_id as userId, dimension, opt_key as optKey, label, sort_order as sortOrder, "
          + "enabled as enabled FROM user_option_items WHERE user_id = #{userId} "
          + "ORDER BY dimension, sort_order ASC, opt_key ASC")
  List<UserOptionItem> listByUser(String userId);

  @Insert(
      "INSERT INTO user_option_items(id, user_id, dimension, opt_key, label, sort_order, enabled) "
          + "VALUES(#{id}, #{userId}, #{dimension}, #{optKey}, #{label}, #{sortOrder}, #{enabled})")
  int insert(UserOptionItem row);

  @Delete("DELETE FROM user_option_items WHERE user_id = #{userId} AND dimension = #{dimension}")
  int deleteByUserAndDimension(@Param("userId") String userId, @Param("dimension") String dimension);

  @Delete("DELETE FROM user_option_items WHERE user_id = #{userId}")
  int deleteAllForUser(String userId);
}
