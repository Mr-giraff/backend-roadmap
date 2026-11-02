---
{"title":"并发与异步编程典型案例"}
---

# 并发与异步编程典型案例

## 技能迁移表

| 前端已有经验 | Java 中的对应能力 | 需要补上的差异 |
| --- | --- | --- |
| 多个异步请求同时进行 | 多线程与任务并发 | Java 线程可能真正并行访问同一对象 |
| 避免旧响应覆盖新状态 | 共享状态与竞争分析 | 还要考虑可见性、原子性和执行顺序 |
| 限制批量请求并发 | 线程池与有界队列 | 线程、排队任务及连接持续占用资源 |
| Promise、Promise.all | Future、CompletableFuture | 等待可能阻塞；取消或超时不等于工作停止 |
| 将用户和项目传给请求 | 显式上下文传递、ThreadLocal | 线程池会复用线程，残留数据可能串请求 |

按“线程 → 共享状态 → 同步 → 集合 → 线程池 → 结果与取消 → 上下文 → 虚拟线程”学习。主线使用当前学习 JDK；基础写法保留跨版本可读性，公司 Java 8 不直接使用现代 API。版本边界见 [学习版本与中文资料](/参考资料/学习版本与中文资料/)。

从服务中的共享对象、后台任务和下游等待进入，解释执行者、状态所有权、资源上限与失败收尾。具体 API 随项目写法查阅，虚拟线程按实际 JDK 和工作负载选择。

## 一 线程让另一条执行路径开始工作

一个请求等待数据库或远程服务时，它的执行线程可能阻塞；其他请求能否继续，取决于服务的线程与资源模型。Thread 表示一条执行路径，`start()` 请求启动它，`join()` 等待其结束；直接调用 `run()` 仍是当前线程中的普通方法调用。

前端 Promise 描述异步结果，不等于为每个回调创建线程。并发指任务时间上有交错，并行指同一时刻可能同时运行，阻塞指当前线程等待期间不能继续执行后面的工作。阅读代码先定位执行器、任务提交与等待位置，再判断会同时访问什么。

## 二 共享状态中的读改写可能交错

假设两个请求都要把剩余额度减一，但实现把读取和写入分开：

| 时间 | 请求 A | 请求 B | 已保存余额 |
| --- | --- | --- | --- |
| 1 | 读到 20 | | 20 |
| 2 | | 读到 20 | 20 |
| 3 | 写回 19 | | 19 |
| 4 | | 写回 19 | 19 |

两次业务扣减只留下了一次变化，叫丢失更新。`count++` 同样包含读取、计算、写回，不能把一行代码当作不可分割的动作。

共享指多个线程能访问同一个对象；局部变量引用的对象也可能共享。检查 Spring 单例 Bean 的可变字段、static 集合和外部缓存，先问清数据由谁持有、谁能修改、哪条规则必须共同成立。跨实例扣减还要由数据库约束或条件更新等机制保护，不能只加 JVM 内锁。

## 三 锁、volatile 与原子类分别保证什么

### 1. synchronized 保护一段共同操作

同步块或同步方法让使用同一锁的线程轮流执行临界区。业务若要求两个字段保持关系，读写这两个字段的所有相关路径都必须遵守同一保护规则。

锁不仅互斥，还建立先前写入对后续获取同一锁的线程可见的关系。锁错对象，或有一条路径绕过锁，都可能破坏保证。

### 2. volatile 提供可见性，不合并复合操作

```java
class Signal {
    volatile boolean stopped;
}
```

一个线程更新 stopped，其他线程通过对应规则看到变化。它适合表达这种状态信号；将计数器字段改成 volatile，仍不能把读取、加一、写回合并成一个原子操作。

Java 内存模型中的 happens-before 描述哪些先前写入对后续操作可见。先掌握锁、volatile、Thread.start/join 和任务提交/取结果的常用关系，不靠“代码写在前面”推断跨线程顺序。

### 3. AtomicInteger 直接提供原子加一

AtomicInteger 的 incrementAndGet 等方法保证单次操作原子化；但两个原子变量分别修改，不能自动保证“余额总和不变”。选择工具时，先确定业务规则跨越几个对象、几个操作和几个提交边界。

ReentrantLock 可提供显式锁操作，释放应放 finally。选择锁或原子类前，先尝试局部变量、不可变对象和明确所有权来减少共享。

## 四 并发集合保护具体操作，不包办业务规则

