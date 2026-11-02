---
{"title":"Flink Roadmap"}
---

# Flink Roadmap

| 类别 | 作用 | 前端类比 | 类比边界 |
| --- | --- | --- | --- |
| 流与时间窗口 | 从持续事件得到分段结果 | 定时刷新累计数 | 事件时间和到达时间不同，窗口需要结束规则 |
| 状态与恢复 | 保存运行中的计算进度 | 保存页面编辑状态 | 输入位置、内部状态、外部输出需共同核对 |
| 并行与背压 | 分配计算并识别处理瓶颈 | 上游请求速度超过处理速度 | 未处理数据可能在消息系统或多个环节积压 |

目标是理解窗口计算的时间与状态规则，并能判断持续流运行、检查点和故障恢复怎样影响业务结果。前置为 [SQL](/05-数据库设计与SQL/README/)与[共同基础](/15-大数据组件与数据链路/01-分布式计算基础/README/)。

## 学习顺序

先明确业务问题，再理解计算与运行保证，读对应资料，最后在 SQL、代码、配置和计划中定位。

| 业务问题 | 概念与保证 | 代码与讲解入口 |
| --- | --- | --- |
| 持续事件从哪里来、在哪里计算、输出到哪里 | Source、算子、Sink 与 JobManager/TaskManager 的职责 | [流与执行角色](/15-大数据组件与数据链路/04-Flink/典型案例/#一-持续输入经过-source算子和-sink) |
| 按发生时间还是到达时间统计，窗口什么时候产生结果 | 事件时间、处理时间、窗口边界、Watermark 和空闲输入 | [时间与窗口](/15-大数据组件与数据链路/04-Flink/典型案例/#二-事件时间和处理时间可以不同) |
| 时间恰在边界或输入结束时，事件归属和输出怎样决定 | 滚动窗口 SQL 与有限输入完成 | [窗口口径](/15-大数据组件与数据链路/04-Flink/典型案例/#三-用三个固定事件验证窗口边界) |
| 重启从哪里继续，内部状态和外部写入是否一致 | 状态、Checkpoint、Savepoint、输入回放与 Sink 提交 | [恢复时间线](/15-大数据组件与数据链路/04-Flink/典型案例/#四-状态保存到哪里失败后从哪里继续) |
| 积压为什么增长，上游变慢是否意味着上游是瓶颈 | 速率、背压、慢 Sink、热点任务和运行指标 | [处理瓶颈](/15-大数据组件与数据链路/04-Flink/典型案例/#五-背压说明下游处理速度限制了上游) |

时间与窗口查 [Flink 1.17 中文窗口资料](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/dev/table/sql/queries/window-tvf/)，恢复查[检查点](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/ops/state/checkpoints/)与[保存点](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/ops/state/savepoints/)。参考 SQL 使用 Flink 1.17 的 filesystem/CSV；实际引擎 JDK、连接器和权限独立匹配。Kafka 输入结合 [08](/08-缓存与消息队列/README/)，外部一致性结合 [09](/09-分布式系统与服务协作/README/)。复杂状态算子、源码和高可用集群按职责深入。

## 综合案例与业务验证

[分钟点击案例](/15-大数据组件与数据链路/04-Flink/典型案例/#六-综合案例-窗口正确与恢复正确分别验证)用于解释时间、状态和输出的不同保证。项目中沿事件字段、Watermark、算子状态和 Sink 找到实际规则；有限文件窗口结果不代表持续流乱序与恢复已验证。

窗口或恢复逻辑变更时，核对事件时间、边界、迟到规则、输入位置、已完成检查点与最终输出。涉及吞吐和积压时，使用输入、消费、输出速率与算子指标判断瓶颈。端到端处理保证要同时确认 Source、状态与 Sink 的协作，不能只看检查点成功。

## 参考资料

- **中文前置**：[廖雪峰 SQL](https://liaoxuefeng.com/books/sql/introduction/index.html)；Java 背景用 [Dev.java](https://dev.java/learn/)补充。
- **中文专项**：[Flink 1.17 架构](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/concepts/flink-architecture/)、[窗口函数](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/dev/table/sql/queries/window-tvf/)、[Checkpoint](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/ops/state/checkpoints/)、[Savepoint](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/ops/state/savepoints/)、[背压](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/ops/monitoring/back_pressure/)。
- **连接器核对**：[文件系统](https://nightlies.apache.org/flink/flink-docs-release-1.17/docs/connectors/table/filesystem/)与 [CSV](https://nightlies.apache.org/flink/flink-docs-release-1.17/docs/connectors/table/formats/csv/)。

[返回大数据路线](/15-大数据组件与数据链路/README/) · [返回总纲](/)
