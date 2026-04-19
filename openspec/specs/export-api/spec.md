# export-api Specification

## Purpose

导出当前用户数据为 CSV，前缀 **`/api/v1/export`**。
## Requirements
### Requirement: 导出 CSV
系统 SHALL 提供 GET /api/v1/export/csv 端点下载所有快照数据的 CSV 文件。

#### Scenario: 导出成功
- **WHEN** GET /api/v1/export/csv
- **THEN** 返回 200，Content-Type 为 text/csv，Content-Disposition 为 attachment; filename="snapshots.csv"

#### Scenario: CSV 格式
- **WHEN** 下载 CSV 文件
- **THEN** 包含表头和所有快照明细，每行一条明细记录

### Requirement: CSV 列定义
CSV SHALL 包含以下列：快照日期、账户名称、账户类型、归属、余额。

#### Scenario: 列内容
- **WHEN** 生成 CSV
- **THEN** 每行格式为 `date,account_name,account_type,owner,balance`

### Requirement: 编码格式
CSV 文件 SHALL 使用 UTF-8 with BOM 编码，确保中文在 Excel 中正确显示。

#### Scenario: 中文显示
- **WHEN** 用 Excel 打开导出的 CSV
- **THEN** 中文账户名称正确显示

