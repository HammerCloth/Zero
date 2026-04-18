## Context

- 后端：Go 1.18+、`github.com/mattn/go-sqlite3`（CGO），业务逻辑主要在 `internal/service`，纯计算如 `CalculateNetWorth` 已集中在 `snapshot_service.go`。
- 前端：Vite + React + TypeScript，格式化等在 `src/lib/`，与后端重复的余额规则在前端 `api` 层有部分封装。
- 当前无 `*_test.go`、无 Vitest；归档变更 `asset-manager` 未包含自动化测试任务。

## Goals / Non-Goals

**Goals:**

- 建立 **可重复的** `go test ./...` 与 `npm test`（或等价脚本），默认在本地无额外服务即可跑通核心单测。
- 优先覆盖 **高价值、低耦合**：纯函数、无 I/O 的 service 逻辑；前端 `lib/` 与纯工具函数。
- 对依赖 **时间** 的代码采用 **可注入时钟** 或 **参数化日期**，避免 flaky test。

**Non-Goals:**

- 不强制 E2E（Playwright）或完整 API 集成测试（可作为后续 change）。
- 不追求初始阶段覆盖率数值门槛（可在任务中保留「可选」覆盖率报告）。
- 不替换 `go-sqlite3` 为纯 Go 驱动；集成测若需真 DB，可用临时文件或现有 repository 模式，本设计不强制。

## Decisions

| 决策 | 选择 | 备选 | 理由 |
|------|------|------|------|
| 后端测试框架 | `testing` + 表驱动 | testify 全家桶 | 标准库即可；若断言繁琐可仅加 `github.com/stretchr/testify/assert`（可选） |
| 前端测试运行器 | Vitest | Jest | 与 Vite 同构，配置成本低 |
| Handler 层 | 第二轮或少量烟测 | 一开始就大量 httptest | 先保证 service/工具，handler 绑定薄、收益递减 |
| Service + DB | 优先无 DB 单测；必要时 **临时 SQLite 文件** | sqlmock | sqlmock 维护成本高；真库更贴近集成 |
| 时间 | `DashboardService` 等：**注入 `func() time.Time` 或把「当前日」传入纯函数** | 只测固定历史数据 | 避免 `time.Now()` 导致按日失败 |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| CGO 导致 `go test` 在部分环境变慢 | 接受；CI 使用与生产一致的 toolchain |
| 补测时发现隐藏 bug | 修 bug 与加测同一变更内完成，并在 PR 中说明 |
| 前端图表/Recharts 组件测起来重 | 不测渲染细节，只测数据映射与工具函数 |

## Migration Plan

- 无数据迁移。合并后开发者拉代码执行 `go test ./...` 与 `npm test` 即可。
- 若启用 CI：在默认分支 protection 中逐步要求检查通过（团队自行打开）。

## Open Questions

- 是否引入 ** testify** 仅用于 `require.NoError`（团队偏好）。
- CI 使用 **GitHub Actions** 还是其他（仓库托管未定则任务标为可选）。
