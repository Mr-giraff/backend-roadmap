---
{"title":"数据库设计与 SQL 典型案例"}
---

# 数据库设计与 SQL 典型案例

## 技能迁移表

| 前端已有经验 | 数据库里的对应能力 | 需要补上的差异 |
| --- | --- | --- |
| 接口返回对象与数组 | 表、行、列和查询结果 | 数据长期保存，约束由数据库执行 |
| 给列表项设置唯一 key | 主键和唯一约束 | 主键识别记录，业务唯一规则可能跨多列 |
| filter、sort、map | WHERE、ORDER BY、SELECT | SQL 必须考虑空值、排序稳定性和扫描成本 |
| 根据 ID 合并两份数组 | 外键、JOIN 与关系建模 | 要先确定关系和重复行的含义 |
| 页面分页 | 数据库分页 | 需要稳定排序，并评估大偏移和并发变化 |
| 前端类型与表单校验 | 列类型、NOT NULL、UNIQUE、CHECK | 页面校验不能替代数据库最终约束 |

先学表与查询，再讨论索引、Java 接入和迁移，最后组合成项目目录查询。SQL 面向 **MySQL 8.0.16 及以上、InnoDB**；该版本范围会执行 CHECK 约束。以下只用于新建的独立学习库 `roadmap_sql_lab`，先确认所连环境；正文不包含清库或生产变更操作。

当前学习范围：先手算固定数据的查询结果，再接 Java 验证参数和结果映射。涉及写入时学习必要的本地事务；分库分表和数据库内核不作为当前前置。

## 一 表保存同一种记录

表可以先理解为有固定列定义的记录集合。一行是一条记录，一列是一种属性。

前端对象：

```javascript
const project = { id: 1, tenantId: 10, code: 'ads', name: '应用层' };
```

先建立一张最小表，并插入同一条数据：

```sql
CREATE DATABASE roadmap_sql_lab CHARACTER SET utf8mb4;
USE roadmap_sql_lab;
CREATE TABLE project (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(100) NOT NULL
) ENGINE=InnoDB;
INSERT INTO project (id, tenant_id, code, name) VALUES (1, 10, 'ads', '应用层');
SELECT id, tenant_id, code, name FROM project;
```

预期只有一行：`1 | 10 | ads | 应用层`。SQL 的列名可以与接口字段名不同，映射规则由应用定义。

`PRIMARY KEY` 表示主键：它唯一识别一行，而且不能为空。`NOT NULL` 表示该列不能保存 SQL 空值，不会自动拒绝空字符串。

## 二 类型与约束表达长期规则

### 1. 类型按数据含义选择

| 数据 | 示例类型 | 需要确定的规则 |
| --- | --- | --- |
| 标识编号 | BIGINT | 与 Java long、JSON 字符串的契约 |
| 金额 | DECIMAL(12,2) | 最大值、两位小数与舍入规则 |
| 编码或名称 | VARCHAR | 长度、字符集、大小写比较规则 |
| 时间 | DATETIME 或 TIMESTAMP | 时区、时间点与本地时间的语义 |
| 可选说明 | 允许 NULL 的 VARCHAR | NULL 与空字符串是否具有不同含义 |

Java 的 String 能装很多内容，数据库列仍需要长度和业务规则。不能只把页面控件类型照抄成表结构。

### 2. 联合唯一约束匹配业务范围

“同一租户内编码不重复，不同租户可以相同”需要两列一起唯一：

```sql
ALTER TABLE project ADD CONSTRAINT uk_project_tenant_code
    UNIQUE (tenant_id, code);
INSERT INTO project (id, tenant_id, code, name) VALUES (2, 20, 'ads', '另一租户');
INSERT INTO project (id, tenant_id, code, name) VALUES (3, 10, 'ads', '重复编码');
```

第一条 INSERT 成功；第二条因 `(10, 'ads')` 已存在而失败。失败语句不应作为后续查询的已有数据。

数据库的文本相等规则由排序规则决定；如果业务区分大小写，需选择相应排序规则并验证，不能只观察前端字符串比较。

### 3. CHECK 约束兜住单行数值规则

建立一个独立小表：

```sql
CREATE TABLE price_example (
    id BIGINT PRIMARY KEY,
    amount DECIMAL(12,2) NOT NULL,
    CONSTRAINT ck_price_nonnegative CHECK (amount >= 0)
) ENGINE=InnoDB;
INSERT INTO price_example VALUES (1, 12.50);
INSERT INTO price_example VALUES (2, -1.00);
```

预期第一条成功、第二条失败。CHECK 能约束这行金额，不能自动保证“某租户所有项目金额总和不超过预算”这类跨行规则。

