---
{"title":"Hive Roadmap"}
---

# Hive Roadmap

| 类别 | 作用 | 前端类比 | 类比边界 |
| --- | --- | --- | --- |
| 表与 Metastore | 描述字段、格式、位置和分区 | schema 与接口元信息 | 描述存在不代表数据文件已经存在 |
| HiveServer2 与 SQL | 接收查询、组织编译和执行 | API 服务与请求入口 | 实际计算由所用执行引擎和资源承担 |
| 文件与分区 | 组织数据并减少读取范围 | 按日期组织静态文件 | 分区登记、文件存在、权限都需分别核对 |

目标是能从 SQL 跟到表描述和文件，解释表、分区和读取范围，定位常见不可见问题。前置为 [SQL 基础](/05-数据库设计与SQL/README/)与[分布式共同基础](/15-大数据组件与数据链路/01-分布式计算基础/README/)。

## 学习顺序

先明确业务问题，再理解计算与运行保证，读对应资料，最后在 SQL、代码、配置和计划中定位。

| 业务问题 | 概念与保证 | 代码与讲解入口 |
| --- | --- | --- |
| 一次查询由谁接收、谁解释表、谁执行、谁读取文件 | Client、HiveServer2、Metastore、执行引擎和存储的分工 | [角色分工](/15-大数据组件与数据链路/02-Hive/典型案例/#一-先分清谁接收-sql谁记录表谁保存文件) |
| 表名存在，为什么文件可能不存在或字段读取错误 | 字段类型、格式、LOCATION、外部表与文件生命周期 | [表与文件](/15-大数据组件与数据链路/02-Hive/典型案例/#二-表描述怎样读取文件) |
| 文件已落盘，为什么目标日期仍查不到 | 分区列、目录、元数据登记及执行身份 | [分区可见性](/15-大数据组件与数据链路/02-Hive/典型案例/#三-文件放进目录后再登记分区) |
| SQL 带日期过滤，是否真的减少了读取范围 | 分区裁剪、执行计划、格式与文件数量 | [查询范围](/15-大数据组件与数据链路/02-Hive/典型案例/#四-查询慢先看读了什么) |
| 空结果、NULL、权限错误与金额偏大如何区分 | 解析、过滤、权限、重复文件和 JOIN 口径 | [问题定位](/15-大数据组件与数据链路/02-Hive/典型案例/#五-表存在但查不到数据怎样排查) |

表、LOCATION 和分区按 [Hive DDL](https://hive.apache.org/docs/latest/language/languagemanual-ddl/)核对，查询入口查 [HiveServer2](https://hive.apache.org/docs/latest/admin/setting-up-hiveserver2/)。实际 Hive、Hadoop、存储、执行引擎和 JDK 的组合按项目版本确认。先理解字段与文件对应，再按业务需要深入 Parquet/ORC、ACID、Compaction 和跨版本升级。

## 综合案例与业务验证

[订单外部表案例](/15-大数据组件与数据链路/02-Hive/典型案例/#六-综合案例-从五行文件得到地区金额)展示表定义、文件和分区如何共同影响结果。短 DDL 和 SQL 用于阅读概念；实际数据源、存储路径、权限和执行引擎按项目核对。

涉及表或查询变更时，保留 DDL、实际存储位置、分区元数据、查询身份、执行计划及对账结果。查询为空时按文件、元数据、字段、过滤与权限查首个偏离环节。与 [Spark](/15-大数据组件与数据链路/03-Spark/README/)共享 Metastore 时，还需确认 Catalog、版本和权限，不能从两端都能查询推断读取同一份数据。

## 参考资料

- **中文前置主读**：[廖雪峰 SQL](https://liaoxuefeng.com/books/sql/introduction/index.html)；Java/JDBC 背景按[廖雪峰 JDBC](https://liaoxuefeng.com/books/java/jdbc/index.html)与 [Dev.java](https://dev.java/learn/)补充。
- **专项核对**：[Hive DDL](https://hive.apache.org/docs/latest/language/languagemanual-ddl/)中的外部表、LOCATION、分区和 DESCRIBE；[HiveServer2](https://hive.apache.org/docs/latest/admin/setting-up-hiveserver2/)用于理解客户端入口。
- **运行环境**：[Hive 官方文档](https://hive.apache.org/docs/latest/)按所用版本查阅。

[返回大数据路线](/15-大数据组件与数据链路/README/) · [返回总纲](/)
