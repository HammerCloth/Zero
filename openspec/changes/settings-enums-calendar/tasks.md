## 1. 数据与迁移

- [x] 1.1 新增选项表（或等价 schema）及 Flyway 迁移，含 `user_id`、维度、`key`、`label`、`sort_order`、`enabled`
- [x] 1.2 实现默认种子：新用户或空维度时写入与当前硬编码等价的 `account_type` / `account_owner` / `event_category`
- [x] 1.3 为已有用户回填种子数据（迁移脚本或首次读时惰性插入）

## 2. 设置 API（后端）

- [x] 2.1 实现 `GET /api/v1/settings/options`（或分维度 GET）返回当前用户全部选项
- [x] 2.2 实现 `PUT`（或 PATCH）更新某维度选项列表，含 key 唯一性、格式校验、禁止删仍被引用的 key（或 409）
- [x] 2.3 实现「恢复默认」端点或在 PUT 中支持 reset 语义（与 proposal 一致）
- [x] 2.4 将 `AccountValidation` / `EventValidation` 改为读取当前用户选项（或注入服务）替代固定 `Set`

## 3. 快照按日 API（后端）

- [x] 3.1 实现 `GET .../snapshots/for-date?date=YYYY-MM-DD`（路径以现有 `/api/v1` 为准），返回有/无快照
- [x] 3.2 调整 `SnapshotService`/`EventService` 中大事记 category 校验与事件统计逻辑（支出仅计负数，见 event-api delta）

## 4. 前端：设置与 Store

- [x] 4.1 新增 `api/settings` 与 `settingsStore`（Pinia），登录后拉取并缓存选项
- [x] 4.2 新增设置页路由 `/settings` 与侧边栏/底部导航入口
- [x] 4.3 实现设置页 UI：三分组列表、编辑 label、排序、启用、新增 key、保存、恢复默认、错误提示

## 5. 前端：枚举消费与中文化

- [x] 5.1 `AccountsPage`：类型/归属下拉与标签使用 `settingsStore` 映射，移除硬编码英文展示
- [x] 5.2 `SnapshotFormPage`：大事记分类下拉使用配置；金额区增加收入/支出语义（控件或文案）并正确提交正负号
- [x] 5.3 `SnapshotDetailPage`：大事记表格展示分类中文标签与收支含义
- [x] 5.4 `EventStatsPage`：分类列使用 label；处理未知 key 降级展示

## 6. 前端：日期与日历

- [x] 6.1 `SnapshotFormPage`：日期改为 `n-date-picker`（或等价），与 `YYYY-MM-DD` 绑定
- [x] 6.2 新增快照日历视图页：月历、标记有快照日期、点击调用 `for-date` 后跳转详情/新建（带 `date` query）
- [x] 6.3 `SnapshotsPage`：增加进入日历视图的入口

## 7. 验证与文档

- [x] 7.1 手测：设置修改后账户/快照/大事记/统计页选项与标签一致（构建已通过；请在本地联调确认）
- [x] 7.2 手测：日历有/无快照跳转与预填日期（构建已通过；请在本地联调确认）
- [x] 7.3 更新 `docs/DEPLOYMENT.md` 或 README 中与枚举/设置相关的说明（若有用户可见行为变化）
