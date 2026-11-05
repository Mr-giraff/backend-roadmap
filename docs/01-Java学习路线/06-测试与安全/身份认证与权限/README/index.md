---
{"title":"安全与权限 Roadmap"}
---

# 安全与权限 Roadmap

| 类别 | 作用 | 前端类比 | 类比边界 |
| --- | --- | --- | --- |
| 身份与信任边界 | 确认谁发起操作 | 登录态和路由守卫 | 客户端声明不是服务端可信身份 |
| 动作与资源授权 | 限定可访问的数据与行为 | 菜单按钮控制 | 每个直接、批量、异步入口仍要检查 |
| 输入与外部访问 | 避免输入改变执行含义 | 表单、上传和请求校验 | 还涉及 SQL、文件系统和内部网络 |
| 秘密与审计 | 保护凭据并保留必要证据 | 配置和操作日志 | 日志不能泄露 Token/密码，脱敏不代替授权 |

## 目标与前置条件

目标是能定义和验证“身份 × 动作 × 资源”的权限边界。先掌握 [Web与Spring服务开发](/01-Java学习路线/05-Web与Spring服务开发/README/)请求处理基础，SQL 部分结合 [MySQL基础与部署](/01-Java学习路线/04-数据库与持久化/MySQL基础与部署/README/)；纯 Java 授权规则只需 01。优先接入公司成熟安全体系，不要求重做登录或自制密码算法。

## 起步专题 网关与账户认证

本路线的认证、授权和信任边界前移到服务开发主线。完成 01/03 基础和 05 请求处理后即可开始；结合 [微服务开发](/01-Java学习路线/08-缓存、消息与服务协作/微服务调用与注册配置/README/)追踪配置、服务发现和调用，复杂并发与跨服务故障恢复可随需求深入。

