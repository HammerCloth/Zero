## Context

Project Zero 是一个家庭资产管家应用，当前架构为：
- 后端：Go (Gin) + SQLite
- 前端：React + Tailwind
- 部署：Docker + Caddy

维护者对 Go/React 不熟悉，导致：
- 部署问题频发（迁移文件丢失、CORS、路由匹配）
- 难以自主排查和修复问题
- 前端 UI 不理想

服务器资源：2C2G，需要控制内存占用。

## Goals / Non-Goals

**Goals:**
- 使用 Spring Boot + MyBatis + Vue 3 完全重写，降低维护成本
- 使用 Flyway 从根本上解决数据库迁移问题
- 使用 Naive UI 提升前端视觉效果
- 保持 SQLite + Docker + Caddy 部署模式，控制内存占用
- 修复现有安全隐患（Cookie Secure/SameSite）

**Non-Goals:**
- 数据迁移（现有数据量少，可重置）
- API 向后兼容（前后端同步重写）
- 引入 MySQL（2C2G 内存不足）
- 微服务架构（单体足够）

## Decisions

### 1. 后端框架：Spring Boot 3 + Java 21

**选择**：Spring Boot 3.2+ with Java 21

**理由**：
- 维护者熟悉 Spring 生态
- Spring Boot 3 是当前 LTS，长期支持
- Java 21 是 LTS，虚拟线程可选

**备选**：
- Spring Boot 2.7：即将 EOL，不推荐
- Kotlin：学习成本，暂不引入

### 2. 数据访问：MyBatis

**选择**：MyBatis 3 + mybatis-spring-boot-starter

**理由**：
- 维护者熟悉 MyBatis
- 对 SQLite 无方言限制（JPA/Hibernate 有）
- 手写 SQL 更可控，适合从 Go 迁移
- 排查问题直观

**备选**：
- Spring Data JPA：SQLite 方言支持差，ddl-auto 坑多
- JDBC Template：太底层，重复代码多

### 3. 数据库迁移：Flyway

**选择**：Flyway

**理由**：
- SQL 脚本在 resources/db/migration/，打包进 JAR，不会丢失
- 启动时自动检测、自动执行
- 与 Spring Boot 深度集成

**备选**：
- Liquibase：XML 配置，不如 SQL 直接
- 手动迁移：已证明会出问题

### 4. 认证方案：Spring Security + JJWT

**选择**：
- Spring Security（Stateless 模式）
- JJWT（io.jsonwebtoken）生成/验证 JWT
- access_token：前端内存，Authorization: Bearer
- refresh_token：HttpOnly + Secure + SameSite=Strict Cookie

**理由**：
- 标准方案，文档丰富
- 与现有认证逻辑一致，迁移平滑
- 顺手修复 Cookie 安全性

**备选**：
- Spring Security OAuth2：过于重量级
- Session：不适合 SPA

### 5. 前端框架：Vue 3 + Vite + TypeScript

**选择**：
- Vue 3 (Composition API)
- Vite 5
- TypeScript
- Pinia（状态管理）
- Axios（HTTP 客户端）

**理由**：
- 维护者对 Vue 更熟悉
- Vite 开发体验好
- TypeScript 有助于 AI 辅助开发

**备选**：
- Vue 2：即将 EOL
- Nuxt：SSR 不需要

### 6. UI 组件库：Naive UI

**选择**：Naive UI

**理由**：
- 现代简洁风格
- Vue 3 + TypeScript 友好
- 组件质量高，个人项目流行

**备选**：
- Element Plus：风格偏企业后台
- Ant Design Vue：偏重量级

### 7. 项目结构

**后端目录**：
```
backend/
├── src/main/java/com/zero/
│   ├── ZeroApplication.java
│   ├── config/           # 配置类
│   ├── controller/       # REST Controller
│   ├── service/          # 业务逻辑
│   ├── mapper/           # MyBatis Mapper
│   ├── model/            # 实体类
│   ├── dto/              # 请求/响应 DTO
│   └── security/         # JWT + Security
├── src/main/resources/
│   ├── application.yml
│   ├── db/migration/     # Flyway SQL
│   └── mapper/           # MyBatis XML (可选)
└── pom.xml
```

**前端目录**：
```
frontend/
├── src/
│   ├── main.ts
│   ├── App.vue
│   ├── router/           # Vue Router
│   ├── stores/           # Pinia
│   ├── api/              # Axios 封装
│   ├── views/            # 页面组件
│   ├── components/       # 通用组件
│   └── types/            # TypeScript 类型
├── index.html
├── vite.config.ts
├── tsconfig.json
└── package.json
```

### 8. 部署方案

**Docker 镜像**：
- 后端：`eclipse-temurin:21-jre-alpine` (~180MB)
- 多阶段构建：Maven 构建 → 只保留 JAR

**内存预算**：
```
组件          预估内存
───────────   ────────
OS + Docker   ~300MB
Caddy         ~20MB
Spring Boot   ~350MB (-Xmx256m)
SQLite        ~0MB (文件)
───────────   ────────
合计          ~670MB
剩余          ~1330MB ✓
```

**docker-compose.yml**：
- backend：Spring Boot JAR
- caddy：不变
- 挂载 SQLite 数据文件

## Risks / Trade-offs

### [R1] SQLite 并发写性能有限
→ 对于个人/家庭使用场景，写并发极低，不构成问题

### [R2] Spring Boot 启动慢（冷启动 10-20s）
→ 容器设置 restart: unless-stopped，挂了自动重启；可接受

### [R3] 全栈重写工期长
→ 分阶段：先后端跑通（Postman 验证），再前端对接；避免同时改接口和页面

### [R4] MyBatis + SQLite 生态小众
→ JDBC 层面无障碍；只是社区讨论少，但技术上完全可行

### [R5] 前后端同时重写，容易出错
→ API 设计先固定，Postman 集合验证；前端再对接

## Migration Plan

**阶段 1：后端骨架**
1. 创建 Spring Boot Maven 项目
2. 配置 SQLite + Flyway + MyBatis
3. 实现 Auth 模块（JWT + Cookie）
4. 用 curl/Postman 验证登录流程

**阶段 2：后端业务**
1. 实现 Users / Accounts / Snapshots API
2. 实现 Dashboard / Events / Export API
3. 完整 Postman 集合测试

**阶段 3：前端骨架**
1. 创建 Vue 3 + Vite 项目
2. 配置 Router + Pinia + Axios
3. 实现登录/初始化页面
4. 验证与后端联调

**阶段 4：前端业务**
1. 仪表盘页面（图表）
2. 快照/账户/大事记页面
3. 用户管理页面（管理员）

**阶段 5：部署**
1. 更新 Dockerfile（多阶段构建）
2. 更新 docker-compose.yml
3. 服务器重置数据卷
4. 部署上线

**回滚策略**：
- 保留 main 分支现有代码
- spring-backend 分支开发
- 验证通过后再合并/替换
