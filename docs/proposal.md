# Project Zero - 资产管家

## 概述

一个轻量级的家庭资产管理系统，帮助追踪净资产变化趋势，了解财富增长情况。

**核心理念**：随时知道自己有多少钱，钱在增长还是减少，大钱花在哪了。

**录入原则**：每月 2 次快照（15日 A发薪后、30日 B发薪后），5分钟搞定。

---

## 用户场景

### 使用者
- A（你）：15日发工资
- B（妻子）：30日发工资

### 月度资金流转
```
15日 A发工资 → 2.5k 到小荷包 + 5k 投国内基金 + 剩余存银行
30日 B发工资 → 2.5k 到小荷包 + 5k 投海外基金 + 剩余存银行
```

### 特殊支出
- 每季度房租 1.5w
- 旅行/意外支出使用信用卡
- 存单到期转存

---

## 账户体系

| 类型 | 账户名 | 所有人 | 备注 |
|------|--------|--------|------|
| 现金 cash | A 工资卡 | A | 发薪15日 |
| 现金 cash | B 工资卡 | B | 发薪30日 |
| 现金 cash | 小荷包 | 共同 | 日常花销 5k/月 |
| 固收 deposit | 招行存单 | 共同 | 记总额，会到期转存 |
| 固收 deposit | 月月宝 | 共同 | |
| 基金 fund | 国内标普 QDII | A | 15日定投 5k |
| 基金 fund | 海外基金 (汇丰 Trade25) | B | 30日定投 5k |
| 养老 pension | 个人养老金 | A | |
| 公积金 housing_fund | A 住房公积金 | A | |
| 公积金 housing_fund | B 住房公积金 | B | |
| 负债 credit | 信用卡（含亲情卡） | A | 旅行/意外支出 |

**账户总数：11 个**

---

## 功能模块

### 1. 认证
- 登录 / 登出
- 首次启动交互设置管理员账户
- 管理员可添加其他用户（设置临时密码）
- 首次登录强制修改密码
- JWT Token + HttpOnly Cookie
- 「记住我」保持登录 30 天

### 2. 账户管理
- 查看账户列表
- 添加 / 编辑 / 停用账户
- 调整显示顺序

### 3. 快照录入
- 新建快照（自动复制上次数据，只改变化的）
- 实时显示差异和净资产变化
- 可添加「大事记」（大额支出）
- 查看 / 编辑 / 删除历史快照

### 4. 大事记
- 分类：房租 / 旅行 / 医疗 / 家电装修 / 人情往来 / 其他
- 记录金额和描述
- 年度统计汇总

### 5. 资产总览（图表）

#### 关键指标卡片
- 当前净资产
- 本月增长（金额 + 百分比）
- 年度增长（金额 + 百分比）
- 年化收益率

#### 图表
| 图表 | 类型 | 作用 |
|------|------|------|
| 净资产趋势 | 折线图 | 核心指标，看整体增长 |
| 资产构成（类型） | 饼图 | 现金/固收/基金/公积金占比 |
| 资产构成（所有人） | 饼图 | A/B/共同资产占比 |
| 资产堆叠 | 堆叠面积图 | 各类资产随时间变化 |
| 月度增长 | 柱状图 | 每月增/减，发现异常月 |
| 账户明细 | 多折线图 | 各账户单独趋势，可筛选 |

#### 交互
- 时间范围选择：近3月 / 近6月 / 近1年 / 近2年 / 全部
- Hover 显示具体数值
- 按资产类型/所有人筛选
- 导出图表为图片 / 数据为 CSV

---

## 数据模型

### users（用户）
```
id            UUID        主键
username      string      用户名
password_hash string      bcrypt 哈希
is_admin      boolean     是否管理员
must_change   boolean     是否需要修改密码
created_at    timestamp
```

### accounts（账户）
```
id            UUID        主键
user_id       UUID        所属用户
name          string      账户名
type          enum        cash/deposit/fund/pension/housing_fund/credit
owner         enum        A/B/shared
sort_order    int         显示顺序
is_active     boolean     是否启用
created_at    timestamp
```

### snapshots（快照主表）
```
id            UUID        主键
user_id       UUID        所属用户
date          date        快照日期
note          string?     备注
created_at    timestamp
created_by    UUID        创建者
```

### snapshot_items（快照明细）
```
id            UUID        主键
snapshot_id   UUID        关联快照
account_id    UUID        关联账户
balance       decimal     余额（负债为负数）
```

### events（大事记）
```
id            UUID        主键
snapshot_id   UUID        关联快照
category      enum        rent/travel/medical/appliance/social/other
description   string      描述
amount        decimal     金额（支出为负数）
created_at    timestamp
```

---

## 技术架构

### 技术栈
| 层 | 选型 | 理由 |
|----|------|------|
| 后端 | Go + Gin | 用户偏好，单二进制，内存小 |
| 数据库 | SQLite | 数据量小，备份简单 |
| 前端 | React + Vite + Tailwind | 现代 UI，响应式 |
| 图表 | Recharts | React 友好 |
| 认证 | JWT + bcrypt | 安全标准 |
| 部署 | Docker Compose + Caddy | 自动 HTTPS |

### 部署架构
```
┌─────────────┐
│   Caddy     │ ← 自动 HTTPS (Let's Encrypt)
│  (反代)     │
└──────┬──────┘
       │
       ├─────────────────┐
       │                 │
       ▼                 ▼
┌────────────┐    ┌────────────┐
│   前端     │    │   后端     │
│  (静态)    │    │   (Go)     │
│  React     │    │   Gin      │
│  Tailwind  │    │   SQLite   │
└────────────┘    └────────────┘
```

### 项目结构
```
zero/
├── backend/
│   ├── cmd/server/main.go
│   ├── internal/
│   │   ├── handler/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   │   ├── middleware/
│   │   └── config/
│   ├── migrations/
│   ├── go.mod
│   └── go.sum
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── hooks/
│   │   ├── api/
│   │   └── App.tsx
│   ├── package.json
│   └── vite.config.ts
│
├── docker-compose.yml
├── Caddyfile
└── README.md
```

---

## 安全设计

### 传输安全
- Caddy 自动申请 Let's Encrypt 证书
- 强制 TLS 1.2+
- HTTP 自动跳转 HTTPS
- HSTS 头

### 认证安全
- bcrypt 密码哈希（cost=12）
- Access Token: 1小时有效
- Refresh Token: 30天有效，HttpOnly Cookie
- 登录限流：5次失败锁15分钟

### 密码要求
- 最少 8 位
- 不强制复杂度（用户自行决定）

### 用户初始化
- 首次访问显示初始设置页面
- 管理员创建其他用户时设置临时密码
- 用户首次登录强制修改密码
- 密码全程不接触文件/环境变量

---

## 移动端适配

- 响应式布局（手机/平板/桌面）
- 一列布局便于录入
- 图表支持横向滑动
- 可添加到主屏幕（PWA）

---

## 后续扩展（暂不实现）

- 模块 B：定投顾问（标普/纳指投资比例建议）
- 2FA (TOTP)
- 登录通知（Telegram）
- IP 白名单
- 存单到期提醒

---

## 部署要求

- 服务器：2C2G（绰绰有余）
- 域名：需要（用于 HTTPS）
- 端口：80, 443
