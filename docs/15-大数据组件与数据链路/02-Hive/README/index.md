---
{"title":"Hive Roadmap"}
---

# Hive Roadmap

| 类别 | 作用 | 前端类比 | 类比边界 |
| --- | --- | --- | --- |
| 表与 Metastore | 描述字段、格式、位置和分区 | schema 与接口元信息 | 描述存在不代表数据文件已经存在 |
| HiveServer2 与 SQL | 接收查询、组织编译和执行 | API 服务与请求入口 | 实际计算由所用执行引擎和资源承担 |
| 文件与分区 | 组织数据并减少读取范围 | 按日期组织静态文件 | 分区登记、文件存在、权限都需分别核对 |

目标是能从 SQL 跟到表描述和文件，完成建表、登记分区、查询、计划检查以及常见不可见问题排查。前置为 [SQL 基础](/05-数据库设计与SQL/README/)与[分布式共同基础](/15-大数据组件与数据链路/01-分布式计算基础/README/)。

## 学习顺序

| 顺序 | 范围 | 对应最小案例 | 过关证据 |
| --- | --- | --- | --- |
| 1 | Client、HiveServer2、Metastore、执行引擎、存储 | [角色分工](/15-大数据组件与数据链路/02-Hive/典型案例/#一-先分清谁接收-sql谁记录表谁保存文件) | 一张查询经过的职责图 |
| 2 | 字段、类型、格式、LOCATION、外部表 | [表与文件](/15-大数据组件与数据链路/02-Hive/典型案例/#二-表描述怎样读取文件) | DDL 和实际文件可对应 |
| 3 | 分区列、目录与分区登记 | [固定数据查询](/15-大数据组件与数据链路/02-Hive/典型案例/#三-文件放进目录后再登记分区) | SHOW PARTITIONS 与查询结果 |
| 4 | 分区过滤、计划、文件数量 | [查询范围](/15-大数据组件与数据链路/02-Hive/典型案例/#四-查询慢先看读了什么) | EXPLAIN 与读取范围的解释 |
| 5 | 不可见、字段错、权限与小文件 | [故障定位](/15-大数据组件与数据链路/02-Hive/典型案例/#五-表存在但查不到数据怎样排查) | 根据证据找到首个失败环节 |

## 学习与环境范围

用已有的隔离学习环境即可，不要求先搭建完整生产集群。记录 Hive、Hadoop、执行引擎、存储和 JDK 版本，以及访问身份。通用当前学习 JDK 不决定 Hive 的运行 JDK；具体组合按版本文档核对。

本单元先用无引号、无内嵌逗号的简单 CSV 文本理解表与文件。之后再认识 Parquet/ORC、列式读取和压缩。Hive ACID、Compaction、复杂 SQL 调优和跨版本升级按实际任务深入。

## 综合案例与验收

完成[订单外部表案例](/15-大数据组件与数据链路/02-Hive/典型案例/#六-综合案例-从五行文件得到地区金额)。

- [ ] 核对五行输入、表字段、分区列和存储位置。
- [ ] 登记一个日期分区，查询出三笔有效订单、总额 23000 分。
- [ ] 说明外部表、Metastore 元数据库、数据文件的职责区别。
- [ ] 保存 DDL、查询、EXPLAIN、表位置、分区与运行日志。
- [ ] 对一个空结果或错误结果，按文件、分区、类型、过滤和权限逐步定位。

后续与 [Spark](/15-大数据组件与数据链路/03-Spark/README/)共享 Metastore 时，需要单独核对 Catalog、版本与访问权限；各自能查询不代表已共享同一份元数据。

## 参考资料

- **中文前置主读**：[廖雪峰 SQL](https://liaoxuefeng.com/books/sql/introduction/index.html)；Java/JDBC 背景按[廖雪峰 JDBC](https://liaoxuefeng.com/books/java/jdbc/index.html)与 [Dev.java](https://dev.java/learn/)补充。
- **专项核对**：[Hive DDL](https://hive.apache.org/docs/latest/language/languagemanual-ddl/)中的外部表、LOCATION、分区和 DESCRIBE；[HiveServer2](https://hive.apache.org/docs/latest/admin/setting-up-hiveserver2/)用于理解客户端入口。
- **运行环境**：[Hive 官方文档](https://hive.apache.org/docs/latest/)按所用版本查阅。

[返回大数据路线](/15-大数据组件与数据链路/README/) · [返回总纲](/)
