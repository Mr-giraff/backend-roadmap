---
{"title":"大数据组件与数据链路 Roadmap"}
---

# 大数据组件与数据链路 Roadmap

| 类别 | 作用 | 前端类比 | 类比边界 |
| --- | --- | --- | --- |
| 分布式计算基础 | 把数据与计算分给多个执行者，并处理交换和失败 | 将列表分段处理 | 执行者跨进程，通信、资源和恢复都要计入 |
| Hive | 用表和 SQL 描述、读取文件中的数据 | schema 与数据查询 | 表描述、实际文件、分区和查询执行分别管理 |
| Spark | 执行有边界数据上的批处理与分析 | filter、groupBy、reduce | 还要观察执行计划、任务与跨节点数据交换 |
| Flink | 持续处理事件并维护计算状态 | 持续接收事件后更新页面 | 还要定义事件时间、窗口、迟到与故障恢复 |

本路线是第二重点。先建立共同的分布式计算理解，再分别完成 Hive、Spark、Flink 的最小实验。目标是能解释一次计算、核对结果、查看运行证据并定位慢、错、失败；源码实现和完整集群运维属于后续按需专题。

## 前置与环境

先掌握 [05](/05-数据库设计与SQL/README/) 的 SELECT、WHERE、JOIN、GROUP BY、表与类型基础。能够启动和调试一个 Java 服务后，可以与本路线并行推进；不要求先完整学完 07、09、13。

通用 Java 学习环境按统一说明准备。Hive、Spark、Flink、Hadoop 的运行 JDK、连接器和依赖按选定引擎版本单独核对。本路线保留 Flink 1.17 SQL 样本，Spark 样本按 3.5 系列资料讲解；迁移到其他版本时先核对语法和运行环境。版本边界见[统一说明](/参考资料/学习版本与中文资料/)。

## 四个学习单元

```mermaid
flowchart TD
    SQL["05 SQL 基础"] --> BASE["01 分布式计算基础"]
    BASE --> HIVE["02 Hive：表与文件"]
    BASE --> SPARK["03 Spark：批处理执行"]
    BASE --> FLINK["04 Flink：持续计算与状态"]
    HIVE --> CASE["串联案例：从数据到可核对结果"]
    SPARK --> CASE
    FLINK --> CASE
```

| 顺序 | 路线与讲解 | 本单元学到哪里 | 最低验收 |
| --- | --- | --- | --- |
| 1 | [分布式计算基础](/15-大数据组件与数据链路/01-分布式计算基础/README/) · [典型案例](/15-大数据组件与数据链路/01-分布式计算基础/典型案例/) | 数据分区、并行、Shuffle、倾斜、资源、调度与恢复 | 手算分区聚合，画出数据交换和失败后的处理 |
| 2 | [Hive](/15-大数据组件与数据链路/02-Hive/README/) · [典型案例](/15-大数据组件与数据链路/02-Hive/典型案例/) | HiveServer2、Metastore、文件、表、分区、SQL 与读取权限 | 在学习环境登记分区并查到固定样本 |
| 3 | [Spark](/15-大数据组件与数据链路/03-Spark/README/) · [典型案例](/15-大数据组件与数据链路/03-Spark/典型案例/) | 批处理、Driver/Executor、计划、Job/Stage/Task、交换与运行界面 | 执行订单汇总，保存计划和任务证据 |
| 4 | [Flink](/15-大数据组件与数据链路/04-Flink/README/) · [典型案例](/15-大数据组件与数据链路/04-Flink/典型案例/) | 批流、时间、窗口、状态、Checkpoint、Savepoint、背压 | 执行窗口样本，并单独验证持续流恢复 |

README 定学习范围，各单元的典型案例按“概念 → 最小例子 → 相邻结果 → 综合案例 → 练习”展开。先完成一个单元的最低验收，再逐步补充其深入项目。

## 串联案例与共同验收

进入[四单元串联案例](/15-大数据组件与数据链路/典型案例/)。订单样本用于 Hive/Spark，点击样本用于 Flink；不同引擎的 SQL 分开执行。

- [ ] 定义一行代表什么、金额单位、去重、状态和统计时间。
- [ ] 区分表描述、文件存在、分区可见、计算完成和结果可查询。
- [ ] 批处理结果与手算一致：三笔有效订单，总额 23000 分。
- [ ] 窗口边界与预期一致：两个一分钟窗口的点击数为 2 和 1。
- [ ] 根据计划、任务、输入输出和日志解释一个慢、错或失败现象。
- [ ] 记录引擎版本、JDK、连接器、输入、SQL、预期和实际结果。

[批处理脚本](/assets/15-大数据组件与数据链路/examples/batch.sql)、[Hive 输入](/assets/15-大数据组件与数据链路/examples/orders.csv)、[窗口脚本](/assets/15-大数据组件与数据链路/examples/window.sql)、[点击输入](/assets/15-大数据组件与数据链路/examples/clicks.csv)都保留在本目录。固定样本可以手算；引擎运行和故障恢复需要独立环境中的执行证据。

## 按业务需要深入

| 触发问题 | 补充知识 | 进入条件 |
| --- | --- | --- |
| 任务慢、等待资源或失败重试 | 数据倾斜、小文件、资源队列、运行指标、调度职责 | 四单元基础；结合 [13](/13-部署运维与故障排查/README/) |
| 恢复后重复或少算 | 输入回放、输出幂等、连接器与 Checkpoint 协作 | Flink 基础；结合 [08](/08-缓存与消息队列/README/)、[09](/09-分布式系统与服务协作/README/) |
| 模型、口径或历史数据变化 | 事实/维度、分层、重算、补数、版本和质量 | SQL 与串联案例；结合 [04](/04-分层架构与业务建模/README/)、[06](/06-事务与数据一致性/README/) |
| 接入分析引擎或数据治理 | StarRocks/ClickHouse 的模型与导入差异，元数据、血缘和访问控制 | 先跑通一种引擎；结合 [12](/12-安全与权限/README/) |

## 参考资料

- **中文前置主读**：[廖雪峰 SQL](https://liaoxuefeng.com/books/sql/introduction/index.html)与 [JDBC](https://liaoxuefeng.com/books/java/jdbc/index.html)；Java 背景用 [Dev.java](https://dev.java/learn/)补充。它们用于 SQL 与客户端基础。
- **Hive 专项**：[官方 DDL 手册](https://hive.apache.org/docs/latest/language/languagemanual-ddl/)与 [HiveServer2](https://hive.apache.org/docs/latest/admin/setting-up-hiveserver2/)。
- **Spark 专项**：[3.5.7 集群架构](https://spark.apache.org/docs/3.5.7/cluster-overview.html)、[运行观察](https://spark.apache.org/docs/3.5.7/monitoring.html)与 [SQL 调优](https://spark.apache.org/docs/3.5.7/sql-performance-tuning.html)。
- **Flink 中文专项**：[1.17 架构](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/concepts/flink-architecture/)、[窗口](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/dev/table/sql/queries/window-tvf/)与 [Checkpoint](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/ops/state/checkpoints/)。

[返回总纲](/)
