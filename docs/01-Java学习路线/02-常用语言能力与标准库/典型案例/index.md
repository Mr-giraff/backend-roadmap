---
{"title":"常用语言能力与标准库典型案例"}
---

# 常用语言能力与标准库典型案例

## 六 泛型

泛型让一个类型可复用，同时保留“里面装什么”的类型检查。先看 JS 的简单容器：

```javascript
class Box {
  constructor(value) { this.value = value; }
  get() { return this.value; }
}
console.log(new Box('ODS').get());
```

Java 的完整类型定义：

```java
class Box<T> {
    private final T value;
    Box(T value) { this.value = value; }
    T get() { return value; }
}
```

使用：

```java
Box<String> box = new Box<>("ODS");
String name = box.get();
System.out.println(name);
```

输出 `ODS`。`T` 是待确定的类型，`Box<String>` 把它确定为 String，所以 get 返回 String。`<>` 允许编译器从上下文推断类型参数。

把取出的值赋给 int 会编译失败。泛型主要提供编译期类型检查，不能替代接口入参校验；泛型擦除等机制按 Roadmap 深入。

## 七 集合

### 1. List 按位置保存多条数据

JS 数组和 Java List 都可以按顺序保存元素。先看增加和读取：

```javascript
const names = [];
names.push('ODS');
names.push('ADS');
console.log(names[0]);
console.log(names.length);
```

Java 使用时先导入这两个类型，import 写在文件顶部：

```java
import java.util.List;
import java.util.ArrayList;
```

使用：

```java
List<String> names = new ArrayList<>();
names.add("ODS");
names.add("ADS");
System.out.println(names.get(0));
System.out.println(names.size());
```

两边都输出 `ODS`、`2`。此时你已经学过接口和泛型：左侧 List 是列表接口，右侧 ArrayList 提供具体实现，String 限制元素类型。Java 中读取位置用 get，数量用 size。

### 2. Map 按键找值

Map 保存“键 → 值”。这里先用名字找年龄，不加入其他业务规则：

```javascript
const ages = new Map();
ages.set('张三', 18);
console.log(ages.get('张三'));
```

Java 先导入 `java.util.Map`、`java.util.HashMap`，再使用：

```java
Map<String, Integer> ages = new HashMap<>();
ages.put("张三", 18);
System.out.println(ages.get("张三"));
```

两边都输出 `18`。String 是键类型，Integer 是值类型；泛型中不能直接写 int，所以使用前面学过的包装类型。

再运行 `ages.put("张三", 20);`，随后 get 得到 20：同一个键再次 put 会替换旧值。允许覆盖还是拒绝重复，由之后的业务逻辑决定。

#### Map、HashMap 和 LinkedHashMap 的区别

`Map` 是接口，规定 `put()`、`get()` 等操作；`HashMap` 和 `LinkedHashMap` 是具体实现，真正创建的是右侧的对象。这与 `List` 和 `ArrayList` 的关系相同。

```java
Map<String, Integer> ages = new HashMap<>();
```

左侧用 `Map`，表示通过变量使用 Map 接口提供的能力；右侧用 `HashMap`，决定实际存储和查询的实现方式。接口不能直接实例化，因此不能写 `new Map<>()`。变量声明为 Map 并不会改变实际对象的行为。

| 类型 | 作用 | 遍历顺序 |
| --- | --- | --- |
| Map | 定义键值映射的操作 | 由具体实现决定 |
| HashMap | 基于哈希表实现 Map | 不保证顺序 |
| LinkedHashMap | 在哈希表基础上维护顺序 | 默认按键首次插入的顺序 |

需要按键查找、没有顺序要求时，通常使用 HashMap；需要按插入顺序遍历时，使用 LinkedHashMap，它需要额外内存维护顺序。JS 的 Map 保留插入顺序，因此不能因为名称相同，就认为 Java 的 HashMap 也有这项保证。

下面的例子额外导入 `java.util.LinkedHashMap`：

```java
Map<String, Integer> scores = new LinkedHashMap<>();
scores.put("张三", 90);
scores.put("李四", 80);
scores.put("张三", 95);
System.out.println(scores); // {张三=95, 李四=80}
```

