## ADDED Requirements

### Requirement: 大事记年度统计
系统 SHALL 提供 GET /api/events/stats 端点返回指定年份按分类汇总的大事记支出。

#### Scenario: 获取当年统计
- **WHEN** GET /api/events/stats
- **THEN** 返回 200 和 `{"year": 2024, "byCategory": [{"category": "travel", "total": 15000, "count": 3}], "grandTotal": 50000}`

#### Scenario: 指定年份
- **WHEN** GET /api/events/stats?year=2023
- **THEN** 返回 2023 年的统计数据

#### Scenario: 无数据年份
- **WHEN** GET /api/events/stats?year=2020 该年无大事记
- **THEN** 返回 200 和 `{"year": 2020, "byCategory": [], "grandTotal": 0}`

### Requirement: 金额取绝对值
统计时 total SHALL 为该分类所有大事记金额绝对值之和（大事记金额存储为负数）。

#### Scenario: 金额处理
- **WHEN** 两条 travel 大事记金额为 -5000 和 -10000
- **THEN** byCategory 中 travel.total = 15000

### Requirement: 大事记分类
category SHALL 为以下枚举值之一：rent（房租）、travel（旅行）、medical（医疗）、appliance（家电装修）、social（人情往来）、other（其他）。

#### Scenario: 分类标签
- **WHEN** 前端显示分类
- **THEN** 应映射为中文标签
