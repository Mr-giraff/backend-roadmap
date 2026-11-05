---
{"title":"常用语言能力与标准库"}
---

# 常用语言能力与标准库

### 4. 数据结构与数据流

**业务问题：** 这批目录按什么查找、怎样去重、是否需要保持顺序？过滤和转换发生在哪里，会不会改变原数据或触发外部操作？

| 需要理解的概念与保证 | Java 中要能识读的表达 | 对应教程 |
| --- | --- | --- |
| 容器按元素类型组织数据；位置、键、去重和顺序分别是不同需求 | 数组、泛型、List、Map、Set；接口与 ArrayList、HashMap、HashSet 等实现 | [数组类型](https://liaoxuefeng.com/books/java/quick-start/basic/array/index.html)、[泛型](https://liaoxuefeng.com/books/java/generics/what-is-generics/index.html)、[使用泛型](https://liaoxuefeng.com/books/java/generics/use-generics/index.html)、[List](https://liaoxuefeng.com/books/java/collection/list/index.html)、[Map](https://liaoxuefeng.com/books/java/collection/map/index.html)、[Set](https://liaoxuefeng.com/books/java/collection/set/index.html) |
| 处理逻辑可以作为参数传递；流水线表达转换步骤及何时产生结果 | 函数式接口、Lambda、方法引用、循环、filter、map、collect、中间与终止操作 | [Lambda](https://liaoxuefeng.com/books/java/functional/basic/index.html)、[方法引用](https://liaoxuefeng.com/books/java/functional/method-ref/index.html)、[Stream](https://liaoxuefeng.com/books/java/functional/stream/index.html)、[filter](https://liaoxuefeng.com/books/java/functional/stream/filter/index.html)、[map](https://liaoxuefeng.com/books/java/functional/stream/map/index.html)、[输出集合](https://liaoxuefeng.com/books/java/functional/stream/to-collection/index.html) |
| 容器结构不可修改与元素不可变化是两项保证；集合转换不自动提供业务去重或并发安全 | List.of、Set.of、Map.of、元素字段修改、put 与 putIfAbsent | [本地现代写法](/01-Java学习路线/02-常用语言能力与标准库/典型案例/#现代-java-写法补充)、[Dev.java 集合与 Stream](https://dev.java/learn/api/collections-and-streams/) |

**回到代码：** 读[泛型](/01-Java学习路线/02-常用语言能力与标准库/典型案例/#六-泛型)、[集合](/01-Java学习路线/02-常用语言能力与标准库/典型案例/#七-集合)和 [Stream](/01-Java学习路线/02-常用语言能力与标准库/典型案例/#九-stream)，解释 activeNames 怎样得到可用目录名称、indexById 为什么单独拒绝重复编号。Java Stream 表达进程内的数据处理流水线；Kafka 的消息持久化与消费进度、Flink 的时间与状态恢复，分别进入 [缓存、消息与服务协作](/01-Java学习路线/08-缓存、消息与服务协作/README/)和 [大数据学习路线](/02-大数据学习路线/README/)，不能从相似名字推断相同保证。

### 5. 失败、资源与边界

**业务问题：** 一行输入不合法，还是整个文件读不到？谁处理失败、谁向调用方说明？失败前已经发生的数据修改或外部调用需要怎样处理？

| 需要理解的概念与保证 | Java 中要能识读的表达 | 对应教程 |
| --- | --- | --- |
| 失败沿调用关系传播，处理位置决定调用方还能做什么 | 异常类型、throw、throws、catch、finally、异常原因与调用栈 | [Java 的异常](https://liaoxuefeng.com/books/java/exception/java-exception/index.html)、[捕获异常](https://liaoxuefeng.com/books/java/exception/catch-exception/index.html)、[抛出异常](https://liaoxuefeng.com/books/java/exception/throw-exception/index.html) |
| 文件、连接等资源有使用期限；内存回收不能代替及时释放资源 | try-with-resources、AutoCloseable、BufferedReader、Files、UTF-8 | [InputStream](https://liaoxuefeng.com/books/java/io/input-stream/index.html)中的资源关闭、[Files](https://liaoxuefeng.com/books/java/io/files/index.html) |
| 抛出异常不自动撤销已经发生的副作用，继续处理或整体失败需要业务规则 | 单行捕获与外层抛出、失败返回、后续事务与补偿调用 | [本地异常处理](/01-Java学习路线/02-常用语言能力与标准库/典型案例/#十-异常处理)；数据库回滚进入 [事务](/01-Java学习路线/04-数据库与持久化/数据变更与事务/README/)，跨服务恢复进入 [服务间调用](/01-Java学习路线/08-缓存、消息与服务协作/服务间调用/README/) |

**回到代码：** [DirectoryImportExample](/assets/01-Java学习路线/01-语言与开发环境/examples/DirectoryImportExample.java)将单行校验失败记录后继续，文件读取失败则向上报告；它只读取与统计，不包含数据库写入。阅读真实导入服务时，另查部分成功的规则、事务范围、重复请求以及失败前后的数据状态，不能只看是否有 catch。


### 7. 反射与运行时类型信息

**业务问题：** 框架如何处理没有为它专门编写代码的业务类？不同实例的类型信息是否相同，运行时怎样按名称找到方法？

**概念与保证：** Class 表示运行时类型，反射用于查看成员与注解、创建对象和调用方法；访问检查与对象自身的数据规则仍需分别理解。

**对应教程与案例：** 先读[廖雪峰 Class类](https://liaoxuefeng.com/books/java/reflection/class/index.html)，再运行[反射与运行时类型信息](/01-Java学习路线/02-常用语言能力与标准库/典型案例/#反射与运行时类型信息)，解释 String 实例与 Class 对象的区别，对照 JS 动态调用，最后认识 JSON 映射、依赖注入与测试发现的用途。

[进入本节典型案例](/01-Java学习路线/02-常用语言能力与标准库/典型案例/)