默认情况下，更新已有键的值不会改变它的位置。换成 HashMap 后仍能正确按键查询，但不能依赖打印和遍历的顺序，即使某次输出恰好符合插入顺序。

### 3. Set 保存不重复的元素

与 JS Set 一样，Java Set 的同一元素只保留一份。先导入 `java.util.Set`、`java.util.HashSet`：

```javascript
const names = new Set();
names.add('ODS');
names.add('ODS');
console.log(names.size);
```

```java
Set<String> names = new HashSet<>();
names.add("ODS");
names.add("ODS");
System.out.println(names.size());
```

两边都输出 `1`。先记住 List 按位置取、Map 按键取、Set 用于去重；集合实现与自定义对象的比较规则再按 Roadmap 深入。

## 八 Lambda 与方法引用

### 1. Lambda 给“只有一个抽象方法的接口”提供实现

JS 的箭头函数可以直接作为一个操作传递：

```javascript
const doubleValue = value => value * 2;
console.log(doubleValue(3));
```

Java 需要先确定这个操作的类型。这个接口只有一个需要实现的方法，称为函数式接口：

```java
interface IntOperation {
    int apply(int value);
}
```

使用：

```java
IntOperation doubleValue = value -> value * 2;
System.out.println(doubleValue.apply(3));
```

两边都输出 `6`。左侧 IntOperation 告诉编译器：接收一个 int，返回一个 int。`->` 右边就是这个接口方法的实现；先读懂这一点，再到集合中使用 Lambda。

### 2. 方法引用复用现有方法

已有方法能完成同样操作时，可以缩写引用它。String.trim 用于去掉首尾空白；先导入 `java.util.function.Function`：

```java
Function<String, String> first = value -> value.trim();
Function<String, String> second = String::trim;
System.out.println(first.apply(" ODS "));
System.out.println(second.apply(" ODS "));
```

两行都输出 `ODS`。Function 是 Java 提供的函数式接口：本例输入和输出都是 String。这里的 `String::trim` 与 Lambda 执行同一个方法；暂不需要记住方法引用的全部形式。

## 九 Stream

Stream 把一组数据的过滤、变换、收集连接起来。现在用数字演示，避免同时引入新的业务类型。

```javascript
const numbers = [1, 2, 3, 4, 5];
const result = numbers
  .filter(value => value > 2)
  .map(value => value * 2);
console.log(result);
```

Java 需要导入 `java.util.List`、`java.util.Arrays`、`java.util.stream.Collectors`：

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
List<Integer> result = numbers.stream()
        .filter(value -> value > 2)
        .map(value -> value * 2)
        .collect(Collectors.toList());
