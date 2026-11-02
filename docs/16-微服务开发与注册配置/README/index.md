---
{"title":"微服务开发与注册配置 Roadmap"}
---

# 微服务开发与注册配置 Roadmap

| 类别 | 作用 | 前端类比 | 类比边界 |
| --- | --- | --- | --- |
| Spring Cloud | 组织配置、发现、调用和治理等微服务能力 | 应用接入一组协作工具 | 它是一组项目，需要按实际选型学习 |
| Nacos 配置管理 | 让服务从明确的位置读取配置 | 环境变量与远程配置 | 读取成功、收到更新、业务对象生效是三个检查点 |
| Nacos 服务发现 | 让调用方找到可用服务实例 | 根据名称解析请求地址 | 配置中心、注册中心和业务租户是不同概念 |
| Dubbo RPC | 通过接口代理调用另一进程中的服务 | 调用封装好的请求方法 | 写法像普通方法，仍会发生网络失败和远端副作用 |
| 网关与身份 | 观察入口路由和身份如何交给下游 | 请求代理与登录状态 | 网关认证不替代业务服务的对象权限检查 |

## 定位与前置条件

这是服务开发主线。完成 [01 Java](/01-Java语言与JVM基础/README/)、[02 Maven 与启动](/02-工程构建与研发流程/README/)、[03 Spring 接口与注入](/03-HTTP与Spring服务开发/README/)的基础节点后进入；[04 分层](/04-分层架构与业务建模/README/)、[12 认证授权](/12-安全与权限/README/)随账户查询案例交叉学习。

目标是贯通并解释“入口请求 → 应用服务 → 账户服务”，知道配置和地址来自哪里，并能定位身份缺失、配置未生效、找不到提供方和调用超时。跨服务写入的一致性按具体需求进入 [09](/09-分布式系统与服务协作/README/)。

