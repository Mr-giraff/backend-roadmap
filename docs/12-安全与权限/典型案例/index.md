---
{"title":"安全与权限典型案例"}
---

# 安全与权限典型案例

## 技能迁移表

| 前端已有经验 | 后端对应能力 | 需要补上的差异 |
| --- | --- | --- |
| 登录态与路由守卫 | 服务端认证 | 客户端说自己是谁，不等于已验证身份 |
| 按权限隐藏按钮 | 服务端授权 | 直接调用、批量操作和异步任务也要检查 |
| 根据 projectId 请求列表 | 对象权限与租户隔离 | 编号有效，不代表属于当前用户可访问的范围 |
| 表单校验与上传限制 | 输入、SQL、路径和资源边界 | 服务端面对数据库、文件系统和内部网络 |
| 请求日志与脱敏显示 | 审计、秘密与敏感数据管理 | 日志不能记录原始凭据，脱敏也不能替代授权 |

本阶段先理解网关与账户服务的认证授权链，再用纯 Java 验证对象访问规则。下面的身份对象用于规则实验；真实认证、Token 签发与验证由项目成熟安全体系提供。

## 一 先标出哪些信息可以信任

信任边界表示一份数据跨到哪里后，需要重新验证其来源与权限。

```text
浏览器输入 → 网关/认证链验证 → 应用获得可信身份 → 访问指定资源
```

前端能发送：

```javascript
fetch('/documents/7', { headers: { 'X-User-Id': '1' } });
```

这只能证明客户端发送了字符 `1`，不能证明调用者就是用户 1。服务端应从已经验证的 Session、Token 或平台安全上下文获得身份，不能直接把该 Header 当成身份。

| 信息 | 需要怎样处理 |
| --- | --- |
| 文档编号、筛选条件 | 客户端输入，需要格式与授权检查 |
| 当前用户身份 | 从已验证安全上下文取得 |
| 角色、租户和项目权限 | 从可信身份与权限数据确认 |
| 文档所属租户、项目 | 从服务端资源记录取得 |

本章后面的 Actor 是人工构造的**单元测试身份替身**，不接受 HTTP 输入，也不代表已经验证了认证链。

## 二 认证确认身份，授权决定能做什么

认证回答调用者是谁，授权判断该身份能对当前资源做什么。两者分开才能解释失败。

| 场景 | 认证情况 | 授权情况 | 典型 HTTP 表达 |
| --- | --- | --- | --- |
| 无有效凭据 | 未通过 | 尚不能执行 | 401，按认证协议提供必要挑战信息 |
| 已登录但无编辑权限 | 已通过 | 拒绝 | 403，或按资源隐藏策略返回 404 |
| 已登录且允许编辑当前对象 | 已通过 | 允许 | 执行业务操作 |

Session 与 Token 是承载身份的方式，还需考虑过期、退出、撤销和续期。JWT 的内容可编码读取，不意味着加密；签名有效也不自动授予某个文档的访问权。

前端菜单控制与服务端授权应使用同一业务规则，但服务端每次操作都必须自己作出判断。

## 认证链补充 从登录到接口拒绝

### 1. 一次登录和后续请求是两件事

登录时，账户服务检查凭据，成功后发放后续请求可使用的凭据。后续接口仍要验证凭据，并判断当前操作是否允许。

```text
登录：浏览器 → 网关路由 → 账户服务检查登录条件 → 签发凭据 → 浏览器
访问：浏览器携带凭据 → 验证身份 → 检查接口及对象权限 → 业务处理
```

最小 HTTP 交换只展示契约，`<credential>` 是占位符，不是可用 Token：

```http
GET /api/documents/7 HTTP/1.1
Authorization: Bearer <credential>
```

| 同一个请求，更换什么 | 学习样本的预期结果 |
| --- | --- |
| 没有有效凭据 | 认证层拒绝，常用 HTTP 401 表达 |
| 凭据有效，但只有读取权限，尝试修改 | 授权层拒绝，常用 403；资源隐藏策略也可能使用 404 |
| 有修改权限，但文档属于另一个项目 | 对象授权拒绝，不能仅凭角色通过 |
| 身份、动作、对象范围均有效 | 执行当前业务操作 |

具体服务可能还包装业务错误码。排查时同时记录 HTTP 状态和响应体，不从页面提示反推是哪一层拒绝。

### 2. 网关路由和身份验证分别证明什么

