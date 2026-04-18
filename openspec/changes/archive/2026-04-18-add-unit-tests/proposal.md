## Why

当前 `zero` 后端与前端几乎没有自动化单元测试，回归依赖手工点验，关键计算（净资产、仪表盘时间逻辑、格式化等）缺少可重复验证。需要在不改动产品对外行为的前提下，引入可维护的测试基线与工具链，为后续迭代降低风险。

## What Changes

- 为 Go `service` 层及可测纯函数补充 **`*_test.go`**（表驱动为主）。
- 为前端工具函数等补充 **Vitest** 单测，并在 `package.json` 中提供 `npm test`。
- 在变更设计文档中约定 **时间/依赖注入** 等可测性要点（如仪表盘对 `time.Now` 的处理）。
- （可选）增加 **CI 工作流**，在合并前执行 `go test` 与 `npm test`。
- **不**改变现有 HTTP API 契约或 OpenSpec 中各业务能力的对外需求表述（本变更新增独立「测试」能力规格）。

## Capabilities

### New Capabilities

- `automated-testing`：定义自动化单元测试的范围、工具命令与最低验收（后端/前端/可选 CI）。

### Modified Capabilities

- （无）业务功能规格不因本变更而修改；测试作为工程能力单独成 spec。

## Impact

- **代码**：`zero/backend/**/*.go`（新增 `*_test.go`）、`zero/frontend`（`vitest` 配置与 `src/**/*.test.ts(x)`）。
- **依赖**：前端新增 `vitest` 及相关类型（devDependencies）。
- **流程**：开发者与 CI 需运行测试命令；无数据库 schema 或 API 迁移。