普通 HashMap 不是多线程共享写入工具。ConcurrentHashMap 提供并发操作；先导入 `java.util.concurrent.ConcurrentHashMap`：

```java
ConcurrentHashMap<String, String> tasks = new ConcurrentHashMap<>();
String previous = tasks.putIfAbsent("export-1", "RUNNING");
System.out.println(previous);
System.out.println(tasks.get("export-1"));
```

先输出 null，再输出 RUNNING。同一个键已有值时 putIfAbsent 不替换并返回旧值，因此适合“仅当不存在时创建”的单次操作。

但“先 size 小于上限，再 put”是两个操作，不会因为集合线程安全就自动满足容量规则。跨多个键、多个方法或数据库的约束，都要另行设计。

BlockingQueue 可用于生产消费；Semaphore 限制同时进入的任务数量；CountDownLatch 等待一批已知任务完成。每种工具都应说明共享对象、等待条件和资源上限。

## 五 线程池要同时限制运行和排队数量

线程池复用线程并控制同时执行的任务。排队任务仍占用内存与等待时间，队列满时需要明确拒绝或降级结果；线程数也不能超过数据库连接和下游配额所能承受的范围。

| 状态 | 对业务的影响 |
| --- | --- |
| 执行位置可用 | 任务开始执行并占用下游资源 |
| 执行位置忙，仍可排队 | 用户等待变长，需要有排队时限和容量 |
| 执行与队列均已满 | 拒绝或降级必须明确返回，不能静默丢任务 |

在项目代码中沿 ThreadPoolExecutor 的配置追到提交方，核对核心/最大线程、队列、拒绝策略与任务生命周期；参数取值来自实际流量、耗时和资源预算。

用完执行 shutdown 停止接收新任务，并等待在途任务结束；超过关闭时限时再考虑 shutdownNow，它发送中断而非强制杀掉任意代码。

真实容量要结合 CPU、阻塞时间、数据库连接和下游配额测量。无界队列可能积压内存与延迟；最大线程数在无界队列策略下也可能长期不起作用。

## 六 Future 区分提交、完成与取结果

Future 表示稍后可取得的结果。submit 只说明任务已经交给执行器，get 可能阻塞等待，也会把任务失败带给调用方。任务仍在排队、正在执行、已经完成和调用方已经取得结果，是不同状态。

### 超时与取消对应不同的业务后果

`future.get(timeout, unit)` 只限制本次等待，超时后任务可能继续运行。`cancel(true)` 可以请求中断，但中断是协作信号；忽略中断的代码、部分 IO 和已提交的远端副作用，不会因为 Future 变为取消状态就自动消失。

例如用户取消导出，需要分别确认接口返回、后台任务、数据库状态和临时文件。任务的 finally 清理能处理本地资源，却不能自动撤销已经完成的外部操作。截止时间应覆盖排队和各下游调用，具体恢复约定结合 [09](/09-分布式系统与服务协作/README/)。

## 七 CompletableFuture 组合多个结果

聚合接口可能先查询一份基础信息，再并行取得互不依赖的补充数据。先画依赖，再决定哪些步骤值得并行：

```text
基础信息 → 补充来源 A ─┐
        → 补充来源 B ─┴→ 合并结果
```

thenApply 表达对前一步结果的转换，thenCombine 合并两个结果，handle/exceptionally 处理完成或失败。选择执行器、允许缺失的结果、异常如何返回和总体时限，都属于接口契约。

orTimeout 或 completeOnTimeout 可以限定 CompletableFuture 的完成结果，但不自动停止底层任务；公司 Java 8 没有这两个 API。`CompletableFuture.cancel(true)` 也不会因此中断底层计算。阅读异步代码时，应继续找到实际任务和下游的停止、清理与副作用边界。

## 八 上下文跟着任务传递，复用线程前后要清理

ThreadLocal 把值与当前线程关联。它不是当前用户的永久身份容器；线程池会把同一线程交给下一项任务。

```java
ThreadLocal<String> tenant = new ThreadLocal<>();
String tenantId = "tenant-A";
Runnable task = () -> {
    tenant.set(tenantId);
    try {
        System.out.println(tenant.get());
    } finally {
        tenant.remove();
    }
};
task.run();
```

tenantId 只示意显式传入的上下文；实际身份必须从可信认证上下文取得。这里的重点是任务执行前设置、finally 中清理，不能把线程本身当作用户身份。

