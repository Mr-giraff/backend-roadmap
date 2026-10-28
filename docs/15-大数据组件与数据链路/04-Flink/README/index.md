---
{"title":"Flink Roadmap"}
---

# Flink Roadmap

| 类别 | 作用 | 前端类比 | 类比边界 |
| --- | --- | --- | --- |
| 流与时间窗口 | 从持续事件得到分段结果 | 定时刷新累计数 | 事件时间和到达时间不同，窗口需要结束规则 |
| 状态与恢复 | 保存运行中的计算进度 | 保存页面编辑状态 | 输入位置、内部状态、外部输出需共同核对 |
| 并行与背压 | 分配计算并识别处理瓶颈 | 上游请求速度超过处理速度 | 未处理数据可能在消息系统或多个环节积压 |

目标是完成一个窗口计算，解释时间和结果，再理解持续流运行、状态、检查点及故障恢复。前置为 [SQL](/05-数据库设计与SQL/README/)与[共同基础](/15-大数据组件与数据链路/01-分布式计算基础/README/)。

## 学习顺序

| 顺序 | 范围 | 最小案例 | 过关证据 |
| --- | --- | --- | --- |
| 1 | Source、算子、Sink、JobManager、TaskManager | [流与执行角色](/15-大数据组件与数据链路/04-Flink/典型案例/#一-持续输入经过-source算子和-sink) | 画出数据与执行职责 |
| 2 | 事件时间、处理时间、窗口、Watermark | [时间与窗口](/15-大数据组件与数据链路/04-Flink/典型案例/#二-事件时间和处理时间可以不同) | 说明乱序与窗口完成条件 |
| 3 | 固定事件与滚动窗口 SQL | [有限输入实验](/15-大数据组件与数据链路/04-Flink/典型案例/#三-用三个固定事件验证窗口边界) | 两个窗口计数为 2、1 |
| 4 | 状态、Checkpoint、Savepoint、回放与输出 | [恢复时间线](/15-大数据组件与数据链路/04-Flink/典型案例/#四-状态保存到哪里失败后从哪里继续) | 分开核实内部状态与外部写入 |
| 5 | 速率、积压、背压、慢 Sink | [运行观察](/15-大数据组件与数据链路/04-Flink/典型案例/#五-背压说明下游处理速度限制了上游) | 用算子和输入输出指标找到限制环节 |

## 环境与实践范围

现有 SQL 样本使用 Flink 1.17 的 filesystem、CSV 与窗口函数；运行 JDK、连接器和权限按该版本核对，不直接使用通用学习 JDK 25 启动引擎。先在隔离单机环境运行有限文件样本，再补持续输入实验。

有限文件适合学习确定的输入输出，持续流的 Watermark、迟到和恢复要另做实验。使用 Kafka 时，可结合 [08](/08-缓存与消息队列/README/)的分区、消费位置与重放基础；连接器和外部一致性深入再结合 [09](/09-分布式系统与服务协作/README/)。

## 综合案例与验收

完成[分钟点击统计](/15-大数据组件与数据链路/04-Flink/典型案例/#六-综合案例-窗口正确与恢复正确分别验证)。

- [ ] 区分事件时间、处理时间、Watermark 和窗口边界。
- [ ] 使用 [clicks.csv](/assets/15-大数据组件与数据链路/examples/clicks.csv)与 [window.sql](/assets/15-大数据组件与数据链路/examples/window.sql)核对两行窗口结果。
- [ ] 在运行界面找到作业、算子、并行任务和检查点记录。
- [ ] 持续输入中单独验证乱序、迟到、重启与回放，保存输入输出证据。
- [ ] 解释背压与积压的区别，并定位一个处理速度受限的环节。
- [ ] 核对 Source、状态和 Sink 的配合后，再判断端到端处理保证。

不要求先实现复杂状态算子、研读源码或搭建高可用集群。性能调优、状态演进、升级和外部事务按具体任务深入。

## 参考资料

- **中文前置**：[廖雪峰 SQL](https://liaoxuefeng.com/books/sql/introduction/index.html)；Java 背景用 [Dev.java](https://dev.java/learn/)补充。
- **中文专项**：[Flink 1.17 架构](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/concepts/flink-architecture/)、[窗口函数](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/dev/table/sql/queries/window-tvf/)、[Checkpoint](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/ops/state/checkpoints/)、[Savepoint](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/ops/state/savepoints/)、[背压](https://nightlies.apache.org/flink/flink-docs-release-1.17/zh/docs/ops/monitoring/back_pressure/)。
- **连接器核对**：[文件系统](https://nightlies.apache.org/flink/flink-docs-release-1.17/docs/connectors/table/filesystem/)与 [CSV](https://nightlies.apache.org/flink/flink-docs-release-1.17/docs/connectors/table/formats/csv/)。

[返回大数据路线](/15-大数据组件与数据链路/README/) · [返回总纲](/)