## 三 SELECT 与 WHERE 先保证结果正确

补充两条成功数据：

```sql
INSERT INTO project (id, tenant_id, code, name) VALUES
    (4, 10, 'ods', '贴源层'),
    (5, 10, 'dwd', '明细层');
SELECT code, name FROM project
WHERE tenant_id = 10
ORDER BY id;
```

结果依次是 `ads/应用层`、`ods/贴源层`、`dwd/明细层`。这与按租户过滤数组、再取出 code/name 的思路接近：

```javascript
const result = projects
  .filter(item => item.tenantId === 10)
  .map(({ code, name }) => ({ code, name }));
```

SQL 没有 ORDER BY 就没有顺序保证。NULL 判断用 `IS NULL` / `IS NOT NULL`，而不是 `= NULL`；比较 NULL 的结果还涉及 SQL 的三值逻辑。

### 排序后再分页

```sql
SELECT id, code FROM project
WHERE tenant_id = 10
ORDER BY id
LIMIT 2 OFFSET 0;
```

预期为 `(1, ads)`、`(4, ods)`。第二页 `OFFSET 2` 得到 `(5, dwd)`。

当排序列可能重复时，加主键作为最终排序条件。深分页也可按上次最后一个 ID 查询，例如 `WHERE tenant_id=10 AND id>4 ORDER BY id LIMIT 2`；这要求接口携带游标，并明确数据变化时的行为。

## 四 关系与 JOIN 先明确一行代表什么

一个项目可以有多名成员，因此成员放另一张表。先把关系定义清楚，再做关联：

```sql
CREATE TABLE project_member (
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (project_id, user_id),
    CONSTRAINT fk_member_project FOREIGN KEY (project_id) REFERENCES project(id)
) ENGINE=InnoDB;
INSERT INTO project_member VALUES (1, 100, '张三'), (1, 101, '李四'), (4, 100, '张三');
SELECT p.code, m.user_name
FROM project p JOIN project_member m ON m.project_id = p.id
WHERE p.tenant_id = 10
ORDER BY p.id, m.user_id;
```

结果是 `ads/张三`、`ads/李四`、`ods/张三`。JOIN 后一行代表“项目与成员的一组对应关系”，不是一个项目；所以不能直接把行数当成项目数。

外键保证 project_id 指向存在的项目。它没有证明调用者有权限访问该项目，租户隔离和对象授权还需要应用校验；如果关系两端分别保存租户字段，也要设计如何禁止跨租户关联。

### 聚合与 LEFT JOIN

```sql
SELECT p.id, p.code, COUNT(m.user_id) AS member_count
FROM project p LEFT JOIN project_member m ON m.project_id = p.id
WHERE p.tenant_id = 10
GROUP BY p.id, p.code
ORDER BY p.id;
```

| id | code | member_count |
| --- | --- | --- |
| 1 | ads | 2 |
| 4 | ods | 1 |
| 5 | dwd | 0 |

LEFT JOIN 保留没有成员的项目。COUNT(m.user_id) 不计空值，因此 dwd 得到 0；这里若写 COUNT(*)，保留下来的项目行会被计为 1。

## 五 索引优化查找，EXPLAIN 观察计划

索引是数据库为查找组织的额外结构。先有目标查询，再根据访问方式建立索引：

```sql
CREATE INDEX idx_project_tenant_name ON project(tenant_id, name);
EXPLAIN SELECT id, name FROM project
WHERE tenant_id = 10 AND name = '应用层';
```

观察 `possible_keys`、`key`、`type`、`rows`、`Extra`，分别查看候选索引、实际索引、访问方式、估算扫描行数和额外工作。几行样本下优化器可能选择全表扫描；**本例不预设一定选择新索引，也不预设加索引后耗时必然下降**。

联合索引顺序要匹配查询前缀、范围和排序；二级索引还涉及回表读取。索引会增加存储与写入维护成本。

实际优化实验至少记录数据量、分布、查询条件、执行计划、SQL 次数与耗时。`EXPLAIN ANALYZE` 会实际执行查询，使用前核对数据库版本和语句影响。

## 六 Java 用参数传值，数据库负责执行 SQL

前端把查询参数与路径分开；Java 接入数据库也应把 SQL 结构与参数值分开。PreparedStatement 用占位符传值，避免直接拼接用户输入。

下面是完整方法所需的 import 和定义；连接由调用方提供并负责关闭：

```java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
```

方法放在自己的 Java 类内：

