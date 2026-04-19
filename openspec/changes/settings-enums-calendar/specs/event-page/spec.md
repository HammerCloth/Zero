## MODIFIED Requirements

### Requirement: 分类标签

分类 SHALL 显示为当前用户在设置中为 `event_category` 配置的 `label`；若某历史 `key` 在设置中已删除或禁用，页面 SHALL 仍显示合理降级（例如显示 key 或「未知」）。

#### Scenario: 标签映射

- **WHEN** 统计行中的 category key 在设置中存在
- **THEN** 显示为该 key 对应的 `label`

#### Scenario: 未知 key

- **WHEN** category key 在设置中不存在
- **THEN** 页面 SHALL 显示可辨识的占位文案或原始 key
