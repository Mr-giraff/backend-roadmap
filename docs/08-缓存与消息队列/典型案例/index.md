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

本章学到能使用和排查 Redis、Kafka。先逐点看最小例子，再组合场景。命令仅操作隔离的本地学习环境；缓存一致性与消息双写等复杂方案按需进入 [09](/09-分布式系统与服务协作/README/)。

## 一 缓存保存可重建的结果

缓存让后续请求复用已经得到的结果。先确定哪份数据是最终依据，称为权威数据。

前端可能这样保存查询结果：

```javascript
const cache = new Map();
cache.set('product:7', { name: '键盘', price: 100 });
console.log(cache.get('product:7').price);
```

输出 100。页面刷新后 Map 可以消失；后端缓存也应该先回答“丢了以后从哪里恢复”。

| 数据 | 本例放在哪里 | 丢失后的处理 |
| --- | --- | --- |
| 商品当前价格 100 | 数据库商品记录 | 按数据库备份和恢复策略处理 |
| 商品查询结果的副本 | Redis | 重新查询数据库并回填 |

缓存命中只是找到了副本，不保证副本仍是最新值。支付、库存判断等不能容忍旧值的决策，应按业务要求读取或修改权威数据。

## 二 用 Redis 保存值并设置过期时间

Redis 是独立运行的服务，不是当前 JVM 里的 Map。不同应用进程可以访问它，因此缓存键需要包含租户与对象范围。

在已经可用的本地学习 Redis 中执行：

```bash
redis-cli -h 127.0.0.1 -p 6379 SET roadmap:t1:product:7 'price=100' EX 60
redis-cli -h 127.0.0.1 -p 6379 GET roadmap:t1:product:7
redis-cli -h 127.0.0.1 -p 6379 TTL roadmap:t1:product:7
```

预期依次得到 OK、`price=100` 和不大于 60 的剩余秒数。键中的 t1 表示租户 1，避免另一个租户的商品 7 误用同一结果。

| 命令 | 作用 |
| --- | --- |
| SET | 保存或覆盖值 |
| GET | 读取值，不存在时返回空结果 |
| EX 60 | 本次写入同时设置 60 秒有效期 |
| TTL | 查看剩余有效期；不存在为 -2，没有过期时间为 -1 |

用下面的命令删除这个演示键，再 GET 应得到空结果：

```bash
redis-cli -h 127.0.0.1 -p 6379 DEL roadmap:t1:product:7
```

TTL 是按时间失效；内存淘汰是在容量不足时按策略回收键。给了 TTL 不能保证它一定留到过期时刻。

## 三 未命中时查数据库，更新后使副本失效

旁路缓存由业务代码决定什么时候读缓存、读数据库和回填。先用具体值看读路径：

| 步骤 | Redis | 数据库 | 本次返回 |
| --- | --- | --- | --- |
| 第一次查询商品 7 | 没有键 | 查到价格 100 | 100，并回填缓存 |
| 第二次查询商品 7 | 命中价格 100 | 不查询 | 100 |
| 更新价格为 120 | 更新数据库提交后删除键 | 已提交 120 | 更新成功 |
| 再次查询 | 未命中，重新回填 | 查到 120 | 120 |

前端“更新成功后使查询缓存失效”的思路可以迁移，但后端并发会产生交错：

| 时刻 | 读请求 A | 写请求 B |
| --- | --- | --- |
| 1 | 缓存未命中，数据库读到 100 | |
| 2 | 暂未回填 | 提交新价格 120，删除缓存 |
| 3 | 把旧价格 100 写回缓存 | |

最后数据库是 120，缓存却是 100。因此“先改库再删缓存”仍需配合允许陈旧窗口、过期和恢复手段，不能直接证明强一致。

本例可约定普通展示允许旧值最多 60 秒，最终下单价格由数据库重新确定。60 秒是业务要求，单设 TTL 并不能证明它已经满足；延迟回填和失效失败还要有验证与恢复。若连展示也不能接受旧值，可先直接读权威数据，再评估更复杂方案的成本。

## 四 缓存失败时控制回源量

回源就是缓存没有提供结果后，去权威存储读取。下列三类现象要分开处理：

| 输入或故障 | 直接后果 | 最小处理与边界 |
| --- | --- | --- |
| 不存在的商品被反复查询 | 每次都查数据库 | 输入校验；短时间缓存“未找到”，但新建后可能暂时仍看不到 |
| 同一个热点键到期 | 很多请求同时查同一数据 | 合并同键查询或限制回源并发；需要超时和失败收尾 |
| 大量键同时到期或 Redis 不可达 | 数据库流量突然增加 | 分散到期时间，限制回源，必要时拒绝或返回明确降级结果 |

