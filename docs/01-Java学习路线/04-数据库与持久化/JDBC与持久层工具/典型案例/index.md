---
{"title":"JDBC 参数绑定案例"}
---

# JDBC 参数绑定案例

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

调用 `names(connection, 10L, "ads")` 时，使用本章样本应得到 `[应用层]`。connection 由调用方建立并负责生命周期；实际运行需要匹配版本的驱动、地址和凭证，本例不写真实凭证。

MyBatis 等框架可以减少映射代码，但不会代替理解 SQL、连接生命周期或查询次数。循环查询每个项目成员可能产生 N+1 次查询，应先记录 SQL 次数，再选择关联、批量查询或其他方式。

[准备示例表与数据](/01-Java学习路线/04-数据库与持久化/MySQL基础与部署/典型案例/#一-表保存同一种记录)