System.out.println(result);
```

两边的结果都是 `[6, 8, 10]`。逐步看数据变化：

| 操作 | 逻辑结果 |
| --- | --- |
| 原始数据 | 1、2、3、4、5 |
| filter 保留大于 2 的数 | 3、4、5 |
| map 每个数乘 2 | 6、8、10 |
| collect 收集为 List | `[6, 8, 10]` |

这里的每个类型和写法都已出现过：List 与 Integer 来自集合和包装类型，`->` 来自 Lambda；`Arrays.asList` 为固定样本建立列表。

需要新补的是执行时机：Stream 的中间操作通常等到 collect 等终止操作才开始遍历，上表不表示每一步都已生成一个列表。一个 Stream 只能消费一次；原来的 List 仍可再次创建 Stream。

## 十 异常处理

### 1. 捕获当前操作可能产生的失败

先用字符串转整数说明。JS 的 Number 在这里返回 NaN；Java 的 parseInt 通过异常表示转换失败：

```javascript
const age = Number('abc');
if (Number.isNaN(age)) {
  console.log('年龄必须是整数');
}
```

```java
try {
    int age = Integer.parseInt("abc");
    System.out.println(age);
} catch (NumberFormatException e) {
    System.out.println("年龄必须是整数");
}
```

两边都输出 `年龄必须是整数`。NumberFormatException 是具体异常类型；catch 选择处理这种失败。Java 的整数解析还会拒绝超出 int 范围的输入。

如果需要主动拒绝业务输入，可以在方法中写 `throw new IllegalArgumentException("年龄不能为负数");`。throw 发出异常，catch 处理异常，finally 用于无论成功或失败都要做的收尾。

### 2. 文件读取还需要关闭资源

打开文件会持有系统资源，读取结束时应关闭。下面给出这个读取方法需要的全部 import 和定义：

```java
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
```

方法定义放在 Hello 类内、main 外：

```java
static String firstLine(String file) throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(
            Paths.get(file), StandardCharsets.UTF_8)) {
        return reader.readLine();
    }
}
```

main 中使用：

```java
try {
    System.out.println(firstLine("input.txt"));
} catch (IOException e) {
    System.out.println("读取失败：" + e.getClass().getSimpleName());
}
```

在当前工作目录放一个 UTF-8 文件 input.txt，首行为 ODS，运行后输出 `ODS`；文件不存在时会输出 `读取失败：NoSuchFileException`。

`throws IOException` 表示方法可能把读取异常交给调用方。IOException 是受检异常，调用方需要捕获或继续声明；前面的 NumberFormatException 属于不要求强制捕获的运行时异常。

`try (...)` 在离开代码块时关闭读取器，叫 try-with-resources。GC 负责内存回收，不能代替及时关闭文件。


### 3. 写一行文本，再读回来

沿用上面的 import，在 main 中运行下面的代码。它会创建一个临时文件；写入器关闭后，再用前面的 `firstLine` 读回第一行。

```java
java.nio.file.Path file = Files.createTempFile("study-io-", ".txt");
try (java.io.BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
    writer.write("ODS");
    writer.newLine();
}
System.out.println(firstLine(file.toString())); // ODS
Files.delete(file);
```

运行这段练习时给 main 加上 `throws IOException`。读写文本时，两端约定同一种字符编码；复制图片等二进制文件时，应使用字节流，按原始字节读写。

## 反射与运行时类型信息

### 1. Class 表示类型，实例保存各自的数据

`getClass()` 返回表示对象实际运行时类型的 Class 对象。先把下面的语句放在 Hello 的 main 中运行：

```java
String s = "Hello";
String s2 = "Hello1";
Class<?> cls = s.getClass();
Class<?> cls2 = s2.getClass();