例如数据库最多承受每秒 200 次此类查询，Redis 故障后有每秒 2000 个请求，不能让 2000 个请求全部无条件回源。

先规定“最多 150 次回源/秒，其他请求按业务规则降级或拒绝”，再观察数据库负载和用户结果。缓存命中率不是唯一指标，还要看回源次数、陈旧时间、失败率和热点分布。

## 五 消息把工作交给独立消费者

Kafka 保存事件，消费者按自己的进度读取。生产者不必等待欢迎通知真正写入，代价是要分开跟踪事件与业务结果。

先定义一条业务事件：

```json
{"eventId":"evt-1","userId":7,"type":"WELCOME_REQUESTED"}
```

`eventId` 标识同一次业务事件；重复发送仍保留这个编号。`userId` 是业务对象，两者用途不同。

| 名称 | 在本例里是什么 |
| --- | --- |
| producer | 注册服务，把事件写给 Kafka |
| broker | 保存、复制并提供读取记录的 Kafka 服务进程 |
| controller | 管理 Kafka 集群元数据与分区 leader 等协调工作；4.2 使用 KRaft |
| topic | 一类事件的名称，例如 roadmap.welcome |
| partition | topic 中的一条有序记录序列；不同分区可分给不同消费者 |
| consumer / group | 读取程序 / 一组协作读取程序；各组保存独立进度 |
| offset | 某分区内记录的位置，不能单独当成全局业务事件编号 |

```text
注册服务 → topic 的某个 partition → 欢迎通知消费组 → 通知数据库
                                      ↓
                                  统计消费组
```

