# event-api Specification

## Purpose

定义大事记（快照下事件）相关的只读统计 API。路径前缀为 **`/api/v1/events`**。分类 key 来自当前用户在设置中的 **`event_category`** 选项，而非固定死枚举（默认种子与历史文档中的 rent/travel 等一致）。

## Requirements

### Requirement: 大事记年度统计

系统 SHALL 提供 `GET /api/v1/events/stats` 返回指定年份按分类汇总的支出金额，以及按分类的**支出笔数**（仅统计支出口径的金额，通常为负数）。

#### Scenario: 当年

- **WHEN** `GET /api/v1/events/stats`
- **THEN** 返回 200，包含 `year`、`byCategory`（category → 金额合计）、`grandTotal`、`countByCategory`（category → 笔数）

#### Scenario: 指定年份

- **WHEN** `GET /api/v1/events/stats?year=2023`
- **THEN** `year` 为 2023，结构与上类似

#### Scenario: 无数据年份

- **WHEN** 该年无符合条件的大事记
- **THEN** 返回 200，`byCategory` / `countByCategory` 可为空映射，`grandTotal` 为 0

### Requirement: 统计口径

- `byCategory` / `grandTotal`：对**支出**侧金额汇总（实现上对支出金额取绝对值后按分类求和，与现有 `EventCategoryStat` 一致）。
- `countByCategory`：按分类统计**支出笔数**（例如 `amount < 0` 的条数），用于前端 Tooltip 等展示。

### Requirement: 分类合法性

大事记写入时的 `category` SHALL 校验为当前用户 **`event_category`** 维度下已启用选项的 `key`（见 `UserOptionService`）。
