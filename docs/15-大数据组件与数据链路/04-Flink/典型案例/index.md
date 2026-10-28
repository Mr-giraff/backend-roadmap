---
{"title":"Flink 典型案例"}
---

# Flink 典型案例

## 技能迁移表

| 前端经验 | Flink 对应能力 | 需要补上的差异 |
| --- | --- | --- |
| 订阅事件后更新列表 | Source、算子、Sink | 分布式任务会持续运行，输入可能重放 |
| 按时间段统计页面数据 | 事件时间与窗口 | 迟到和乱序需要明确规则 |
| 保存当前编辑状态 | 有状态计算与检查点 | 状态恢复还要对应输入位置 |
| 请求堆积、处理缓慢 | 背压与积压 | 要找到限制速度的算子或外部输出 |

## 一 持续输入经过 Source、算子和 Sink

```text
Source 读取点击事件 → 算子分窗口、计数 → Sink 输出统计结果
```

Source 是输入来源，算子执行转换或计算，Sink 是输出位置。它们是数据处理职责。

运行中，JobManager 协调作业和恢复；TaskManager 执行分配的并行任务。一个 TaskManager 可以执行多个任务，Slot 是调度资源的抽象，不能直接等同于一颗独占 CPU。[Flink 中文架构](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/concepts/flink-architecture/)

## 二 事件时间和处理时间可以不同

| 事件 | 事件时间 | 到达处理程序的时间 |
| --- | --- | --- |
| A | 10:00:10 | 10:00:11 |
| B | 10:00:08 | 10:00:12 |

B 后到，但发生得更早。按事件时间统计时，不能直接用收到数据的顺序替代业务时间。

一分钟滚动窗口按固定、不重叠的时间段统计：`[10:00,10:01)` 包含 10:00:59，10:01:00 进入下一个窗口。

Watermark 表达系统估计的事件时间进度，用于推动窗口计算。它不能证明未来绝不会再来更早的数据。持续输入存在多个分区时，某个分区不再发数据也可能影响进度，需要另查空闲输入设置和实际 Watermark。[Flink 窗口说明](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/dev/table/sql/queries/window-tvf/)

## 三 用三个固定事件验证窗口边界

输入[clicks.csv](/assets/15-大数据组件与数据链路/examples/clicks.csv)：

```text
1,2026-10-26 10:00:10.000
2,2026-10-26 10:00:20.000
3,2026-10-26 10:01:01.000
```

在有兼容 Flink 1.17、filesystem/CSV 的独立单机学习环境准备文件：

```bash
cd ./15-大数据组件与数据链路
cp examples/clicks.csv /tmp/study15-clicks.csv
```

在 SQL Client 执行：

```sql
SET 'execution.runtime-mode' = 'streaming';

CREATE TEMPORARY TABLE lesson_clicks (
    user_id INT,
    event_time TIMESTAMP(3),
    WATERMARK FOR event_time AS event_time - INTERVAL '5' SECOND
) WITH (
    'connector' = 'filesystem',
    'path' = 'file:///tmp/study15-clicks.csv',
    'format' = 'csv',
    'csv.timestamp-format.standard' = 'SQL'
);

SELECT window_start, window_end, COUNT(*) AS click_count
FROM TABLE(
    TUMBLE(TABLE lesson_clicks, DESCRIPTOR(event_time), INTERVAL '1' MINUTE)
)
GROUP BY window_start, window_end;
```

完整脚本为 [window.sql](/assets/15-大数据组件与数据链路/examples/window.sql)。有限输入结束、窗口完成后，预期：

| window_start | window_end | click_count |
| --- | --- | --- |
| 2026-10-26 10:00:00 | 2026-10-26 10:01:00 | 2 |
| 2026-10-26 10:01:00 | 2026-10-26 10:02:00 | 1 |

SQL Client 显示的行变化标识和时间格式可能不同。本地路径必须对实际执行进程可读；集群任务不能只依赖个人电脑的 `/tmp` 文件。

