## 1. 后端：纯函数与归一化

- [x] 1.1 为 `CalculateNetWorth` 添加表驱动测试（空切片、多账户、正负余额组合）
- [x] 1.2 为 `normalizeSnapshotItems` / `normalizeEvents`（或等价非导出逻辑通过 `Create`/`Update` 可观测行为）添加覆盖排序与 ID 补全的测试
- [x] 1.3 为 `validateAllAccountBalances` 与 `ErrMissingBalances` 路径编写测试（可用 fake account repo 或内存 stub）

## 2. 后端：仪表盘与时间

- [x] 2.1 将 `DashboardService` 中依赖 `time.Now()` 的逻辑抽出为可注入时间或纯函数参数，并补充表驱动测试
- [x] 2.2 为 `CalculateAnnualizedReturn`（若存在）或趋势筛选逻辑添加至少一组边界用例测试

## 3. 前端：Vitest 与工具函数

- [x] 3.1 安装并配置 Vitest（含 `vitest.config` 或与 Vite 合并配置），添加 `npm test` / `npm run test` 脚本
- [x] 3.2 为 `src/lib/format.ts`（`formatCurrency`、`formatPercent`）添加单测
- [x] 3.3 为与后端一致的余额规则函数（若存在于 `api.ts` 或独立模块）添加单测，与 1.1 用例对齐

## 4. 文档与可选 CI

- [x] 4.1 在 `README.md` 或 `frontend/README.md`（择一或两处摘要）中写明 `go test` 与 `npm test` 命令及前置条件
- [x] 4.2 （可选）添加 GitHub Actions 工作流：checkout → setup Go/Node → `go test ./...` → `npm ci` + `npm test`

## 5. 验收

- [x] 5.1 本地执行 `cd backend && go test ./...` 与 `cd frontend && npm test` 通过
- [x] 5.2 运行 `openspec validate --change add-unit-tests`（或项目要求的校验命令）无错误
