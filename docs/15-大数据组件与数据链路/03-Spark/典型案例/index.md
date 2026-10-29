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

在 Spark SQL 中执行：

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

在已有 Spark 学习环境，从仓库根目录执行：

```bash
cd ./15-大数据组件与数据链路
spark-sql --master 'local[2]' -f examples/batch.sql
```

完整输入与查询保留在 [batch.sql](/assets/15-大数据组件与数据链路/examples/batch.sql)。`local[2]` 用本地两个执行线程，适合核对计算；它不提供多机运行证据。

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

在同一会话定义一个有四个输入分区的样本：

```sql
CREATE OR REPLACE TEMP VIEW lesson_numbers AS
SELECT id, CASE WHEN id % 10 = 0 THEN 'south' ELSE 'east' END AS region
FROM range(0, 1000, 1, 4);

SET spark.sql.shuffle.partitions=2;

EXPLAIN FORMATTED
SELECT region, COUNT(*)
FROM lesson_numbers
GROUP BY region;

SELECT region, COUNT(*)
FROM lesson_numbers
GROUP BY region
ORDER BY region;
```

预期 east 900、south 100。四个输入分区来自 range 的最后一个参数。设置的两个 Shuffle 分区与实际最终任务数还可能受自适应执行影响。[Spark SQL 自适应执行](https://spark.apache.org/docs/3.5.7/sql-performance-tuning.html#adaptive-query-execution)

计划中寻找局部聚合、`Exchange` 和最终聚合；具体算子名称按版本与配置核对。局部计数先压缩每个输入分区的数据，再进行后续合并。

观察界面时，先启动交互会话，再粘贴 SQL，使应用在观察期间保持运行：

```bash
spark-sql --master 'local[2]'
```

运行日志通常给出 Driver Web UI 地址，默认从本机 4040 端口尝试分配。观察 SQL、Jobs、Stages、Executors 中的任务数量、输入、交换、耗时和失败。应用结束后是否可回看，取决于事件日志和 History Server 配置。[官方运行观察说明](https://spark.apache.org/docs/3.5.7/monitoring.html)

## 四 用运行证据区分慢在哪里

| 证据 | 候选解释 | 继续核对 |
| --- | --- | --- |
| 少量任务输入和耗时远高于其他任务 | 数据倾斜或局部工作量过大 | key 分布、交换量、关联与聚合策略 |
| 大量任务都有较高磁盘读写和交换量 | 输入、Shuffle 或内存压力 | 读取范围、落盘、垃圾回收和资源配置 |
| 应用等待，尚无可执行任务资源 | 资源队列或申请条件 | 资源管理器、队列与实例请求 |
| 任务反复失败 | 输入、依赖、内存或外部连接问题 | 首个异常、失败任务和对应分区 |

本例只有 1000 行，不能用它证明生产性能，也不能根据 east 占比直接认定聚合发生严重倾斜。先保存计划与实际指标，再选有明确瓶颈的学习任务比较修改前后效果。

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

## 七 练习与验收

1. 加一条完全重复订单，预测并验证原始行数和最终金额。
2. 给同一订单加一条不同金额，说明当前去重逻辑为什么不适用，并定义有效版本。
3. 比较原始记录聚合与先过滤、局部聚合后的交换信息，记录实际计划。
4. 为一个慢任务列出至少两个候选原因，用输入量、等待或交换指标区分。
5. 如果接入 Hive，保存实际 Catalog 与 Metastore 配置来源，确认两边读取的是同一份表和文件。

本篇样本结果可手算；未以阅读代替 Spark 引擎执行、集群交换或性能测试。通用当前学习 JDK 与 Spark 运行 JDK 按[版本说明](/参考资料/学习版本与中文资料/)分别管理。

[返回本单元](/15-大数据组件与数据链路/03-Spark/README/) · [返回大数据路线](/15-大数据组件与数据链路/README/)
