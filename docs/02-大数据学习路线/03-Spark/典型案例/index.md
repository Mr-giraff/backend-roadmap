---
{"title":"Spark 典型案例"}
---

# Spark 典型案例

## 技能迁移表

| 前端经验 | Spark 对应能力 | 需要补充的边界 |
| --- | --- | --- |
| filter、去重、groupBy | SQL 转换和聚合 | 输入分布和执行计划决定工作怎样拆分 |
| 页面调度多个请求 | Driver 组织执行 | Executor 在集群中是独立执行进程 |
| 性能面板 | SQL / Stage / Task 指标 | 任务慢要看输入、交换、等待与失败 |
| 刷新后重新计算 | 作业重跑 | 输出可能覆盖、追加或产生重复 |

## 一 固定输入，先把计算口径写清楚

下面的 SQL 用固定订单说明过滤、去重和聚合的关系：

```sql
CREATE OR REPLACE TEMP VIEW raw_orders AS
SELECT * FROM VALUES
    (101, 'east', 10000, 'PAID'),
    (102, 'east', 5000, 'PAID'),
    (101, 'east', 10000, 'PAID'),
    (103, 'south', 8000, 'PAID'),
    (104, 'south', 7000, 'CANCELLED')
AS t(order_id, region, amount_cents, status);

CREATE OR REPLACE TEMP VIEW clean_orders AS
SELECT DISTINCT order_id, region, amount_cents
FROM raw_orders
WHERE status='PAID';

SELECT region, COUNT(*) AS order_count, SUM(amount_cents) AS amount_cents
FROM clean_orders
GROUP BY region
ORDER BY region;
```

预期 east：2 笔、15000 分；south：1 笔、8000 分。这里的 DISTINCT 按三列去除完全相同记录。若一笔订单有多个版本，要先定义有效版本规则。

完整参考为 [batch.sql](/assets/02-大数据学习路线/examples/batch.sql)。固定输入便于理解口径；实际作业还要核对来源、执行计划、数据规模和输出策略。

## 二 Driver 组织执行，Executor 处理任务

| 名称 | 含义 | 本例怎样理解 |
| --- | --- | --- |
| 应用 | 一次运行中的 Spark 程序 | 由一个 Driver 组织执行 |
| Driver | 组织计划和调度，跟踪结果 | SQL 会话触发计划和工作 |
| Executor | 集群中的执行进程 | 执行分配的任务，使用其 CPU 和内存 |
| Job | 为某次计算动作组织的一组执行工作 | SQL 的实际工作按计划产生 |
| Stage | 可一起组织调度的一段任务 | 常受 Shuffle 等依赖边界影响 |
| Task | 一个阶段中处理某份分区的工作 | 分配给可用执行资源 |

[官方集群架构](https://spark.apache.org/docs/3.5.7/cluster-overview.html)描述这些运行角色。SQL、Job、Stage 之间不应机械地按一一对应计数，优化和执行方式会影响实际组织。

```text
Driver 接收查询 → 生成计划 → 组织 Job/Stage/Task
 → 执行资源读取各分区 → 必要的数据交换与合并 → 返回结果
```

## 三 计划说明怎样算，运行界面说明算得怎样

查询计划解释计算怎样组织，运行界面说明它实际用了多少资源、哪些任务慢或失败。例如对地区聚合查看计划：

```sql
EXPLAIN FORMATTED
SELECT region, COUNT(*)
FROM clean_orders
GROUP BY region;
```

计划中重点识别局部聚合、`Exchange` 和最终聚合，具体算子、交换次数与任务数取决于版本、输入和配置。局部聚合可先压缩输入分区内的数据；[自适应执行](https://spark.apache.org/docs/3.5.7/sql-performance-tuning.html#adaptive-query-execution)还可能调整后续组织方式，不能用一个配置值机械推断最终任务数。

在实际作业的 SQL、Jobs、Stages、Executors 信息中关联输入、交换、耗时和失败。应用结束后是否可回看，取决于事件日志和 History Server 配置。[官方运行观察说明](https://spark.apache.org/docs/3.5.7/monitoring.html)给出指标与历史记录的职责。

## 四 用运行证据区分慢在哪里

| 证据 | 候选解释 | 继续核对 |
| --- | --- | --- |
| 少量任务输入和耗时远高于其他任务 | 数据倾斜或局部工作量过大 | key 分布、交换量、关联与聚合策略 |
| 大量任务都有较高磁盘读写和交换量 | 输入、Shuffle 或内存压力 | 读取范围、落盘、垃圾回收和资源配置 |
| 应用等待，尚无可执行任务资源 | 资源队列或申请条件 | 资源管理器、队列与实例请求 |
| 任务反复失败 | 输入、依赖、内存或外部连接问题 | 首个异常、失败任务和对应分区 |

固定样本不能证明生产性能，也不能根据某个地区占比直接认定聚合严重倾斜。当前任务的输入分布、局部聚合、交换量与耗时需要共同支持判断；优化前后使用可比较的口径和负载。

## 五 临时视图、持久化和重跑是三件事

`TEMP VIEW` 在当前会话提供查询定义。会话结束后不能据此认为结果已经保存为数据资产。

若把结果写到持久表或文件，还要明确：目标位置、格式、分区、权限、追加或覆盖策略，以及重跑是否重复。Spark 的 cache 用于复用计算结果，也不能替代持久输出和发布流程。

## 六 综合案例 订单批处理与执行证据

把步骤组织为：固定输入 → 过滤状态 → 本例完全重复行去重 → 按地区汇总 → 核对 23000 分 → 查看计划与任务 → 记录输出边界。

| 交付物 | 本例要求 |
| --- | --- |
| 口径与数据 | 最终状态、金额分、五行输入 |
| SQL 与结果 | 三笔有效订单、两行汇总 |
| 执行证据 | 计划、任务、运行参数与日志 |
| 重跑说明 | 临时计算结果和持久输出分别描述 |

## 七 业务验证

| 当前改动 | 必要业务证据 |
| --- | --- |
| SQL 口径或来源 | 数据粒度、有效状态、去重/版本规则及源明细对账 |
| 执行与性能 | 实际计划、输入和交换量、任务分布、等待与失败，不能只比较一次耗时 |
| 持久输出或重跑 | 目标位置、格式、分区、权限、覆盖/追加与重复处理规则 |
| 接入 Hive | 实际 Catalog、Metastore、表位置与读取身份一致 |

SQL 参考样本说明算法与口径；引擎执行、集群交换及性能结论以当前任务运行证据为准。通用学习 JDK 与 Spark 运行 JDK 按[版本说明](/参考资料/学习版本与中文资料/)分别管理。

[返回本单元](/02-大数据学习路线/03-Spark/README/) · [返回大数据路线](/02-大数据学习路线/README/)
