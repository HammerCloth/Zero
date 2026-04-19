# vue-frontend Specification

## Purpose

Vue 3 + Vite + TypeScript 单页应用；生产环境由 Caddy 托管静态资源；API 前缀为 **`/api/v1`**。使用 **Bearer** `Authorization`（`access_token` 存 `localStorage`）与 Cookie 刷新配合，见 `src/api/http.ts`。

## Requirements
### Requirement: Vue 3 项目结构
前端 SHALL 使用 Vue 3 + Vite + TypeScript 构建，包含标准目录结构：src/views、src/components、src/stores、src/api、src/router。

#### Scenario: 项目可构建
- **WHEN** 执行 `npm run build`
- **THEN** 生成 dist 目录包含 index.html 和静态资源

### Requirement: Naive UI 组件库
前端 SHALL 使用 Naive UI 作为 UI 组件库，按需导入组件。

#### Scenario: 组件可用
- **WHEN** 在 Vue 组件中使用 `<n-button>`
- **THEN** 正确渲染 Naive UI 按钮

### Requirement: Vue Router 配置
前端 SHALL 使用 Vue Router 4 进行路由管理，支持路由守卫进行认证检查。

#### Scenario: 未登录跳转
- **WHEN** 未登录用户访问受保护路由
- **THEN** 自动跳转到登录页

### Requirement: Pinia 状态管理
前端 SHALL 使用 Pinia 管理全局状态，包含 auth store 管理用户登录状态。

#### Scenario: 状态持久化
- **WHEN** 用户刷新页面
- **THEN** 通过 refresh token（Cookie）与 `localStorage` 中的 access_token 协作恢复会话（见 `http.ts`）

### Requirement: Axios HTTP 客户端
前端 SHALL 使用 Axios 封装 HTTP 请求，自动附加 `Authorization: Bearer <access_token>`，处理 401 时调用刷新。

#### Scenario: 令牌刷新
- **WHEN** access_token 过期，API 返回 401
- **THEN** 自动调用 `POST /api/v1/auth/refresh`（`withCredentials`），成功后重试原请求

#### Scenario: 刷新失败
- **WHEN** refresh_token 也过期
- **THEN** 跳转到登录页

### Requirement: 响应式布局
前端 SHALL 支持响应式布局，在桌面端显示侧边栏，移动端显示底部导航。

#### Scenario: 桌面端
- **WHEN** 屏幕宽度 >= 768px
- **THEN** 显示左侧导航栏

#### Scenario: 移动端
- **WHEN** 屏幕宽度 < 768px
- **THEN** 显示底部 Tab 导航

### Requirement: 主题配色
前端 SHALL 使用统一的主题配色，主色调为靛蓝色（类似 Tailwind indigo-600）。

#### Scenario: 品牌一致性
- **WHEN** 查看页面
- **THEN** 主按钮、链接、高亮使用统一的主色调