```java
static List<String> names(Connection connection, long tenantId, String code) throws SQLException {
    String sql = "SELECT name FROM project WHERE tenant_id = ? AND code = ? ORDER BY id";
    List<String> result = new ArrayList<>();
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setLong(1, tenantId);
        statement.setString(2, code);
        try (ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                result.add(rows.getString("name"));
            }
        }
    }
    return result;
}
```

调用 `names(connection, 10L, "ads")` 时，使用本章样本应得到 `[应用层]`。connection 是已建立且指向本章学习库的 JDBC 连接；运行需要匹配版本的驱动、地址和凭证，本例不写真实凭证。

MyBatis 等框架可以减少映射代码，但不会代替理解 SQL、连接生命周期或查询次数。循环查询每个项目成员可能产生 N+1 次查询，应先记录 SQL 次数，再选择关联、批量查询或其他方式。

## 七 表结构变更需要兼容已有数据

增加字段前，先问旧数据如何填、旧程序能否继续写、新约束何时生效。下面只针对学习表演示迁移顺序，分步执行：

```sql
ALTER TABLE project ADD COLUMN description VARCHAR(200) NULL;
UPDATE project SET description = '' WHERE description IS NULL;
SELECT COUNT(*) AS missing FROM project WHERE description IS NULL;
```

missing 应为 0。若计划进一步改成 NOT NULL，先确认仍在运行的旧程序不会继续写 NULL，并为新增记录设计默认值，再收紧约束。

MySQL DDL 可能隐式提交；ALTER TABLE 的锁、磁盘和执行时间取决于版本、表大小与操作方式。迁移脚本要版本化，先在有旧数据的副本演练，不只在空库验证。

## 八 综合案例 项目目录与成员数量

现在把已学的表、约束、关联和分页串起来。需求是“按租户列出项目，每个项目带成员数”：

```sql
SELECT p.id, p.code, p.name, COUNT(m.user_id) AS member_count
FROM project p LEFT JOIN project_member m ON m.project_id = p.id
WHERE p.tenant_id = 10
GROUP BY p.id, p.code, p.name
ORDER BY p.id
LIMIT 2;
```

结果为项目 1 与 4，人数分别为 2、1。这里先聚合回“一项目一行”，再分页；如果在一对多明细上先 LIMIT，项目数量与成员数量都可能被误解。

设计说明应包含：项目主键、租户内编码唯一、项目成员复合主键、外键、查询权限范围、索引、分页和迁移策略。技术组件之前，先保证这些业务含义清楚。

## 九 练习与验收

1. 重复插入同一租户编码、改成不同租户编码，分别预测结果并检查唯一约束。
2. 把 LEFT JOIN 改为 JOIN，说明为什么没有成员的 dwd 消失；再比较 COUNT(*) 与 COUNT(m.user_id)。
3. 生成十万条分布不均的数据，比较索引前后执行计划，报告扫描量和写入代价，不只比较一次耗时。
4. 为已有重复编码的数据增加唯一约束，先做检测与清理方案，说明旧应用继续写入时的兼容要求。

通用 Java 学习按 Java 25；公司业务案例保留 Java 8 兼容要求。中文主读顺序与环境边界见 [学习版本与中文资料](/参考资料/学习版本与中文资料/)。

## 参考与验证范围

- **中文主读**：[廖雪峰 SQL 教程](https://liaoxuefeng.com/books/sql/introduction/index.html)与 [JDBC 编程](https://liaoxuefeng.com/books/java/jdbc/index.html)：查询、关系与 Java 数据访问。
- **Java 补充**：[Dev.java](https://dev.java/learn/)：Java 类型与资源管理背景；SQL 语义按数据库文档核对。
- **专项核对（英文）**：[MySQL CREATE TABLE](https://dev.mysql.com/doc/refman/8.0/en/create-table.html)、[SELECT](https://dev.mysql.com/doc/refman/8.0/en/select.html)、[EXPLAIN](https://dev.mysql.com/doc/refman/8.0/en/explain.html)：约束、查询计划和版本行为。
- **迁移与事务边界（英文）**：[Flyway 版本化迁移](https://documentation.red-gate.com/fd/versioned-migrations-273973333.html)、[MySQL 隐式提交](https://dev.mysql.com/doc/refman/8.0/en/implicit-commit.html)：工具按工程选型。

SQL 及预期结果按 MySQL 8.0.16+ InnoDB 语义编写，尚未在本机 MySQL 实例执行；JDBC 方法已有 Java 8 目标编译证据，尚未按 Java 25 编译或连接数据库。需要在独立学习库核对版本、排序规则、约束和执行计划。

[返回本节 Roadmap](/05-数据库设计与SQL/README/) · [返回总纲](/)