先读[认证链补充案例](/01-Java学习路线/06-测试与安全/身份认证与权限/典型案例/#认证链补充-从登录到接口拒绝)，依次回答：请求怎样到达账户服务、谁验证凭据、谁产生可信身份、谁决定操作权限、下游怎样取得身份、退出后凭据怎样失效。

| 起步任务 | 需要留下的理解 |
| --- | --- |
| 跟一次登录和后续请求 | 登录校验、凭据签发、凭据验证和业务处理分别在哪里 |
| 分开网关、账户服务、业务服务 | 路由、身份有效性、接口许可和对象归属各由谁判断 |
| 比较允许与拒绝 | 未认证、过期、已登录无权限、跨项目和身份丢失停在哪一步 |
| 输出源码阅读记录 | 输入输出、方法与配置、两张时序图、事实与待验证问题分开 |

SQL、上传、密钥治理等内容按实际场景继续学习。基本权限检查和日志不含秘密作为每个需求的交付条件。

## 学习方式与顺序

围绕当前接口的身份、动作和对象范围学习：先明确业务规则，再理解信任边界和授权保证，读对应安全资料，最后追到实际入口、查询与异步路径。[典型案例](/01-Java学习路线/06-测试与安全/身份认证与权限/典型案例/)提供规则解释和短代码。

## 一 逐个知识点学习

### 1. 信任边界

**业务问题：** 用户编号、角色和项目字段分别是谁提供的，服务端凭什么信任？

可信身份来自经过验证的安全上下文；对象编号和筛选条件仍是输入。浏览器、网关、应用与数据库之间的每条边界，都要分清数据来源与允许的使用范围。

**代码与资料：** 读[信任边界](/01-Java学习路线/06-测试与安全/身份认证与权限/典型案例/#一-先标出哪些信息可以信任)，在项目中追踪身份生成、覆盖和接收位置；[OWASP REST Security](https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html)用于核对边界。

### 2. 认证与授权

**业务问题：** 为什么已登录仍被拒绝，退出成功后旧凭据是否还有效？

认证确认身份，授权判断当前动作能否作用于具体资源。Session、Token、JWT 还涉及有效期、签发者、接收方和撤销状态；接口路由成功不能证明认证或授权已经执行。

**代码与资料：** 读[认证与授权](/01-Java学习路线/06-测试与安全/身份认证与权限/典型案例/#二-认证确认身份授权决定能做什么)与[认证链补充](/01-Java学习路线/06-测试与安全/身份认证与权限/典型案例/#认证链补充-从登录到接口拒绝)，结合 [APISIX 路由](https://apisix.apache.org/docs/apisix/terminology/route/)和 [Spring Security 过滤链](https://docs.spring.io/spring-security/reference/servlet/architecture.html)定位项目实际检查点。

### 3. 动作与对象规则

**业务问题：** 同样是编辑角色，为什么不能修改另一个项目的对象？

角色提供一部分能力，对象归属、状态和字段可写范围还需单独判断。授权矩阵明确允许关系；未明确允许的情况按项目策略拒绝，不能只靠菜单和按钮限制。

**代码与资料：** 读[动作与资源规则](/01-Java学习路线/06-测试与安全/身份认证与权限/典型案例/#三-先给动作和资源定义规则)，找到项目中资源加载、归属检查与最终写入的位置；[OWASP Authorization](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html)补充对象授权原则。

### 4. 项目与租户隔离

**业务问题：** 列表、详情、缓存、导出和后台任务是否使用同一业务范围？

隔离要覆盖所有访问路径。查询条件、缓存键、文件下载、消息和异步上下文都需要携带正确范围；线程复用后的身份残留可能把上一请求的范围带给下一请求。

**代码与资料：** 读[项目与租户边界](/01-Java学习路线/06-测试与安全/身份认证与权限/典型案例/#四-交换一个资源编号也不能越过项目边界)，沿当前需求检查直接、批量与异步入口；上下文管理衔接 [并发](/01-Java学习路线/07-并发与JVM/README/)。

### 5. SQL 与输入边界

**业务问题：** 输入会被当作数据，还是改变 SQL、路径或外部访问的执行含义？

值通过参数绑定传递，列名和排序等结构通过允许列表约束。CORS、CSRF、XSS、上传、路径和网络出口各有边界，不能用一种检查替代全部防护；合法身份也需受资源上限约束。

**代码与资料：** 读[SQL 输入](/01-Java学习路线/06-测试与安全/身份认证与权限/典型案例/#五-数据值用参数绑定结构字段用允许列表)与[文件和外部请求](/01-Java学习路线/06-测试与安全/身份认证与权限/典型案例/#六-浏览器文件和外部请求有不同边界)，按实际功能查 [SQL 注入防护](https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html)和 [SSRF 防护](https://cheatsheetseries.owasp.org/cheatsheets/Server_Side_Request_Forgery_Prevention_Cheat_Sheet.html)。

### 6. 秘密与审计

**业务问题：** 凭据由谁读取和撤销，操作记录能否定位问题而不泄露秘密？

密码哈希、存储加密、传输加密、显示脱敏与授权解决不同问题。密钥需要最小读取范围和生命周期管理；审计记录操作者、对象、动作和结果，但不保存可复用凭据。

**代码与资料：** 读[秘密与审计](/01-Java学习路线/06-测试与安全/身份认证与权限/典型案例/#七-密码秘密与审计分开处理)，跟踪项目配置与审计入口；基础读[廖雪峰加密与安全](https://liaoxuefeng.com/books/java/security/index.html)，密码方案按 [Password Storage](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)及项目框架核对。

## 二 综合案例

[多项目资源授权](/01-Java学习路线/06-测试与安全/身份认证与权限/典型案例/#八-综合案例-多项目资源访问)展示身份、动作与资源的组合规则。[SecurityDemo](/assets/01-Java学习路线/06-测试与安全/身份认证与权限/examples/SecurityDemo.java)使用人工身份和内存对象，只能说明规则计算；真实认证链、查询范围、批量及异步入口要在当前服务中追踪。

## 三 业务验证

对当前需求保留能够说明权限边界的证据：

| 业务情况 | 应核对的结果与证据 |
| --- | --- |
| 未认证、失效凭据、权限不足 | 实际拒绝层、HTTP 与业务码、未发生的受保护副作用 |
| 同租户异项目、跨租户、批量混入无权对象 | 资源归属与查询条件，拒绝或逐项结果符合约定 |
| 同步、异步和内部调用 | 可信身份来源、范围传递及清理，不能串用主体 |
| 输入与日志 | 值绑定、结构允许列表、资源上限；审计可关联且不含原始秘密 |

源码理解、配置核对与实际请求结果分别记录。涉及退出、刷新或禁用时，还要确认已有凭据在相关入口怎样失效。

## 四 进阶范围与前置条件

| 深入主题 | 前置条件 | 后续能力 |
| --- | --- | --- |
| 跨服务身份深入 | 起步见 [微服务调用与注册配置](/01-Java学习路线/08-缓存、消息与服务协作/微服务调用与注册配置/README/)；异步结合 [并发与JVM](/01-Java学习路线/07-并发与JVM/README/)、恢复结合 [服务间调用](/01-Java学习路线/08-缓存、消息与服务协作/服务间调用/README/) | 代用户与服务身份、最小权限、线程清理和失败恢复 |
| 安全链深入 | 本页起步认证链及项目成熟框架 | 多种登录协议、CSRF/CORS 策略、静态资源和完整入口覆盖 |
| 文件和网络出口 | 本页输入边界；结合 [部署与运行维护](/01-Java学习路线/09-部署与运行维护/README/) | 上传下载隔离、DNS/重定向/目标范围与出口联合约束，避免片段检查冒充完整防护 |
| 数据安全与审计治理 | 本页秘密及 [大数据学习路线](/02-大数据学习路线/README/) | 敏感数据、加密与密钥、审计权限和保留周期，按平台能力拆场景验证 |

学习环境、存量业务兼容要求与中文资料顺序见[学习版本与中文资料](/参考资料/学习版本与中文资料/)。

## 参考资料

- **中文主读**：[廖雪峰 加密与安全](https://liaoxuefeng.com/books/java/security/index.html)：编码、哈希、口令与证书基础，认证授权规则仍按本路线与安全指南学习。
- **网关与安全链专项**：[APISIX Route](https://apisix.apache.org/docs/apisix/terminology/route/)、[Consumer](https://apisix.apache.org/docs/apisix/terminology/consumer/)与 [Spring Security 过滤链](https://docs.spring.io/spring-security/reference/servlet/architecture.html)。协议、插件与实际配置分别核对。
- **Java 补充**：[Dev.java 安全学习入口](https://dev.java/learn/)：JDK 加密、证书与安全工具背景。
- **授权与协议专项（英文）**：[OWASP Authorization](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html)、[REST Security](https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html)。
- **输入与秘密专项（英文）**：[SQL Injection Prevention](https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html)、[Password Storage](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)、[SSRF Prevention](https://cheatsheetseries.owasp.org/cheatsheets/Server_Side_Request_Forgery_Prevention_Cheat_Sheet.html)。

[返回总纲](/)
