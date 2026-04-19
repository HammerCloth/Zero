## ADDED Requirements

### Requirement: Maven 项目结构
系统 SHALL 使用 Maven 构建 Spring Boot 3 项目，包含标准目录结构：src/main/java、src/main/resources、src/test/java。

#### Scenario: 项目可构建
- **WHEN** 执行 `mvn clean package -DskipTests`
- **THEN** 生成可执行的 JAR 文件

### Requirement: Spring Boot 配置
系统 SHALL 支持通过 application.yml 和环境变量配置以下参数：
- APP_PORT：服务端口（默认 8080）
- DATABASE_PATH：SQLite 文件路径
- JWT_ACCESS_SECRET：访问令牌密钥
- JWT_REFRESH_SECRET：刷新令牌密钥
- FRONTEND_ORIGIN：允许的 CORS 来源（支持逗号分隔多个）

#### Scenario: 环境变量覆盖配置
- **WHEN** 设置环境变量 APP_PORT=9090
- **THEN** 应用启动时监听 9090 端口

### Requirement: SQLite 数据库连接
系统 SHALL 使用 xerial sqlite-jdbc 连接 SQLite 数据库，数据库文件路径通过 DATABASE_PATH 配置。

#### Scenario: 数据库自动创建
- **WHEN** 配置的数据库文件不存在
- **THEN** 应用启动时自动创建数据库文件

### Requirement: Flyway 数据库迁移
系统 SHALL 使用 Flyway 管理数据库迁移，SQL 脚本位于 src/main/resources/db/migration/，启动时自动执行未应用的迁移。

#### Scenario: 首次启动建表
- **WHEN** 数据库为空且存在 V1__init.sql
- **THEN** 应用启动时自动执行 V1__init.sql 创建表结构

#### Scenario: 增量迁移
- **WHEN** 数据库已执行 V1，新增 V2__add_column.sql
- **THEN** 应用启动时仅执行 V2__add_column.sql

### Requirement: MyBatis 配置
系统 SHALL 使用 MyBatis 作为数据访问层，Mapper 接口位于 com.zero.mapper 包，支持注解 SQL 或 XML 映射。

#### Scenario: Mapper 接口可用
- **WHEN** 定义 @Mapper 注解的接口
- **THEN** Spring 自动创建代理实现并注入

### Requirement: CORS 配置
系统 SHALL 配置 CORS 允许 FRONTEND_ORIGIN 指定的来源访问，支持 GET/POST/PUT/DELETE 方法，允许 Authorization 和 Content-Type 头，允许携带凭证。

#### Scenario: 跨域请求成功
- **WHEN** 前端从 FRONTEND_ORIGIN 发起跨域请求
- **THEN** 响应包含正确的 CORS 头，请求成功

### Requirement: 健康检查端点
系统 SHALL 提供 GET /healthz 端点返回 `{"ok": true}`，无需认证。

#### Scenario: 健康检查
- **WHEN** GET /healthz
- **THEN** 返回 200 和 `{"ok": true}`

### Requirement: 全局异常处理
系统 SHALL 提供全局异常处理器，捕获未处理异常并返回统一格式：`{"error": "错误信息"}`，HTTP 状态码根据异常类型决定。

#### Scenario: 业务异常返回 400
- **WHEN** 业务逻辑抛出参数校验异常
- **THEN** 返回 400 和 `{"error": "..."}`

#### Scenario: 未知异常返回 500
- **WHEN** 发生未预期的异常
- **THEN** 返回 500 和 `{"error": "服务器内部错误"}`
