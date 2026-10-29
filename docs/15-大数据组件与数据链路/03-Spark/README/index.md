---
{"title":"Spark Roadmap"}
---

# Spark Roadmap

| 类别 | 作用 | 前端类比 | 类比边界 |
| --- | --- | --- | --- |
| SQL 与批处理 | 对固定输入执行清洗、关联和聚合 | 数组过滤和分组 | 计算由分区任务执行，可能发生数据交换 |
| 执行架构 | 组织计划并运行任务 | 调度逻辑与工作线程 | 集群中 Driver、Executor、资源管理分工明确 |
| 计划与运行界面 | 解释怎样算、哪里慢或失败 | 看请求与性能工具 | 计划和实际运行指标必须相互印证 |

目标是跑通一个批处理，核对结果，能从 SQL 计划找到数据交换，并在运行界面识别应用、阶段、任务和执行资源。前置为 [SQL](/05-数据库设计与SQL/README/)与[共同基础](/15-大数据组件与数据链路/01-分布式计算基础/README/)；接 Hive 数据时补 [Hive](/15-大数据组件与数据链路/02-Hive/README/)。

## 学习顺序

| 顺序 | 范围 | 最小案例 | 过关证据 |
| --- | --- | --- | --- |
| 1 | 有界输入、临时视图、过滤去重聚合 | [固定输入](/15-大数据组件与数据链路/03-Spark/典型案例/#一-固定输入先把计算口径写清楚) | 手算与实际输出一致 |
| 2 | Driver、Executor、应用、Job、Stage、Task | [执行角色](/15-大数据组件与数据链路/03-Spark/典型案例/#二-driver-组织执行executor-处理任务) | 解释代码、进程与任务的关系 |
| 3 | 分区、计划、Exchange、局部与最终聚合 | [计划和界面](/15-大数据组件与数据链路/03-Spark/典型案例/#三-计划说明怎样算运行界面说明算得怎样) | 保存计划、任务与运行参数 |
| 4 | 输入量、Shuffle、倾斜、资源等待 | [慢任务](/15-大数据组件与数据链路/03-Spark/典型案例/#四-用运行证据区分慢在哪里) | 至少两个候选原因及区分证据 |
| 5 | 临时结果、持久化、重跑和输出边界 | [结果保存](/15-大数据组件与数据链路/03-Spark/典型案例/#五-临时视图持久化和重跑是三件事) | 解释会话结束和重跑会发生什么 |

## 环境与实践范围

SQL 样本按 Spark 3.5 系列资料讲解，先使用兼容 JDK 的隔离学习环境。`local[2]` 提供两个本地执行线程，用于小实验；多机网络、独立执行进程和资源调度要在集群运行时验证。

通用当前学习 JDK 不决定 Spark 运行 JDK。记录 Spark、JDK、Catalog、连接器和存储配置；迁移到公司版本前核对语法和依赖兼容。

先学 Spark SQL 与执行过程，再按任务补 Java/Scala API、数据格式和作业提交。完整 RDD API、源码、复杂调优不作为本单元前置。

## 综合案例与验收

完成[订单批处理](/15-大数据组件与数据链路/03-Spark/典型案例/#六-综合案例-订单批处理与执行证据)。

- [ ] 执行 [batch.sql](/assets/15-大数据组件与数据链路/examples/batch.sql)，结果为 east 15000、south 8000 分。
- [ ] 对一个分组查询保存 EXPLAIN，解释数据交换与聚合。
- [ ] 在运行界面找到应用、任务、输入与交换指标；区别预期计划和实际执行。
- [ ] 对空结果、结果重复或慢任务给出有证据的排查过程。
- [ ] 记录临时视图、持久表、输入批次和重跑策略。

## 参考资料

- **中文前置**：[廖雪峰 SQL](https://liaoxuefeng.com/books/sql/introduction/index.html)、[JDBC](https://liaoxuefeng.com/books/java/jdbc/index.html)；[Dev.java](https://dev.java/learn/)补 Java API 背景。
- **执行角色**：[Spark 3.5.7 集群架构](https://spark.apache.org/docs/3.5.7/cluster-overview.html)。
- **计划与性能**：[SQL 性能说明](https://spark.apache.org/docs/3.5.7/sql-performance-tuning.html)、[运行界面与指标](https://spark.apache.org/docs/3.5.7/monitoring.html)。
- **数据交换与恢复**：[RDD 指南](https://spark.apache.org/docs/3.5.7/rdd-programming-guide.html)按概念查阅。

[返回大数据路线](/15-大数据组件与数据链路/README/) · [返回总纲](/)