路由回答“转发到哪个服务”；认证回答“凭据对应谁”；授权回答“此人可以对哪个对象做什么”。给请求配置了路由，不代表启用了认证插件。网关采用外部认证时，还需要追踪它向认证服务发送了什么，以及允许、拒绝和认证服务不可用时怎样处理。

[APISIX Route](https://apisix.apache.org/docs/apisix/terminology/route/)可以关联插件和上游；[forward-auth](https://apisix.apache.org/docs/apisix/plugins/forward-auth/)是把认证检查交给外部服务的一种通用机制。阅读公司项目时，应先确认真实插件和生效配置，再按其实际行为追踪，不能直接套用这个插件的实现。

[APISIX Consumer](https://apisix.apache.org/docs/apisix/terminology/consumer/)是 API 消费方的概念，可能代表应用或其他调用者，不能直接等同于产品中一个已登录的终端用户。

### 3. 下游收到用户编号还不足以信任它

```text
浏览器声称 X-User-Id: 1       → 客户端输入
认证链验证后生成的用户上下文  → 应用可依约使用的身份
```

如果通过请求头传递已验证身份，必须确认：入口覆盖或剔除客户端同名字段、下游确认请求确实来自受信链路、绕过网关的入口也有保护。仅看到一个字段名，不能证明这些条件都成立。

服务调用还要区分“服务自己操作”和“服务代表某个用户操作”。[16 的账户查询案例](/16-微服务开发与注册配置/典型案例/)用于比较 HTTP、RPC 和身份边界；同步方法、网络请求及异步任务不会因为参数长得相同而自动共享安全上下文。

### 4. 凭据有效性要覆盖整个生命周期

JWT 的签名、有效期以及适用的签发者和接收方需要由安全实现检查。若系统还使用会话或撤销状态，必须同时追踪对应状态；读取出 JWT 内容不能代替验证。

| 操作 | 需要追踪的变化 |
| --- | --- |
| 登录 | 什么条件通过后才签发凭据，关联哪些用户或会话状态 |
| 请求 | 谁验签或查询会话，谁检查失效，谁建立当前身份 |
| 刷新 | 旧凭据、旧会话、新凭据如何关联，失败和并发怎样表现 |
| 退出或禁用 | 删除或更新了什么，后续请求在哪里据此拒绝 |

“退出接口返回成功”与“旧凭据从所有入口都失效”是两项不同验证。它们是否同时成立，应以请求和运行结果证明。

### 5. 用自己的话输出理解

完成一次源码阅读后，先写出下面六项，再请同事或 AI 核对：

1. 一个用户动作、入口请求和输入输出。
2. 路由、登录、凭据验证、权限判断的调用图，标明 HTTP/RPC 和进程内调用。
3. 凭据与身份数据从哪里来、经过哪里、在哪里清理。
4. 允许路径和一种拒绝路径，各自指出源码方法及关键配置。
5. 一张“角色/动作/资源范围”小矩阵，并标出实际对象归属校验。
6. 已由源码证明的结论、仍需配置或运行确认的问题，以及下一步验证方法。

先用自己的话解释，再运行学习或获准的测试环境中的正常、过期、无权限、跨项目和依赖失败场景。记录脱敏请求、结果与关联标识，不粘贴可复用凭据。

## 三 先给动作和资源定义规则

假设规则是：当前用户必须属于资源的租户和项目，读取允许所有项目成员，编辑还需要编辑能力。

| 用户能力与资源关系 | 读取 | 编辑 |
| --- | --- | --- |
| 同租户、在该项目内、有编辑能力 | 允许 | 允许 |
| 同租户、在该项目内、只有读取能力 | 允许 | 拒绝 |
| 同租户但不在该项目内 | 拒绝 | 拒绝 |
| 不同租户 | 拒绝 | 拒绝 |

先写表再写代码，未明确允许的组合默认拒绝。角色可提供初步能力，资源关系仍需单独检查。

本节全部类型定义如下。Actor.java：

```java
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Actor {
    public final long userId;
    public final long tenantId;
    public final Set<Long> projects;
    public final boolean canEdit;
    public Actor(long userId, long tenantId, Set<Long> projects, boolean canEdit) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.projects = Collections.unmodifiableSet(new HashSet<>(projects));
        this.canEdit = canEdit;
    }
}
```

Resource.java：

```java
public final class Resource {
    public final long id;
    public final long tenantId;
    public final long projectId;
    public Resource(long id, long tenantId, long projectId) {
        this.id = id;
        this.tenantId = tenantId;
        this.projectId = projectId;
    }
}
```

访问规则 AccessPolicy.java：

```java
public final class AccessPolicy {
    public boolean canRead(Actor actor, Resource resource) {
        return actor != null && resource != null
                && actor.tenantId == resource.tenantId
                && actor.projects.contains(resource.projectId);
    }
    public boolean canEdit(Actor actor, Resource resource) {
        return canRead(actor, resource) && actor.canEdit;
    }
}
```

此时 Actor 与 Resource 都有完整定义，代码中没有隐含的全局用户。实际应用需要将安全框架给出的身份转换成所需上下文，并核对权限数据的有效性。

## 四 交换一个资源编号，也不能越过项目边界

下面语句放在 main 中，人工身份只用于本地测试：

```java
Actor reader = new Actor(1L, 10L, java.util.Collections.singleton(100L), false);
Resource own = new Resource(7L, 10L, 100L);
Resource otherProject = new Resource(8L, 10L, 200L);
AccessPolicy policy = new AccessPolicy();
System.out.println(policy.canRead(reader, own));
System.out.println(policy.canEdit(reader, own));
System.out.println(policy.canRead(reader, otherProject));
```

依次输出 true、false、false。编号 8 即使存在，reader 也没有项目 200 的访问关系。

列表、详情、修改、删除、批量导出都应按相同原则检查。批量请求如果混入一个无权对象，必须先规定整批拒绝还是返回逐项结果，不能默默忽略约束。

实际查询还可以把可信租户与项目范围加入条件，避免先读出不该访问的数据；缓存键、文件下载和消息中的资源标识也需相同范围。

## 五 数据值用参数绑定，结构字段用允许列表

SQL 参数绑定把输入当作数据，不把它拼成 SQL 语句的一部分。假定学习数据库有 document 表，至少包含 tenant_id、project_id、title、created_at；后者用于下一段的创建时间排序。

下面方法所用类型全部来自 `java.sql` 与 `java.util`；connection 是调用方已获得的测试数据库连接：

```java
static java.util.List<String> titles(java.sql.Connection connection,
        long trustedTenantId, long allowedProjectId, String keyword)
        throws java.sql.SQLException {
    String sql = "SELECT title FROM document WHERE tenant_id=? AND project_id=? AND title=?";
    java.util.List<String> result = new java.util.ArrayList<>();
    try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setLong(1, trustedTenantId);
        statement.setLong(2, allowedProjectId);
        statement.setString(3, keyword);
        try (java.sql.ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(rows.getString("title"));
        }
    }
    return result;
}
```

trustedTenantId 来自可信身份，allowedProjectId 已经过权限检查。标题即使包含引号，仍只是第三个参数的值；不能用 `"...title='" + keyword + "'"` 代替。

排序列名等 SQL 结构不能用普通值占位符解决。假设只允许按标题或创建时间排序：

```java
static String orderColumn(String field) {
    if ("title".equals(field)) return "title";
    if ("createdAt".equals(field)) return "created_at";
    throw new IllegalArgumentException("不支持的排序字段");
}
```

后续只能拼接这个方法返回的固定值。MyBatis 的 `#{}` 用于值绑定，`${}` 是文本替换；动态结构需要允许列表，不能仅因为采用框架就取消检查。

## 六 浏览器、文件和外部请求有不同边界

### 1. CORS、CSRF 与 XSS 分别保护什么

| 概念 | 需要理解的行为 | 不能替代什么 |
| --- | --- | --- |
| CORS | 浏览器是否允许前端读取跨源响应 | 服务端认证授权 |
| CSRF | 浏览器自动携带凭据时，跨站请求可能触发操作 | 不能仅因响应是 JSON 就忽略 |
| XSS | 不可信内容被当成脚本或 HTML 执行 | 后端权限与数据隔离 |

Vue 的普通文本插值与把外部内容交给 v-html 是不同的输出方式。服务端应明确字段含义，前端按输出上下文编码或净化；不能把“过滤所有尖括号”当通用方案。

### 2. 文件名与 URL 都是输入

上传文件需要检查大小、内容、目标存储位置和下载权限。客户端文件名不能直接成为服务器保存路径，显示名称与存储标识可以分开。

SSRF 是输入诱导服务器访问不应访问的目标。若业务只需请求固定供应方，应限制协议、精确目标范围、重定向及网络出口；还需考虑 DNS 解析和地址变化，单纯 `startsWith("https")` 或字符串域名后缀不构成完整防护。

对分页、导出和批量操作还要设定服务端资源上限，防止合法身份也能无限占用系统资源。

## 七 密码、秘密与审计分开处理

登录密码需要专用、自适应、带盐的密码哈希方案，不能使用普通 MD5、SHA 或可逆加密代替。算法和成本参数按当前安全指南及项目框架核对，不在业务代码里自制。

| 数据 | 保存与使用原则 | 日志处理 |
| --- | --- | --- |
| 登录密码 | 通过成熟库哈希并验证 | 不记录明文或可复用值 |
| 数据库密码、API 密钥 | 由配置或秘密管理设施按最小范围提供 | 不输出完整连接凭据 |
| 身份 Token | 校验适用声明、过期和撤销规则 | 不记录原始 Token |
| 操作审计 | 操作者、资源、动作、时间、结果、请求标识 | 保留定位证据，控制审计读取权限 |

显示脱敏、存储加密、传输加密和权限控制解决不同问题。显示为星号，不能证明存储已加密；存储加密也不代表任何用户都可解密读取。

## 八 综合案例 多项目资源访问

将第三节完整类型和 [SecurityDemo.java](/assets/12-安全与权限/examples/SecurityDemo.java) 放在同一目录。示例数据只有人工身份和本地对象，不访问真实平台。

```java
import java.util.Collections;

public class SecurityDemo {
    public static void main(String[] args) {
        AccessPolicy policy = new AccessPolicy();
        Actor reader = new Actor(1L, 10L, Collections.singleton(100L), false);
        Actor editor = new Actor(2L, 10L, Collections.singleton(100L), true);
        Resource own = new Resource(7L, 10L, 100L);
        Resource foreign = new Resource(8L, 20L, 100L);
        System.out.println("reader read own: " + policy.canRead(reader, own));
        System.out.println("reader edit own: " + policy.canEdit(reader, own));
        System.out.println("editor edit own: " + policy.canEdit(editor, own));
        System.out.println("editor read foreign: " + policy.canRead(editor, foreign));
    }
}
```

在章节目录中执行：

```bash
mkdir -p /tmp/study-12-classes
javac --release 21 -encoding UTF-8 -d /tmp/study-12-classes examples/*.java
java -cp /tmp/study-12-classes SecurityDemo
```

主线需使用当前学习 JDK。预期分别为 true、false、true、false。说明规则同时核对动作、租户和项目，而不只看角色。

真实服务还需在已认证入口调用规则，验证 SQL 和批量查询确实带范围，并检查缓存、导出、异步与内部入口。单元对象结果没有证明这些基础设施已经正确接入。

## 九 练习与验收

1. 为权限矩阵每行增加本地用例，再加入未认证 actor=null；不匹配时都应拒绝。
2. 构造同租户不同项目、不同租户同项目号的资源，验证数字相同也不能越界。
3. 在隔离测试数据库中给标题加入引号，确认它仍作为值绑定；未知排序字段应被拒绝。
4. 检查一个请求的日志与审计：能追到操作者和对象，但找不到密码、原始 Token 或完整密钥。
5. 选择真实服务的列表、详情、批量导出三种入口，分别记录它们从哪里获得可信身份和资源范围。

验收包括授权矩阵、允许与拒绝的最终数据结果、可信身份来源及日志样例，不能只验证按钮是否可见。

学习环境、存量业务兼容要求与中文资料顺序见[学习版本与中文资料](/参考资料/学习版本与中文资料/)。

## 参考资料与验证范围

- **中文主读**：[廖雪峰 加密与安全](https://liaoxuefeng.com/books/java/security/index.html)：编码、哈希、口令与证书基础，认证授权规则仍按本路线与安全指南学习。
- **Java 补充**：[Dev.java 安全学习入口](https://dev.java/learn/)：JDK 加密、证书与安全工具背景。
- **授权与协议专项（英文）**：[OWASP Authorization](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html)、[REST Security](https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html)。
- **输入与秘密专项（英文）**：[SQL Injection Prevention](https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html)、[Password Storage](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)、[SSRF Prevention](https://cheatsheetseries.owasp.org/cheatsheets/Server_Side_Request_Forgery_Prevention_Cheat_Sheet.html)。

本地授权源码已在当前学习 JDK 中编译运行，正常读取、禁止编辑、跨租户、跨项目、无身份和无资源边界通过；原 Java 8 目标记录保留。认证链、真实数据库、密码库、网关与跨服务身份传递需在对应环境验证，测试 Actor 不能替代真实认证。

[返回 Roadmap](/12-安全与权限/README/) · [返回总纲](/)
