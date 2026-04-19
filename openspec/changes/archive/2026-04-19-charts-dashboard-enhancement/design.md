## Context

前端已使用 ECharts 6 + vue-echarts；仪表盘 API 已实现 `summary`、`trend`、`composition`、`monthly-growth`。设置中心已提供 `account_type` / `account_owner` / `event_category` 的 key→label。大事记 `stats` 返回 `byCategory` 映射与 `grandTotal`。

## Goals / Non-Goals

**Goals:**

- P1：大事记页在**不削弱表格**的前提下增加图表，数据优先复用 `GET /api/v1/events/stats`。
- P2：总览图表视觉与格式统一（环形/分色/货币格式）。
- P3：构成饼图扇区名称使用 **settings 的 label**（未知 key 显示 key 或「未知」）。
- P4：月度增长增加 **年内累计变化** 序列：`cumulative[m] = sum(change[1..m])`（1 月为第一个月增量，累计从该月起滚动）。
- P5：按账户余额时间序列：对每个**活跃账户**，按快照日期升序，取该快照中 `snapshot_items` 对应余额；若某快照缺该账户行（历史数据不应出现，若出现则跳过或填 null，实现时选**与前一条快照同账户余额向前填充**或 0——本设计选：**仅使用该行存在时的点，折线不连续处断开** 或 **使用上一快照余额向前填充**；推荐 **向前填充** 以得到连续曲线，与「每月都填全账户」的产品假设一致）。

**Non-Goals:**

- 实时行情、预测线、机器学习。
- 导出 PDF 报表。
- 替换现有 CSV 导出语义。

## Decisions

**1. 堆叠面积（按类型时间序列）**

- **做法**：在 `range` 与全局 `trend` 相同的快照集合上，对每个快照计算 `byType`（与 `composition` 相同算法，按最新逻辑扩展到每个历史快照）。
- **理由**：与现有净资产口径一致；类型 key 与设置对齐在前端映射 label。
- **备选**：仅返回「最新快照构成」——否决，无法形成时间序列。

**2. 按账户趋势 API 形状**

- **做法**：`GET /api/v1/dashboard/account-trends?range=3m|6m|1y|all` 返回 `{ accounts: [ { "accountId", "name", "type", "points": [ { "date", "balance" } ] } ] }`，仅包含 `is_active` 账户。
- **理由**：前端一次拉取多序列；筛选按类型在前端做。
- **备选**：每账户单独请求——否决，请求风暴。

**3. 月度累计字段**

- **做法**：在 `monthly-growth` 的每个 `points[]` 元素中增加 `cumulativeChange`（number），由服务端计算。
- **理由**：单一真源，避免前端双算不一致。

**4. 大事记图表类型**

- **做法**：柱状图（分类 × 支出合计）为主；饼图为辅（占比）；合计沿用 `grandTotal`。
- **理由**：与现有 Map 结构兼容；若后续需要笔数，再在 `stats` 增加 `countByCategory`。

## Risks / Trade-offs

- **[Risk]** 账户很多时 `account-trends` 体积大。  
  **Mitigation**：限制返回账户数上限（如 50）或仅活跃账户；前端默认展示前 N 条可折叠。

- **[Risk]** 堆叠图与饼图数据因快照频率不一致产生视觉误导。  
  **Mitigation**：卡片说明「按快照日期」。

## Migration Plan

仅应用代码与前端资源更新；无 DB 迁移。部署后用户刷新即可。

## Open Questions

- P4 的「同比」若要做：需定义「去年同月净资产变化」是否用 `monthly-growth` 两年数据对齐；可在第二迭代实现。
