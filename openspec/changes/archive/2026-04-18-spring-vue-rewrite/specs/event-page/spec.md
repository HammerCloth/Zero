## ADDED Requirements

### Requirement: 大事记统计页
系统 SHALL 提供大事记统计页 /events/stats，显示年度大事记支出统计。

#### Scenario: 默认当年
- **WHEN** 访问 /events/stats
- **THEN** 显示当年的大事记统计

#### Scenario: 切换年份
- **WHEN** 修改年份输入框
- **THEN** 显示对应年份的统计

### Requirement: 统计表格
页面 SHALL 显示分类汇总表格，包含分类、笔数、金额列。

#### Scenario: 表格内容
- **WHEN** 查看统计表格
- **THEN** 每行显示一个分类的统计数据

#### Scenario: 合计金额
- **WHEN** 查看页面
- **THEN** 显示年度支出合计金额

### Requirement: 分类标签
分类 SHALL 显示为中文标签：房租、旅行、医疗、家电装修、人情往来、其他。

#### Scenario: 标签映射
- **WHEN** 分类为 travel
- **THEN** 显示为「旅行」

### Requirement: 空数据处理
页面 SHALL 正确处理无数据的年份。

#### Scenario: 无数据
- **WHEN** 该年无大事记
- **THEN** 表格为空，合计显示 ¥0.00
