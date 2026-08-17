package com.zero.mapper;

import com.zero.domain.GiftOccasionStat;
import com.zero.domain.GiftRecipient;
import com.zero.domain.GiftRecord;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GiftMapper {

  @Select("SELECT r.id, r.user_id AS userId, r.name, r.relationship, r.note, r.is_active AS active, "
      + "r.created_at AS createdAt, r.updated_at AS updatedAt, COUNT(g.id) AS giftCount, "
      + "COALESCE(SUM(g.amount), 0) AS giftTotal "
      + "FROM gift_recipients r LEFT JOIN gift_records g ON g.gift_recipient_id = r.id "
      + "WHERE r.user_id = #{userId} AND (#{includeInactive} = 1 OR r.is_active = 1) "
      + "GROUP BY r.id ORDER BY r.is_active DESC, r.name COLLATE NOCASE ASC")
  List<GiftRecipient> listRecipients(
      @Param("userId") String userId, @Param("includeInactive") boolean includeInactive);

  @Select("SELECT id, user_id AS userId, name, relationship, note, is_active AS active, "
      + "created_at AS createdAt, updated_at AS updatedAt FROM gift_recipients "
      + "WHERE id = #{id} AND user_id = #{userId}")
  GiftRecipient findRecipient(@Param("id") String id, @Param("userId") String userId);

  @Insert("INSERT INTO gift_recipients(id, user_id, name, relationship, note, is_active) "
      + "VALUES(#{id}, #{userId}, #{name}, #{relationship}, #{note}, #{active})")
  int insertRecipient(GiftRecipient recipient);

  @Update("UPDATE gift_recipients SET name = #{name}, relationship = #{relationship}, note = #{note}, "
      + "updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND user_id = #{userId}")
  int updateRecipient(GiftRecipient recipient);

  @Update("UPDATE gift_recipients SET is_active = 0, updated_at = CURRENT_TIMESTAMP "
      + "WHERE id = #{id} AND user_id = #{userId}")
  int deactivateRecipient(@Param("id") String id, @Param("userId") String userId);

  @Select("SELECT g.id, g.user_id AS userId, g.gift_recipient_id AS giftRecipientId, "
      + "r.name AS recipientName, r.relationship AS recipientRelationship, g.occasion, "
      + "g.gift_date AS giftDate, g.amount, g.payment_method AS paymentMethod, g.note, "
      + "g.created_at AS createdAt, g.updated_at AS updatedAt "
      + "FROM gift_records g INNER JOIN gift_recipients r ON r.id = g.gift_recipient_id "
      + "WHERE g.user_id = #{userId} "
      + "AND (#{year} IS NULL OR substr(g.gift_date, 1, 4) = #{year}) "
      + "AND (#{recipientId} IS NULL OR g.gift_recipient_id = #{recipientId}) "
      + "AND (#{keyword} IS NULL OR r.name LIKE '%' || #{keyword} || '%' OR g.occasion LIKE '%' || #{keyword} || '%' "
      + "OR COALESCE(g.note, '') LIKE '%' || #{keyword} || '%') "
      + "ORDER BY g.gift_date DESC, g.created_at DESC")
  List<GiftRecord> listRecords(
      @Param("userId") String userId,
      @Param("year") String year,
      @Param("recipientId") String recipientId,
      @Param("keyword") String keyword);

  @Select("SELECT g.id, g.user_id AS userId, g.gift_recipient_id AS giftRecipientId, "
      + "r.name AS recipientName, r.relationship AS recipientRelationship, g.occasion, "
      + "g.gift_date AS giftDate, g.amount, g.payment_method AS paymentMethod, g.note, "
      + "g.created_at AS createdAt, g.updated_at AS updatedAt "
      + "FROM gift_records g INNER JOIN gift_recipients r ON r.id = g.gift_recipient_id "
      + "WHERE g.id = #{id} AND g.user_id = #{userId}")
  GiftRecord findRecord(@Param("id") String id, @Param("userId") String userId);

  @Insert("INSERT INTO gift_records(id, user_id, gift_recipient_id, occasion, gift_date, amount, payment_method, note) "
      + "VALUES(#{id}, #{userId}, #{giftRecipientId}, #{occasion}, #{giftDate}, #{amount}, #{paymentMethod}, #{note})")
  int insertRecord(GiftRecord record);

  @Update("UPDATE gift_records SET gift_recipient_id = #{giftRecipientId}, occasion = #{occasion}, "
      + "gift_date = #{giftDate}, amount = #{amount}, payment_method = #{paymentMethod}, note = #{note}, "
      + "updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND user_id = #{userId}")
  int updateRecord(GiftRecord record);

  @Delete("DELETE FROM gift_records WHERE id = #{id} AND user_id = #{userId}")
  int deleteRecord(@Param("id") String id, @Param("userId") String userId);

  @Select("SELECT occasion, COALESCE(SUM(amount), 0) AS total, COUNT(*) AS count "
      + "FROM gift_records WHERE user_id = #{userId} AND substr(gift_date, 1, 4) = #{year} "
      + "GROUP BY occasion ORDER BY total DESC, occasion")
  List<GiftOccasionStat> statsByOccasion(@Param("userId") String userId, @Param("year") String year);

  @Select("SELECT COUNT(DISTINCT gift_recipient_id) FROM gift_records "
      + "WHERE user_id = #{userId} AND substr(gift_date, 1, 4) = #{year}")
  int countRecipientsForYear(@Param("userId") String userId, @Param("year") String year);
}
