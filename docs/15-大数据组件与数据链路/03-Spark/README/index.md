---
{"title":"Spark Roadmap"}
---

# Spark Roadmap

| 类别 | 作用 | 前端类比 | 类比边界 |
| --- | --- | --- | --- |
| SQL 与批处理 | 对固定输入执行清洗、关联和聚合 | 数组过滤和分组 | 计算由分区任务执行，可能发生数据交换 |
| 执行架构 | 组织计划并运行任务 | 调度逻辑与工作线程 | 集群中 Driver、Executor、资源管理分工明确 |
| 计划与运行界面 | 解释怎样算、哪里慢或失败 | 看请求与性能工具 | 计划和实际运行指标必须相互印证 |

目标是读懂并修改批处理，核对业务结果，能从 SQL 计划找到数据交换，并在运行界面识别应用、阶段、任务和执行资源。前置为 [SQL](/05-数据库设计与SQL/README/)与[共同基础](/15-大数据组件与数据链路/01-分布式计算基础/README/)；接 Hive 数据时补 [Hive](/15-大数据组件与数据链路/02-Hive/README/)。

## 学习顺序

先明确业务问题，再理解计算与运行保证，读对应资料，最后在 SQL、代码、配置和计划中定位。

| 业务问题 | 概念与保证 | 代码与讲解入口 |
| --- | --- | --- |
| 批处理统计的是什么记录，重复和多版本怎样处理 | 有界输入、数据粒度、过滤、去重与聚合口径 | [业务口径](/15-大数据组件与数据链路/03-Spark/典型案例/#一-固定输入先把计算口径写清楚) |
| SQL、进程和分区工作分别由谁组织与执行 | Driver、Executor、应用、Job、Stage、Task | [执行角色](/15-大数据组件与数据链路/03-Spark/典型案例/#二-driver-组织执行executor-处理任务) |
| 为什么发生数据交换，计划与实际任务数为何可能不同 | 分区、Exchange、局部与最终聚合、自适应执行 | [计划与运行](/15-大数据组件与数据链路/03-Spark/典型案例/#三-计划说明怎样算运行界面说明算得怎样) |
| 慢任务来自数据倾斜、落盘还是资源等待 | 输入量、Shuffle、耗时、资源和首个失败证据 | [定位慢任务](/15-大数据组件与数据链路/03-Spark/典型案例/#四-用运行证据区分慢在哪里) |
| 会话结束或作业重跑后，结果会保留、覆盖还是重复 | 临时视图、缓存、持久输出和重跑策略 | [结果生命周期](/15-大数据组件与数据链路/03-Spark/典型案例/#五-临时视图持久化和重跑是三件事) |

执行角色查 [Spark 3.5.7 架构](https://spark.apache.org/docs/3.5.7/cluster-overview.html)，计划、指标与自适应执行查 [SQL 性能说明](https://spark.apache.org/docs/3.5.7/sql-performance-tuning.html)和[运行观察](https://spark.apache.org/docs/3.5.7/monitoring.html)。参考 SQL 按 3.5 系列讲解；实际 Spark、JDK、Catalog、连接器和存储按工程匹配。local[2] 仅表示本地两个执行线程，不能代替多机执行证据。先理解 SQL 与执行过程，Java/Scala API、完整 RDD 与源码按任务深入。

## 综合案例与业务验证

[订单批处理案例](/15-大数据组件与数据链路/03-Spark/典型案例/#六-综合案例-订单批处理与执行证据)把口径、转换、交换和输出连成一条处理链。项目中先从 SQL 或 API 找到输入与输出，再用执行计划和任务记录解释实际计算。

SQL 或性能改动需核对正确结果、数据规模与分布、读取和交换量、任务耗时及资源等待。涉及写入时确认追加/覆盖、分区和重跑规则；接 Hive 时确认 Catalog、Metastore、文件与读取身份。参考 SQL 的结果不能单独证明集群性能或失败恢复保证。

## 参考资料

- **中文前置**：[廖雪峰 SQL](https://liaoxuefeng.com/books/sql/introduction/index.html)、[JDBC](https://liaoxuefeng.com/books/java/jdbc/index.html)；[Dev.java](https://dev.java/learn/)补 Java API 背景。
- **执行角色**：[Spark 3.5.7 集群架构](https://spark.apache.org/docs/3.5.7/cluster-overview.html)。
- **计划与性能**：[SQL 性能说明](https://spark.apache.org/docs/3.5.7/sql-performance-tuning.html)、[运行界面与指标](https://spark.apache.org/docs/3.5.7/monitoring.html)。
- **数据交换与恢复**：[RDD 指南](https://spark.apache.org/docs/3.5.7/rdd-programming-guide.html)按概念查阅。

[返回大数据路线](/15-大数据组件与数据链路/README/) · [返回总纲](/)
