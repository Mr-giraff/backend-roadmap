---
{"title":"缓存与消息队列典型案例"}
---

# 缓存与消息队列典型案例

## 技能迁移表

| 前端已经会的事 | 后端对应能力 | 需要补上的差异 |
| --- | --- | --- |
| 暂存一次接口结果 | Redis 缓存 | 多实例共享，失效与权限范围需明确 |
| 给结果设置有效期 | TTL | 到期后可能同时产生大量数据库查询 |
| 发出事件，让其他模块处理 | Kafka 生产与消费 | 接收方是另一进程，按消费组保存读取进度 |
| 请求失败后重试 | 事件重放与幂等 | 同一事件可能已经成功，重放不能重复产生业务结果 |
| 控制并发请求数 | 分区分工与消费能力 | 分区数、处理时限、数据库和下游容量共同限制速度 |

从一次查询和一条通知事件理解 Redis、Kafka 的职责与保证，再定位项目中的客户端、配置、缓存键和消费提交。复杂缓存一致性与消息双写方案按当前问题进入 [服务间调用](/01-Java学习路线/08-缓存、消息与服务协作/服务间调用/README/)。

[一 缓存保存可重建的结果](/01-Java学习路线/08-缓存、消息与服务协作/Redis缓存/典型案例/#一-缓存保存可重建的结果)

[五 消息把工作交给独立消费者](/01-Java学习路线/08-缓存、消息与服务协作/Kafka消息队列/典型案例/#五-消息把工作交给独立消费者)

## 十 综合案例 商品展示与欢迎通知

把缓存和消息放回同一业务流程：

| 用户动作 | 应用处理 | 组件的职责 | 完成依据 |
| --- | --- | --- | --- |
| 查询商品 | 缓存未命中时查询数据库 | Redis 保存租户范围的副本 | 数据符合约定时效 |
| 修改商品 | 提交数据库，再使缓存失效 | Redis 副本按规则更新或重建 | 权威数据正确，副本可恢复 |
| 新用户注册 | 保存用户并记录待通知事件 | Kafka 保存事件，欢迎通知组处理 | 对应 eventId 只有一条通知记录 |
| 消费者重启 | 按已提交位置继续读取 | Kafka 保存组进度，数据库约束阻止重复写 | 重复事件不重复产生通知 |

注册提交与事件发布之间的缺口，进入 [Outbox 与恢复](/01-Java学习路线/08-缓存、消息与服务协作/服务间调用/典型案例/)理解；保存后直接发送的顺序调用不构成完整一致性保证。

## 十一 业务验证

| 业务要求 | 需要的证据 |
| --- | --- |
| 租户与对象隔离 | 键的范围、可信身份来源及不同租户同编号的查询结果 |
| 展示时效与故障恢复 | 实际陈旧窗口、失效失败处理、回源流量和用户降级结果 |
| 重复事件不重复通知 | 稳定 eventId、数据库去重与通知结果、业务提交与位置推进的顺序 |
| 重启与分区变化可恢复 | 消费位置、重投处理、重平衡后的分工与未完成工作归属 |
| 积压有解释与恢复办法 | 各分区进度、处理速率、下游耗时及恢复后的业务结果 |

本文状态和 SQL 用于说明保证范围，未启动 Redis、Kafka 或 MySQL。项目是否满足时效、生产消费和恢复要求，需要实际客户端、配置和运行记录；控制台连通不能替代业务验证。Java 学习与公司兼容边界见[学习版本与中文资料](/参考资料/学习版本与中文资料/)。

## 参考资料

- **中文主读**：[廖雪峰访问 Redis](https://liaoxuefeng.com/books/java/springboot/integration/redis/index.html)；消息专项直接读官方资料。
- **Java 补充**：[Dev.java](https://dev.java/learn/)。
- **Redis**：[字符串](https://redis.io/docs/latest/develop/data-types/strings/)、[过期](https://redis.io/docs/latest/commands/expire/)、[内存淘汰](https://redis.io/docs/latest/develop/reference/eviction/)。
- **Kafka**：[4.2 Quick Start](https://kafka.apache.org/42/getting-started/quickstart/)、[Java 版本](https://kafka.apache.org/42/operations/java-version/)、[消费者 API](https://kafka.apache.org/42/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html)、[消费配置](https://kafka.apache.org/42/configuration/consumer-configs/)、[生产配置](https://kafka.apache.org/42/configuration/producer-configs/)、[消费组运维](https://kafka.apache.org/42/operations/basic-kafka-operations/)。

[返回 Roadmap](/01-Java学习路线/08-缓存、消息与服务协作/README/) · [返回总纲](/)