两个消费组可分别处理同一批事件。同一个普通消费组内，一个分区同一时刻分配给一个消费者；增加消费者可以重新分工，超过分区数的消费者可能空闲。应用连接 broker，controller 负责集群协调，并不替应用执行通知业务。[Kafka 概念](https://kafka.apache.org/42/getting-started/introduction/)

## 六 先观察一条消息怎样进入 Kafka

以下命令在已经解压的 Kafka **4.2** 分发目录执行，假设独立学习 broker 在 `127.0.0.1:9092` 可用，且 topic 名未被其他练习使用。服务启动可按 [官方 Quick Start](https://kafka.apache.org/42/getting-started/quickstart/)准备；本章不自动格式化数据目录或搭建生产集群。JDK 支持范围按所选发行版的官方文档核对；公司 Java 8 工程另按实际框架和客户端版本选择依赖。

创建两分区、单副本的学习 topic：

```bash
bin/kafka-topics.sh --bootstrap-server 127.0.0.1:9092 \
  --create --topic roadmap.welcome --partitions 2 --replication-factor 1
bin/kafka-topics.sh --bootstrap-server 127.0.0.1:9092 \
  --describe --topic roadmap.welcome
```

预期看到两个分区。单副本便于观察；它不能容忍该副本所在 broker 的数据丢失。生产集群的副本、controller 和 broker 部署由运维方案决定。

打开一个终端启动生产者：

```bash
bin/kafka-console-producer.sh --bootstrap-server 127.0.0.1:9092 \
  --topic roadmap.welcome --property parse.key=true --property key.separator=:
```

输入下列一行，再用 Ctrl-D 退出：

```text
7:{"eventId":"evt-1","userId":7,"type":"WELCOME_REQUESTED"}
```

冒号左侧的 `7` 是 key，右侧 JSON 是 value。在默认按 key 选分区的配置下，同一 key、相同分区数会落到同一分区；这里不预设一定是 0 还是 1。业务若要求同一对象顺序，还需控制生产顺序、分区策略和消费处理方式。

另一个终端观察记录：

```bash
bin/kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 \
  --topic roadmap.welcome --group roadmap-welcome-observe \
  --from-beginning --max-messages 1 \
  --property print.partition=true --property print.offset=true --property print.key=true
```

新 topic 的预期是一条含分区、offset、key `7` 和 JSON 的记录；具体格式以本地工具输出为准。控制台读取只证明“事件能被读取”，不证明业务已写入通知。

再用新的组 `roadmap-stat-observe` 执行同一命令，它也能读到该事件。`--from-beginning` 在该组没有可用已提交位置时选择较早的位置；已有提交时，它不会自动覆盖进度。Kafka 读取后仍按保留策略保存记录，其他消费组可以继续读取。控制台工具的自动提交行为也不能作为业务提交顺序的证据。

## 七 写入结果与消费位置分别说明什么

生产者发送结果、消费者当前位置、消费者已提交位置，回答三个不同问题：

| 观察项 | 能说明什么 | 尚不能说明什么 |
| --- | --- | --- |
| 生产者收到成功结果 | 写入满足当前生产与 broker 配置的要求 | 通知业务已经完成 |
| `poll` 返回记录 | 消费者已经取得这一批记录，读取位置已向前推进 | 业务处理成功或恢复位置已提交 |
| 提交 offset 成功 | 此消费组记录了下次恢复的位置 | 外部数据库业务自动只执行一次 |

例如某分区依次读到 offset `0、1、2`。只完成了 0、1 的业务时，可提交的下一位置是 **2**；若把 3 提交了，就把尚未完成的 2 也越过去了。各分区各有位置，不能把不同分区 offset 作大小比较。[位置与手动提交](https://kafka.apache.org/42/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html)

业务消费者关闭自动提交，按下面的顺序处理。这里是行为伪代码，SQL 在下一节具体展开：

```text
enable.auto.commit = false
poll 取得记录
按分区逐条处理：
    开启数据库事务
    写入该事件的通知；已处理事件则核对已有结果
    提交数据库事务
    记录该分区已连续完成的位置
提交这些分区的下一读取位置
```

| 停止位置 | 数据库 | 重启后的可能结果 |
| --- | --- | --- |
| 数据库提交前 | 没有通知 | 从旧提交位置重新读取并处理 |
| 数据库提交后、offset 提交前 | 已有通知 | 再次读取同一事件，需要业务去重 |
| offset 提交后 | 已有通知 | 从已提交位置继续，记录仍按 Kafka 保留策略保存 |

**不提交 offset 并不会让当前进程下一次 `poll` 自动回到失败记录。** 失败时需要停止处理并重启，或明确暂停、调整位置后再重试；不能继续处理后续记录再越过失败位置提交。

消费者加入、退出或处理过慢，可能引起分区重新分配。此时提交可能失败，业务却已经完成。初学先保持单线程顺序处理，理解恢复和去重；涉及并行、暂停和重平衡回调时再补专门实现。[消费配置](https://kafka.apache.org/42/configuration/consumer-configs/)

生产者幂等与 `acks=all` 也不能替代上述数据库去重。它们保障的范围和结果还受副本等配置影响；“发送没有重复写入”不等于“外部业务恰好执行一次”。[生产配置](https://kafka.apache.org/42/configuration/producer-configs/)

## 八 用数据库保证同一事件只产生一条记录

本例把通知限定为数据库里的一条站内通知。先在独立 MySQL 学习库中创建：

```sql
CREATE TABLE inbox_notice (
    event_id VARCHAR(64) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content VARCHAR(200) NOT NULL
) ENGINE=InnoDB;
```

第一次消费 `evt-1`：

```sql
START TRANSACTION;
INSERT INTO inbox_notice(event_id, user_id, content)
VALUES ('evt-1', 7, '欢迎使用');
COMMIT;
SELECT event_id, user_id FROM inbox_notice;
```

预期为一行 `evt-1, 7`，再提交该分区对应的下一消费位置。第二次直接插入同一 `event_id` 会触发唯一键冲突；消费者需识别这条约束冲突、回滚本次事务，再核对已有记录确实属于同一事件和相同业务内容。确认已成功处理后，才可以推进该事件的消费位置。其他数据库异常按失败处理。

若还要修改其他业务表，应将“去重记录＋业务修改”放在同一个本地事务。给真正的邮件或短信发送只加这张表，不能证明外部动作恰好一次；对应恢复问题按需进入 [09](/09-分布式系统与服务协作/典型案例/)。

## 九 重试、顺序与积压

Kafka 记录不会因为业务失败就自动重新入队。应用或消费框架需要决定重试和隔离方式：

| 情况 | 基础处理选择 | 边界 |
| --- | --- | --- |
| 数据库短暂不可用 | 有限重试，保留未完成位置；必要时暂停对应分区 | 暂停和重试需维持消费协议要求，不能无限阻塞处理线程 |
| 消息缺必填字段 | 记录错误并按明确策略停止或隔离 | 普通消费者没有自动死信流程；隔离后才推进原位置也有失败窗口 |
| 相同事件再次读取 | 核对已有业务结果后按幂等规则处理 | 不能每次重放换 eventId |

重试 topic、隔离 topic 和受控重放属于后续实现选择；跨 topic 的转移与原位置提交不能凭两次普通调用就证明原子性。

顺序先按业务对象讨论。把同一用户的事件放在同分区，并按顺序处理，可以保持该分区的处理顺序；多个分区没有统一先后，异步并行也可能让业务完成顺序改变。增加分区数可能改变 key 的映射，因此不能只靠 key 对跨扩容过程作全程顺序承诺。

查看消费组进度：

```bash
bin/kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 \
  --describe --group roadmap-welcome-observe
```

关注各分区 `CURRENT-OFFSET`、`LOG-END-OFFSET` 和 `LAG`；刚创建且未提交的组可能没有可用位置。[消费组运维](https://kafka.apache.org/42/operations/basic-kafka-operations/)

LAG 是位置差，不必等于精确的未处理业务条数，offset 也不保证连续。处理进程落后、已读但未提交、数据库变慢都可能影响观察。先结合处理速率和数据库结果定位，再决定是否增加消费者；单分区的热点不会自动被多个同组消费者共同分摊。

若另有业务计数证明积压 6000 条，每秒新增 20 条、处理 50 条，在速率稳定时预计清空时间为 `6000 ÷ (50 - 20) = 200 秒`。处理速度不高于新增速度时，等待无法清空积压。

## 十 综合案例 商品展示与欢迎通知

把缓存和消息放回同一业务流程：

| 用户动作 | 应用处理 | 组件的职责 | 完成依据 |
| --- | --- | --- | --- |
| 查询商品 | 缓存未命中时查询数据库 | Redis 保存租户范围的副本 | 数据符合约定时效 |
| 修改商品 | 提交数据库，再使缓存失效 | Redis 副本按规则更新或重建 | 权威数据正确，副本可恢复 |
| 新用户注册 | 保存用户并记录待通知事件 | Kafka 保存事件，欢迎通知组处理 | 对应 eventId 只有一条通知记录 |
| 消费者重启 | 按已提交位置继续读取 | Kafka 保存组进度，数据库约束阻止重复写 | 重复事件不重复产生通知 |

注册提交与事件发布之间的缺口，进入 [09 Outbox 与恢复](/09-分布式系统与服务协作/典型案例/)单独学习。基础练习先明确这个边界，不能把保存后直接发送的顺序调用当成完整一致性保证。

## 十一 练习与验收

1. 两个租户都有商品 7，验证键不会覆盖；用 SET/GET/TTL/DEL 留下值和过期的实际记录。
2. 在独立 Kafka 4.2 环境创建两分区 topic，发布带 key 事件；两个不同组都能读取，并说明各组的独立进度。
3. 在同组启动两个持续消费进程，观察分区分工；再增加第三个，解释分区数对并行度的限制。使用持续消费命令时去掉 `--max-messages 1`。
4. 做消费位置与业务结果的两个实验：先用新组重读相同事件观察位置；再用真实业务消费者模拟“数据库提交后、offset 提交前停止”，检查通知仍只有一行。控制台读取不替代第二个实验。
5. 记录暂停与恢复前后的各分区 LAG、处理速率和业务结果，解释瓶颈及恢复用时。

验收需有实际环境记录。本文命令、SQL 与状态说明经过静态核对；没有启动 Redis、Kafka 或 MySQL，生产消费、重平衡与故障恢复均需在学习环境执行。Java 学习与公司兼容边界见 [学习版本与中文资料](/参考资料/学习版本与中文资料/)。

## 参考资料

- **中文主读**：[廖雪峰访问 Redis](https://liaoxuefeng.com/books/java/springboot/integration/redis/index.html)；消息专项直接读官方资料。
- **Java 补充**：[Dev.java](https://dev.java/learn/)。
- **Redis**：[字符串](https://redis.io/docs/latest/develop/data-types/strings/)、[过期](https://redis.io/docs/latest/commands/expire/)、[内存淘汰](https://redis.io/docs/latest/develop/reference/eviction/)。
- **Kafka**：[4.2 Quick Start](https://kafka.apache.org/42/getting-started/quickstart/)、[Java 版本](https://kafka.apache.org/42/operations/java-version/)、[消费者 API](https://kafka.apache.org/42/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html)、[消费配置](https://kafka.apache.org/42/configuration/consumer-configs/)、[生产配置](https://kafka.apache.org/42/configuration/producer-configs/)、[消费组运维](https://kafka.apache.org/42/operations/basic-kafka-operations/)。

[返回 Roadmap](/08-缓存与消息队列/README/) · [返回总纲](/)