静态文件会快速结束，适合验证边界。只调整文件行顺序不能保证复现某个迟到时刻；Watermark 的生成和实际推进需要观察。

## 四 状态保存到哪里，失败后从哪里继续

窗口未完成时，计数需要保存在运行状态中。Checkpoint 定期保存可恢复的一致进度，Savepoint 常由用户发起，用于受控停止、迁移或升级等操作。[Checkpoint](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/ops/state/checkpoints/)、[Savepoint](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/ops/state/savepoints/)

考虑一条简化时间线：

```text
已完成检查点：输入位置 100，累计值 12
处理下一条：输入位置 101，累计值 13
外部系统已经写入结果 → 执行进程退出
恢复检查点：累计值回到 12，重新处理 101
```

内部状态可以随重放恢复，但外部结果是否重复取决于 Sink 的写入方式、事务或幂等机制。需要分别检查：

| 环节 | 本次要证明什么 |
| --- | --- |
| Source | 能从恢复位置重读，事件编号与输入顺序可追踪 |
| 状态 | 从哪个已完成 Checkpoint 或 Savepoint 恢复 |
| Sink | 输出是否覆盖、去重或参与提交协议 |
| 对账 | 故障前后是否少算、重复或残留旧结果 |

有限文件实验很快结束，不适合据此证明持续流恢复。后续用可持续、可回放的输入源，设置并观察检查点；运行中终止一个执行进程，再记录实际恢复位置与输出。

迟到也单独实验：窗口已按 Watermark 关闭后，再送入该窗口的旧事件，记录实际丢弃、更新或补算行为。SQL 窗口与 DataStream 窗口的配置和输出规则需分别查阅，不直接照搬另一套 API 的迟到选项。

## 五 背压说明下游处理速度限制了上游

假设输入持续产生 1000 条/秒，而输出只能处理 200 条/秒。在这个简化条件下，未处理记录约以 800 条/秒增长，可能积在输入消息系统或中间缓冲中。

背压会把下游的处理限制传到上游，使上游处理变慢；积压描述的是还没处理完的数据量。二者相关，但观察位置不同。

在 Web UI 对比各算子的输入输出、Busy/Backpressured/Idle 指标与检查点情况。先找到慢 Sink、热点任务或外部服务瓶颈，再判断调整并行度是否有效。[Flink 中文背压监控](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/ops/monitoring/back_pressure/)

## 六 综合案例 窗口正确与恢复正确分别验证

| 实验 | 输入与操作 | 预期或判断依据 |
| --- | --- | --- |
| 窗口边界 | 三个固定事件 | 两个窗口分别为 2、1 |
| 乱序与迟到 | 持续输入并观察 Watermark 后发送旧事件 | 根据实际时间进度和选定 API 规则核对 |
| 故障恢复 | 检查点完成后继续输入，再停止执行进程 | 记录恢复点、回放和最终结果 |
| 输出变慢 | 限制学习 Sink 的处理速度 | 对比上游背压、输入积压和各算子指标 |

窗口结果正确不直接证明恢复正确。最后把四项结果与 Source、内部状态和 Sink 的契约放在一起解释。

## 七 练习与验收

1. 把第三条事件改为 10:01:00，预测并验证它仍属于后一个窗口。
2. 在持续流中先观察 Watermark，再发送旧事件；保留事件编号、发生和发送时间、输出结果。
3. 用选定的可回放输入与 Sink 做一次恢复实验，记录 Checkpoint 编号、故障、恢复与对账。
4. 对一个积压现象，区分输入产生速率、实际消费速率和输出速率，说明瓶颈位置。

本篇有限样本预期可手算；持续流、连接器、检查点和外部输出的恢复保证需要实际环境验证。通用 Java 25 与 Flink 运行 JDK 分别核对。

[返回本单元](/15-大数据组件与数据链路/04-Flink/README/) · [返回大数据路线](/15-大数据组件与数据链路/README/)