通用 Java 学习遵循本计划的统一基线；存量工程按实际 Java 8、Spring、Dubbo 和 Nacos 版本保持兼容。先记录 JDK、Boot、Cloud、Cloud Alibaba、Dubbo、Nacos 客户端及服务端组合，再选对应文档。Boot 与 Cloud 必须按[官方兼容关系](https://spring.io/projects/spring-cloud/#overview)匹配；Nacos 接入方式也按对应 Cloud Alibaba 版本核对。

## 学习顺序

围绕当前服务调用，按“执行边界 → 组件职责 → 配置与发现 → 契约 → 失败与身份”理解概念，再读教程和项目代码。短代码说明机制，不要求为每个节点搭建双服务教学环境。

## 一 必须掌握的节点

### 1. 进程与调用边界

**业务问题：** 调用看起来是普通 Java 方法，为什么会出现找不到服务和超时？

JAR、Bean、进程与服务分别描述代码和运行边界。SDK 可以留在当前进程，代理则可能发出 HTTP/RPC；依赖成功解析不证明远端服务可用。

**代码与资料：** [普通方法与远程调用](/16-微服务开发与注册配置/典型案例/#一-方法写法相似执行位置可能不同)；依赖与调用基础读[廖雪峰 Maven](https://liaoxuefeng.com/books/java/maven/index.html)和 [Spring](https://liaoxuefeng.com/books/java/spring/index.html)。

### 2. Spring Cloud 与选型

**业务问题：** 当前工程的配置、服务地址、路由与调用分别由谁负责？

Spring 管对象，Boot 帮助应用配置启动，Cloud 提供一组协作项目。APISIX、Spring Cloud Gateway 与 Dubbo 职责不同，实际采用的组件由依赖、配置与运行链路共同确定。

**代码与资料：** [职责对照](/16-微服务开发与注册配置/典型案例/#二-spring-cloud-组织协作能力)与[廖雪峰 Spring Cloud](https://liaoxuefeng.com/books/java/springcloud/index.html)；版本组合查 [Spring Cloud 官方页](https://spring.io/projects/spring-cloud/)。

### 3. Nacos 配置管理

**业务问题：** 配置已经发布，为什么某个实例或业务对象仍使用旧值？

namespace ID、group、dataId 定位配置。发布成功、应用配置环境更新、Bean 刷新与业务读取生效是不同检查点；对象生命周期和启动期资源可能保留旧值。

**代码与资料：** [配置读取与生效](/16-微服务开发与注册配置/典型案例/#三-nacos-配置需要定位读取与生效)，从配置导入追到实际业务读取点；[Nacos 发布与监听](https://nacos.io/docs/latest/manual/user/config/publish-query-listen/)按项目版本核对。

### 4. Nacos 注册与发现

**业务问题：** 服务在控制台可见，为什么调用方仍找不到或连不上？

注册、订阅发现、网络可达和业务成功是不同结果。服务名、命名空间、分组、健康状态、协议及发现模式共同影响候选实例；配置隔离不等于业务租户授权。

**代码与资料：** [服务发现](/16-微服务开发与注册配置/典型案例/#四-服务发现回答到哪里调用)，对应提供方注册和消费方订阅；[Nacos 服务订阅](https://nacos.io/docs/latest/manual/user/naming/subscription-and-ops/)与 [Dubbo 注册说明](https://dubbo.apache.org/zh-cn/overview/mannual/java-sdk/tasks/service-discovery/nacos/)。

### 5. Dubbo 提供方与消费方

**业务问题：** 接口、DTO、远程代理和实现分别在哪里，更换契约影响谁？

消费方依赖公共契约，通过代理访问提供方；双方并不共享同一个 Java 对象。接口版本、group、version、协议、编码与异常约定需要匹配；注解还依赖扫描和运行配置生效。

**代码与资料：** [契约与代理](/16-微服务开发与注册配置/典型案例/#五-dubbo-用接口代理发起远程调用)，沿注入点追到提供方方法；[Dubbo Spring Boot 接入](https://dubbo.apache.org/zh-cn/overview/mannual/java-sdk/tasks/develop/springboot/)补实际机制。

### 6. 超时与重试

**业务问题：** 调用超时后，远端是否可能已经完成，重复请求会不会再写一次？

超时限制等待，不自动撤销远端执行。读取、创建和修改的重试条件不同，生效超时及重试还可能受方法级、服务级和动态配置影响。

**代码与资料：** [超时与结果](/16-微服务开发与注册配置/典型案例/#六-超时以后先确认发生了什么)，核对请求到达、执行、返回和状态查询入口；可靠性继续读 [09](/09-分布式系统与服务协作/README/)。

### 7. 网关与身份上下文

**业务问题：** HTTP 进入 RPC 后，谁确认身份，谁决定可以读取哪个账户？

凭证、可信主体、租户或项目、资源 ID 与请求关联 ID 分开追踪。请求头和 RPC 附加参数只是载体，来源验证、对象授权和跨线程清理决定它们能否安全使用。

**代码与资料：** [身份与授权](/16-微服务开发与注册配置/典型案例/#七-网关与下游分别守住身份和资源边界)，结合 [12](/12-安全与权限/README/)与[调用附加参数](https://dubbo.apache.org/zh-cn/overview/mannual/java-sdk/tasks/framework/attachment/)核对项目实际实现。

## 二 综合案例与业务验证

[账户查询案例](/16-微服务开发与注册配置/典型案例/#八-综合案例-从入口查询账户)串起入口、配置、调用、身份与失败。[纯 Java 样本](/assets/16-微服务开发与注册配置/examples/AccountContractExample.java)仅说明当前进程内的对象和授权规则，不能证明注册中心、RPC 或网关已正确接入。

| 实际需求 | 必要证据 |
| --- | --- |
| 配置接入或变更 | 配置定位、应用实际读取值、对象生命周期和业务行为一致 |
| 服务调用与契约变更 | 消费方与提供方可关联，版本和参数、返回、异常约定兼容 |
| 认证授权 | 合法读取、未认证、缺权限及错误归属在正确位置允许或拒绝 |
| 依赖失败与重试 | 区分发现失败、连接失败、超时和业务拒绝；外部副作用有状态查询或幂等依据 |

按当前改动范围保留配置、调用和运行证据；静态阅读、编译、联调分别记录。控制台出现服务名或普通 Java 样本输出正确，都不能代替真实请求链路成功。

## 三 按需求深入

| 方向 | 触发场景 | 补充路线 |
| --- | --- | --- |
| 跨服务写入 | 超时后不知道是否成功、重复创建 | [06 本地事务](/06-事务与数据一致性/README/)、[09 幂等与恢复](/09-分布式系统与服务协作/README/) |
| 身份与权限 | 登录、退出、对象越权、代用户调用 | [12 安全与权限](/12-安全与权限/README/) |
| 线程与资源 | 调用积压、线程阻塞、上下文串用 | [07 并发](/07-并发与异步编程/README/)、[13 排查](/13-部署运维与故障排查/README/) |
| 性能与治理 | 多实例分流、限流、熔断、容量问题 | 先有测量，再补 [10 系统设计](/10-系统设计/README/)与所用框架专题 |

## 参考资料

- **中文主读**：[廖雪峰 Spring Cloud 开发](https://liaoxuefeng.com/books/java/springcloud/index.html)、[Spring](https://liaoxuefeng.com/books/java/spring/index.html)与 [Maven](https://liaoxuefeng.com/books/java/maven/index.html)：先建立对象、依赖与服务协作的基础。
- **Java 补充**：[Dev.java](https://dev.java/learn/)；JDK 与框架的使用边界按[学习版本说明](/参考资料/学习版本与中文资料/)。
- **框架职责与版本**：[Spring Cloud 官方项目页](https://spring.io/projects/spring-cloud/)。
- **中文专项**：[Nacos 概览](https://nacos.io/docs/latest/overview/)、[配置发布与监听](https://nacos.io/docs/latest/manual/user/config/publish-query-listen/)和[服务订阅与运维](https://nacos.io/docs/latest/manual/user/naming/subscription-and-ops/)。
- **中文专项**：[Dubbo Spring Boot 接入](https://dubbo.apache.org/zh-cn/overview/mannual/java-sdk/tasks/develop/springboot/)、[Nacos 注册中心](https://dubbo.apache.org/zh-cn/overview/mannual/java-sdk/tasks/service-discovery/nacos/)与[调用附加参数](https://dubbo.apache.org/zh-cn/overview/mannual/java-sdk/tasks/framework/attachment/)。

[返回总纲](/)
