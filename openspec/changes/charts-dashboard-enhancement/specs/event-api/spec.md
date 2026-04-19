## ADDED Requirements

### Requirement: 统计笔数字段

若后端实现分类笔数统计，`GET /api/v1/events/stats` 响应 SHALL 包含 `countByCategory` 对象，键为分类 key，值为该年该分类大事记条数（非负整数）。

#### Scenario: 笔数与金额一致

- **WHEN** 某分类有两条支出记录
- **THEN** `countByCategory` 中该 key 对应值为 2，且 `byCategory` 中金额 SHALL 与现有支出汇总口径一致
