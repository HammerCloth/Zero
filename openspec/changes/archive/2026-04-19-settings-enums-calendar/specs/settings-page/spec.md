## ADDED Requirements

### Requirement: 设置页入口

前端 SHALL 在主导航中提供「设置」入口，路由 SHALL 指向设置页（例如 `/settings`），且 SHALL 要求用户已登录。

#### Scenario: 未登录访问

- **WHEN** 未登录用户访问设置页
- **THEN** 重定向到登录页（与现有路由守卫一致）

### Requirement: 选项维护界面

设置页 SHALL 分区域展示账户类型、账户归属、大事记分类三类（或等价分组），每区 SHALL 支持列出当前选项、编辑 `label`、调整顺序、启用/禁用，并 SHALL 支持新增选项（指定新 `key` 与 `label`）。

#### Scenario: 保存成功

- **WHEN** 用户修改选项并点击保存
- **THEN** 调用后端更新 API，成功 SHALL 显示成功提示并刷新列表

#### Scenario: 校验失败

- **WHEN** 后端返回 400/409
- **THEN** 页面 SHALL 展示错误信息且不丢失未保存编辑（在合理范围内）

### Requirement: 恢复默认选项

设置页 SHALL 提供「恢复默认」操作入口；用户确认后 SHALL 调用后端将各维度选项重置为默认种子集合。

#### Scenario: 确认恢复

- **WHEN** 用户完成二次确认
- **THEN** 选项列表与新建账户/大事记下拉 SHALL 与默认种子一致