没有清理可能残留，直接换线程则可能丢失。异步任务也不会自动共享调用方 Spring 本地事务；需要明确传递必要字段，并为持久化步骤定义自己的事务边界。

## 九 虚拟线程

虚拟线程让大量会等待的任务可以各自拥有一条轻量执行路径。它属于当前学习主线；线程安全、取消和数据库连接上限仍需按前面的方法单独保证。

在项目代码中找到逐任务虚拟线程执行器、结果等待与关闭位置。执行器退出时需要等待已提交工作结束，仍应检查任务是否可能长期等待。具体 API 写法参考 [VirtualThreadExample.java](/assets/07-并发与异步编程/examples/VirtualThreadExample.java) 与版本化文档，不以一次线程类型输出判断业务容量。

虚拟线程适合大量等待型任务，不会增加 CPU 核心或下游连接配额，也不能解决共享状态竞争。本节对应的[版本化 JDK 指南](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html)说明：虚拟线程在 `synchronized` 内执行阻塞操作时可能固定其载体线程，影响可伸缩性。实际使用时按应用 JDK 匹配指南与运行记录核对。不要为了复用而给虚拟线程设固定池；需要限制同时访问数据库等资源时，仍要用连接池或 Semaphore 等机制控制。公司 Java 8 没有这组 API，业务案例使用其可用的线程与执行器机制。

先读[廖雪峰虚拟线程](https://liaoxuefeng.com/books/java/threading/virtual-thread/index.html)，再用[Dev.java](https://dev.java/learn/new-features/virtual-threads/)核对任务模型；结构化并发等能力按所用 JDK 的正式/预览状态另行学习，默认不启用预览。

## 十 综合案例 让并发结果正确且资源有界

以一个聚合接口为例：它需要查询三份外部信息，允许一份非关键数据缺失，并且不能挤占同一服务中的其他请求。

| 业务问题 | 设计中的体现 |
| --- | --- |
| 哪些调用依赖前一步结果 | 任务依赖图决定串行和并行 |
| 总体多久应有明确结果 | 整体截止时间分配给排队、调用、重试和组装 |
| 哪份数据可以缺失 | 关键失败与部分返回分别定义，不能吞掉异常伪装成功 |
| 资源耗尽时怎样处理 | 执行器、队列、连接及下游并发有预算，拒绝有用户结果 |
| 请求结束以后还有什么继续运行 | 记录在途任务、清理与外部副作用，必要时进入恢复流程 |
| 身份与追踪怎样隔离 | 必要上下文显式传递，复用线程前后清理 |

工具选择需要回到这些要求；公共线程池或更多线程不会自动补齐业务保证。

## 十一 业务验证

| 当前需求涉及的风险 | 应保留的证据 |
| --- | --- |
| 并发修改共享状态 | 不变量、关键交错、最终数据和所有访问路径的保护规则 |
| 下游慢或过载 | 活动线程、排队时间、拒绝、连接占用及用户结果 |
| 超时或取消 | 调用方结果、实际任务终态、资源释放与仍在发生的副作用 |
| 线程复用与异步身份 | 可信身份来源、传递路径及不同请求的隔离结果 |
| 关闭与恢复 | 停止接收、在途任务收尾和未完成工作的归属 |

验证范围按实际改动风险选择。并发正确性不能由某次输出正确推断，任务取消也不能只检查 Future 状态。

## 参考与验证范围

- **中文主读**：[廖雪峰 多线程](https://liaoxuefeng.com/books/java/threading/index.html)与 [使用虚拟线程](https://liaoxuefeng.com/books/java/threading/virtual-thread/index.html)：同步、线程池、异步结果和现代线程模型。
- **Java 补充**：[Dev.java 虚拟线程](https://dev.java/learn/new-features/virtual-threads/)：任务模型、阻塞与资源限制。
- **API 核对（英文）**：[并发包](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/package-summary.html)、[ThreadPoolExecutor](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.html)、[Future](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Future.html)、[CompletableFuture](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/CompletableFuture.html)。
- **规范核对（英文）**：[线程与锁](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html)；公司 Java 8 实现另对照 [Java 8 并发 API](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html)。

参考源码的已有编译与运行记录见[示例验证说明](/参考资料/示例验证说明/)，其范围限于选定进程内机制。业务中的持续负载、真实下游、身份隔离及取消后的副作用，仍需对应实现和运行证据。

[返回本节 Roadmap](/07-并发与异步编程/README/) · [返回总纲](/)
