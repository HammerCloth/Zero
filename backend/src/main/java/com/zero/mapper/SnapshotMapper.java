package com.zero.mapper;

import com.zero.domain.EventCategoryCount;
import com.zero.domain.EventCategoryStat;
import com.zero.domain.Snapshot;
import com.zero.domain.SnapshotEvent;
import com.zero.domain.SnapshotItem;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SnapshotMapper {

  @Insert(
      "INSERT INTO snapshots(id, user_id, date, note, created_by) VALUES(#{id}, #{userId}, #{date}, #{note}, #{createdBy})")
  int insertSnapshot(Snapshot snapshot);

  @Update("UPDATE snapshots SET date = #{date}, note = #{note} WHERE id = #{id} AND user_id = #{userId}")
  int updateSnapshot(Snapshot snapshot);

  @Delete("DELETE FROM snapshots WHERE id = #{id} AND user_id = #{userId}")
  int deleteSnapshot(@Param("id") String id, @Param("userId") String userId);

  @Select(
      "SELECT id, user_id as userId, date, note, created_at as createdAt, created_by as createdBy "
          + "FROM snapshots WHERE id = #{id}")
  Snapshot findSnapshotById(String id);

  @Select(
      "SELECT id, user_id as userId, date, note, created_at as createdAt, created_by as createdBy "
          + "FROM snapshots WHERE user_id = #{userId} ORDER BY date DESC, created_at DESC")
  List<Snapshot> listSnapshotsByUser(String userId);

  @Select(
      "SELECT id, user_id as userId, date, note, created_at as createdAt, created_by as createdBy "
          + "FROM snapshots WHERE user_id = #{userId} ORDER BY date DESC, created_at DESC LIMIT 1")
  Snapshot findLatestSnapshot(String userId);

  @Select("SELECT COUNT(*) FROM snapshots WHERE user_id = #{userId} AND date = #{date}")
  int countByUserAndDate(@Param("userId") String userId, @Param("date") String date);

  @Select(
      "SELECT COUNT(*) FROM snapshots WHERE user_id = #{userId} AND date = #{date} AND id != #{excludeId}")
  int countByUserAndDateExcluding(
      @Param("userId") String userId, @Param("date") String date, @Param("excludeId") String excludeId);

  @Insert(
      "INSERT INTO snapshot_items(id, snapshot_id, account_id, balance) VALUES(#{id}, #{snapshotId}, #{accountId}, #{balance})")
  int insertItem(SnapshotItem item);

  @Delete("DELETE FROM snapshot_items WHERE snapshot_id = #{snapshotId}")
  int deleteItemsForSnapshot(String snapshotId);

  @Select(
      "SELECT id, snapshot_id as snapshotId, account_id as accountId, balance FROM snapshot_items WHERE snapshot_id = #{snapshotId}")
  List<SnapshotItem> listItems(String snapshotId);

  @Insert(
      "INSERT INTO events(id, snapshot_id, category, description, amount) VALUES(#{id}, #{snapshotId}, #{category}, #{description}, #{amount})")
  int insertEvent(SnapshotEvent event);

  @Delete("DELETE FROM events WHERE snapshot_id = #{snapshotId}")
  int deleteEventsForSnapshot(String snapshotId);

  @Select(
      "SELECT id, snapshot_id as snapshotId, category, description, amount, created_at as createdAt FROM events WHERE snapshot_id = #{snapshotId}")
  List<SnapshotEvent> listEvents(String snapshotId);

  @Select(
      "SELECT e.category AS category, "
          + "COALESCE(SUM(CASE WHEN e.amount < 0 THEN ABS(e.amount) ELSE 0 END), 0) AS total "
          + "FROM events e INNER JOIN snapshots s ON e.snapshot_id = s.id "
          + "WHERE s.user_id = #{userId} AND substr(s.date, 1, 4) = #{year} GROUP BY e.category")
  List<EventCategoryStat> sumEventsByCategoryForYear(@Param("userId") String userId, @Param("year") String year);

  @Select(
      "SELECT e.category AS category, COUNT(*) AS cnt FROM events e INNER JOIN snapshots s ON e.snapshot_id = s.id "
          + "WHERE s.user_id = #{userId} AND substr(s.date, 1, 4) = #{year} AND e.amount < 0 GROUP BY e.category")
  List<EventCategoryCount> countExpenseEventsByCategoryForYear(
      @Param("userId") String userId, @Param("year") String year);

  @Select("SELECT id FROM snapshots WHERE user_id = #{userId} AND date = #{date} LIMIT 1")
  String findSnapshotIdByUserAndDate(@Param("userId") String userId, @Param("date") String date);

  @Select(
      "SELECT date FROM snapshots WHERE user_id = #{userId} AND date >= #{from} AND date <= #{to} ORDER BY date")
  List<String> listSnapshotDatesBetween(
      @Param("userId") String userId, @Param("from") String from, @Param("to") String to);

  @Select(
      "SELECT COUNT(*) FROM events e INNER JOIN snapshots s ON e.snapshot_id = s.id "
          + "WHERE s.user_id = #{userId} AND e.category = #{category}")
  int countEventsByUserAndCategory(@Param("userId") String userId, @Param("category") String category);
}