System.out.println(cls == cls2);
System.out.println(cls == String.class);
System.out.println(s.equals(s2));
System.out.println(new String("Hi").getClass() == String.class);
```

依次输出 `true`、`true`、`false`、`true`。字符串实例保存各自的内容；在同一个 JVM 中，所有 `java.lang.String` 实例共用同一个 `String.class`。创建新字符串不会因此创建新的 Class 对象。`Class<?>` 表示这里不限定它所代表的具体类型。

因此，`cls == cls2` 判断是否为同一个类型对象，`s.equals(s2)` 判断字符串内容是否相同。

### 2. 运行时按名称找到并调用方法

反射让程序在运行时查看类型、字段、方法和注解等信息，并按这些信息操作对象。可以先用熟悉的 JS 动态调用来理解它的用途：

```javascript
const text = 'Hello';
const methodName = 'substring';
const method = text[methodName];
console.log(method.call(text, 1));
```

这里先按名称取得函数，再用 `call` 指定 text 为调用时的 this。Java 通过反射实现类似的调用。下面的语句也放在 Hello 的 main 中：

```java
String text = "Hello";
String methodName = "substring";
try {
    java.lang.reflect.Method method = text.getClass().getMethod(methodName, int.class);
    System.out.println(method.invoke(text, 1));
} catch (ReflectiveOperationException e) {
    throw new IllegalStateException("反射调用失败", e);
}
```

两边都输出 `ello`。Java 的 `getMethod` 根据方法名和参数类型查找公开方法，`invoke` 指定要调用的对象和实参。名称或参数类型不匹配时，查找会失败；调用过程也可能发生异常，所以示例处理反射异常并保留原因。

JS 本身支持按名称动态访问，还提供 Reflect API；这是能力上的类比，JS 的 Reflect 对象不等同于 Java 的 Class 对象。这个例子用于理解机制；实际业务已知要调用 substring 时，直接写 `text.substring(1)` 更清楚。

### 3. 框架中的用途与封装边界

框架需要用同一套代码处理不同的业务类型，反射可以帮助它读取类型信息，再按约定创建对象、访问属性或调用方法：

| 场景 | 具体例子 | 反射承担的工作 |
| --- | --- | --- |
| JSON 与 Java 对象互转 | 把 `{"name":"张三"}` 转成 User 对象 | 根据目标类查找构造方法、属性和注解，创建对象并填入数据；反向转换时读取属性 |
| Spring 创建对象和注入依赖 | 给 UserService 提供 UserRepository | 查看构造方法、参数类型或注入注解，调用构造方法、注入方法或设置字段 |
| JUnit 运行测试 | 自动执行标有 `@Test` 的方法 | 检查测试类和方法的注解，创建测试实例并调用测试方法 |
| 按配置创建实现对象 | 配置中指定接口实现类的类名 | 取得对应 Class，找到适合的构造方法创建对象，再通过接口使用 |

例如 JSON 工具不必为每个项目的 User、Order 各写一份转换代码。调用方提供 `User.class` 后，工具可以检查类的结构，按映射规则处理。后续结合 [Spring](/01-Java学习路线/05-Web与Spring服务开发/典型案例/)阅读对象注册与依赖注入。

反射本身不等于修改对象：`getClass()` 只取得类型信息，调用方法是否改变数据取决于方法本身。需要注意的是绕过类对自身数据规则的保护，例如 setter 拒绝负年龄，而直接改私有字段可能跳过校验。依赖私有成员也容易因类的内部调整而失效。

反射默认执行访问检查，`setAccessible(true)` 只能在允许的条件下关闭部分检查，仍受模块等边界限制。普通业务能直接调用公开方法时，优先直接调用。

复习时先解释上面四个布尔结果，再把 Java 示例的方法名改为不存在的名称，观察查找失败与正常调用的区别。

对应教程：[廖雪峰 Class类](https://liaoxuefeng.com/books/java/reflection/class/index.html)。能力与边界查阅 [Java 21 反射 API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/reflect/package-summary.html)、[AccessibleObject](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/reflect/AccessibleObject.html)；JS 对照 [MDN Reflect](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Reflect)。框架示例参考 [Jackson ObjectMapper](https://javadoc.io/static/com.fasterxml.jackson.core/jackson-databind/2.18.2/com/fasterxml/jackson/databind/ObjectMapper.html)、[Spring 注入处理](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/annotation/AutowiredAnnotationBeanPostProcessor.html)与 [JUnit ReflectionSupport](https://docs.junit.org/5.11.4/api/org.junit.platform.commons/org/junit/platform/commons/support/ReflectionSupport.html)。

## 现代 Java 写法补充

前面的显式类型、普通类和 main 仍是当前学习 JDK 的合法写法。读到现代写法时，重点确认它是否改变类型约束、状态可变性或执行行为，具体语法随用随查：

| 已学概念 | 中文主读 | 要掌握的差异 |
| --- | --- | --- |
| 局部变量与类型 | [var 类型推断](https://liaoxuefeng.com/books/java/quick-start/basic/var-type/index.html) | 编译器从初始化值推断类型，变量并未变成动态类型 |
| 条件与分支 | [switch 表达式](https://liaoxuefeng.com/books/java/quick-start/flow/switch/index.html) | 分支可以产生一个结果；与旧式 switch 的语句写法区分 |
| 类与数据字段 | [record 记录类](https://liaoxuefeng.com/books/java/oop/core/record/index.html) | 用于简单数据载体，访问器是字段同名方法，组件引用不自动使内容深度不可变 |
| List、Set、Map | [中文集合教程](https://liaoxuefeng.com/books/java/collection/index.html)与 [Dev.java](https://dev.java/learn/api/collections-and-streams/) | 认识 List.of、Set.of、Map.of 的不可修改结果及空值约束；可变元素仍可能变化 |

这些写法可在遵循学习基线的独立工程中使用。迁移到 Java 8 兼容代码时，需分别采用显式类型、普通类、旧式分支和该版本可用的集合构造方式；`--release 8` 会拒绝不兼容的语法和 API。

[运行短示例的方法](/01-Java学习路线/01-语言与开发环境/典型案例/#阅读顺序与运行方式)
