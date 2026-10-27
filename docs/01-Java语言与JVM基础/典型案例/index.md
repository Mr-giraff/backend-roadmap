---
{"title":"Java 语言与 JVM 基础典型案例"}
---

# Java 语言与 JVM 基础典型案例

## 技能迁移表

| 前端已经会的内容 | Java 中的对应能力 | 学习时重点看什么 |
| --- | --- | --- |
| 变量、字符串、条件和循环 | 类型声明、String、控制流 | 相同逻辑换成什么语法 |
| 函数、class、对象 | 方法、类、构造方法 | 类型、访问范围、类与实例的关系 |
| 约定对象提供哪些方法 | Java 接口 | “接口”在这里是类型契约，与 HTTP API 分开理解 |
| 可复用的数据结构 | 泛型 | 用类型参数限制容器中存放的数据 |
| Array、Map、Set | List、Map、Set | 先掌握增删查，再学批量处理 |
| 箭头函数、filter/map | Lambda、Stream | 函数的类型要求，以及结果何时生成 |
| try/catch | 异常处理、资源关闭 | 哪些失败要处理，哪些要向调用方报告 |

## 阅读顺序与运行方式

先按“基本语法 → 方法 → 对象 → 接口 → 泛型 → 集合 → Lambda → Stream → 异常”阅读。每节只解释当前概念，目录处理的综合案例放在最后。学习环境统一使用 JDK 25，先核对 `java -version` 与 `javac -version`；基础示例保留显式类和 main，现代写法在对应基础之后补齐。

前端的 `console.log` 在这里对应 `System.out.println`。下面这个完整文件可以作为运行短示例的起点，文件名为 `Hello.java`：

```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, Vue");
    }
}
```

在文件所在目录运行 `javac -encoding UTF-8 Hello.java`，再运行 `java Hello`，得到 `Hello, Vue`。第一条命令编译，第二条命令执行。工具的细节在 [02](/02-工程构建与研发流程/典型案例/)讲解。

后文标为“使用”的 Java 语句放在 Hello 的 main 方法内。普通类、接口和枚举可定义在 Hello 类外，放在同一目录的 Java 文件中；辅助方法按提示放在 Hello 类内、main 外。每节分别运行，新增类型后编译该目录全部 `.java` 文件。

## 一 变量与类型

### 1. 声明变量时写清类型

Java 要先确定变量能保存什么类型的值。你已经熟悉变量赋值，先比较同一组数据：

```javascript
let name = '张三';
let age = 18;
let enabled = true;
const limit = 10;
```

```java
String name = "张三";
int age = 18;
boolean enabled = true;
final int limit = 10;
```

`String` 表示字符串，`int` 表示整数，`boolean` 表示布尔值。这里的 `final` 与 const 有相似作用：变量不能再次赋值。它们都不会自动冻结引用对象的内容。

| 后续操作 | Java 的结果 |
| --- | --- |
| `age = 20;` | 可以，仍然是整数 |
| `age = "20";` | 编译失败，字符串不能直接赋给 int |
| `limit = 20;` | 编译失败，final 变量不能再次赋值 |

### 2. 整数、金额和空值需要区分

JS 常用 number 表达各种数字，Java 把用途分得更细：

| 类型 | 最先需要知道的用途 | 示例 |
| --- | --- | --- |
| int | 常用整数 | `int count = 10;` |
| long | 范围更大的整数，常用于编号 | `long id = 100L;` |
| double | 二进制浮点数，常用于近似计算 | `double ratio = 0.5;` |
| boolean | 真或假 | `boolean enabled = true;` |
| char | 保存 A 这样的字符；复杂字符可能占多个 char | `char grade = 'A';` |
| String | 字符串，是类而非基本类型 | `String name = "张三";` |

基本类型还有 byte、short、float，先认识即可。金额的十进制精度问题留到后面的扩展示例处理。

基本类型 int 不能保存 null，包装类型 Integer 可以：

```java
int count = 0;
Integer optionalCount = null;
Integer boxedCount = 3;
```

`Integer` 是 int 对应的包装类。Java 可以自动在它们之间转换，叫装箱和拆箱；null 拆成 int 会报空指针异常，所以“没有值”与“值为零”要按业务区分。

## 二 字符串

字符串的读取、拼接和比较可以与 JS 紧挨着看：

```javascript
const name = '张三';
console.log('你好 ' + name);
console.log(name.length);
console.log(name === '张三');
```

```java
String name = "张三";
System.out.println("你好 " + name);
System.out.println(name.length());
System.out.println(name.equals("张三"));
```

两边都输出 `你好 张三`、`2`、`true`。Java 的 `length()` 是方法，比较文本内容使用 equals。Java 的字符串 `==` 比较对象是否相同，可能受常量复用影响；业务中的文本比较先用 equals。

## 三 控制流与方法

### 1. 条件与循环沿用已有思路

if、普通 for 的写法与 JS 很接近。遍历一组数字时，JS 的 for...of 可以对应 Java 的增强 for：

```javascript
const numbers = [1, 2, 3];
for (const number of numbers) {
  if (number > 1) console.log(number);
}
```

```java
int[] numbers = {1, 2, 3};
for (int number : numbers) {
    if (number > 1) {
        System.out.println(number);
    }
}
```

两边都依次输出 `2`、`3`。这里先用数组，集合在第七节单独学习。

### 2. 方法写清参数类型和返回类型

你在 JS 中这样声明加法函数：

```javascript
function add(a, b) {
  return a + b;
}
console.log(add(2, 3));
```

本章先使用显式类与 main 的入口写法。下面是完整的 `MethodExample.java`：

```java
public class MethodExample {
    static int add(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        System.out.println(add(2, 3));
    }
}
```

编译并运行后输出 `5`。`int add` 表示返回整数，`int a`、`int b` 表示两个参数是整数；不返回值的方法写 void。

`static` 表示方法属于类，调用时不必先创建对象；main 也是这种方法。下一节再看需要先创建对象的实例方法。

## 四 类、对象与构造方法

类定义一类对象有什么数据和行为，对象保存某一次创建得到的数据。先比较同一个 Person：

```javascript
class Person {
  constructor(name) {
    this.name = name;
  }
  sayHi() {
    return '你好 ' + this.name;
  }
}
const person = new Person('张三');
console.log(person.sayHi());
```

Java 的完整类型定义如下：

```java
class Person {
    private final String name;

    Person(String name) {
        this.name = name;
    }

    String sayHi() {
        return "你好 " + name;
    }
}
```

在 main 中使用：

```java
Person person = new Person("张三");
System.out.println(person.sayHi());
```

两边都输出 `你好 张三`。`Person(String name)` 是构造方法，创建对象时执行；名称与类名相同，不写返回类型。`this.name` 指这个对象的字段，右侧 name 是传入的参数。

字段的 `private` 让外部不能直接读写它，`final` 让它在赋值后不能再次赋值。访问范围先掌握：public 允许公开访问，private 限于本类；不写修饰符通常表示同包可访问，protected 还涉及子类访问规则。

### 修改字段与替换参数

Java 和 JS 都要区分这两件事。下面的类型定义放在 Hello 类外：

```java
class Counter {
    int value;
}
```

下面的方法放在 Hello 类内、main 外：

```java
static void change(Counter counter) {
    counter.value = 2;
    counter = new Counter();
    counter.value = 99;
}
```

在 main 中使用：

```java
final Counter counter = new Counter();
counter.value = 1;
change(counter);
System.out.println(counter.value);
```

输出 **2**。参数起初与调用方指向同一对象，修改字段会影响这个对象；参数重新赋值，只改变方法内参数的指向。final 约束变量，允许这里修改对象字段。

## 五 继承、接口与枚举

### 1. 继承在已有类型上增加或替换行为

extends 的基本意图与 JS 的 class 继承相近。这个例子只比较方法覆盖：

```javascript
class Animal { sound() { return '声音'; } }
class Dog extends Animal { sound() { return '汪'; } }
console.log(new Dog().sound());
```

Java 类型定义：

```java
class Animal {
    String sound() { return "声音"; }
}
class Dog extends Animal {
    @Override
    String sound() { return "汪"; }
}
```

使用 `System.out.println(new Dog().sound());`，同样输出 `汪`。`@Override` 让编译器检查是否正确重写了父类或实现了接口的方法。

### 2. 接口先规定能做什么，再由类实现

JS 可以约定“传入的对象要有 greet 方法”；Java 用 interface 明确表达这种类型契约。下面是本节需要的全部类型：

```java
interface Greeter {
    String greet(String name);
}
class FriendlyGreeter implements Greeter {
    @Override
    public String greet(String name) {
        return "你好 " + name;
    }
}
```

在 main 中使用：

```java
Greeter greeter = new FriendlyGreeter();
System.out.println(greeter.greet("张三"));
```

输出 `你好 张三`。变量使用 Greeter 接口类型，具体行为来自 FriendlyGreeter 实现。这里的接口是 Java 类型契约；你熟悉的 HTTP API 是另一层概念。

### 3. 枚举给固定状态起明确的名字

前端可能约定字符串只能是 ACTIVE 或 ARCHIVED；Java 可以把这些可选值写成枚举：

```java
enum Status {
    ACTIVE, ARCHIVED
}
```

使用：

```java
Status status = Status.ACTIVE;
System.out.println(status == Status.ACTIVE);
```

输出 true。变量只能接收这个枚举类型的值，少了随意拼写状态字符串的机会。

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

## 十一 编译与 JVM 的基本关系

现在回看最开始的两条命令：

```text
Hello.java  -- javac -->  Hello.class  -- java -->  执行 main
```

JDK 提供编译器等开发工具及运行能力；JVM 执行字节码。源码写错可能在编译阶段被拒绝，文件不存在等问题则发生在运行阶段。

本例先掌握程序如何开始执行、调用如何进入方法、对象怎样被引用。堆、线程栈、类加载、GC 与 JIT 的深入实验继续按 [Roadmap](/01-Java语言与JVM基础/README/)推进。

## Java 25 主线补充

前面的显式类型、普通类和 main 仍是 Java 25 的合法写法。掌握它们后，按下面顺序继续阅读现代写法，不再把这些内容全部当作选学：

| 已学概念 | 中文主读 | 要掌握的差异 |
| --- | --- | --- |
| 局部变量与类型 | [var 类型推断](https://liaoxuefeng.com/books/java/quick-start/basic/var-type/index.html) | 编译器从初始化值推断类型，变量并未变成动态类型 |
| 条件与分支 | [switch 表达式](https://liaoxuefeng.com/books/java/quick-start/flow/switch/index.html) | 分支可以产生一个结果；与旧式 switch 的语句写法区分 |
| 类与数据字段 | [record 记录类](https://liaoxuefeng.com/books/java/oop/core/record/index.html) | 用于简单数据载体，访问器是字段同名方法，组件引用不自动使内容深度不可变 |
| List、Set、Map | [中文集合教程](https://liaoxuefeng.com/books/java/collection/index.html)与 [Dev.java](https://dev.java/learn/api/collections-and-streams/) | 认识 List.of、Set.of、Map.of 的不可修改结果及空值约束；可变元素仍可能变化 |

这些写法可在 Java 25 独立学习工程使用。迁移到 Java 8 兼容代码时，需分别采用显式类型、普通类、旧式分支和该版本可用的集合构造方式；`--release 8` 会拒绝不兼容的语法和 API。

## 十二 综合案例 将前面知识用于目录处理

现在再看 [完整目录程序](/assets/01-Java语言与JVM基础/examples/DirectoryExample.java)。它把上面已经出现的概念组合起来，得到三个结果：

```text
可用目录：[ODS, ADS]
编号 2：ADS
拒绝重复：重复编号：1
```

| 综合程序中的写法 | 已学概念 | 在这里负责什么 |
| --- | --- | --- |
| Directory、构造方法、getName | 类与对象 | 表示一条目录记录并提供读取方法 |
| Named、implements、Override | 接口与方法实现 | 明确目录能提供名称 |
| Status.ACTIVE / ARCHIVED | 枚举 | 限制状态取值 |
| `Result<T>`、`List<Directory>` | 泛型、集合 | 表明返回数据与元素类型 |
| filter、map、collect | Lambda、Stream | 得到可用目录的名称 |
| `Map<Long, Directory>` | Map 与包装类型 | 按编号查目录 |
| IllegalArgumentException | 异常 | 拒绝不合法输入和重复编号 |

这个案例新加的业务规则是“同一编号不能出现两次”。Map 本身允许 put 替换旧值，程序因此使用 putIfAbsent：无旧值时加入，有旧值时保留并返回旧值，再抛异常拒绝重复。这里目录对象都非空，返回值可用于判断重复。源码使用 LinkedHashMap 保留插入顺序，基本读写方式与前面的 HashMap 相同。

在章节目录中使用 JDK 25 执行以下命令；`--release 25` 固定本节的语言、API 与字节码目标：

```bash
cd ./01-Java语言与JVM基础
mkdir -p /tmp/study-java01-classes
javac --release 25 -encoding UTF-8 -d /tmp/study-java01-classes examples/*.java
java -cp /tmp/study-java01-classes DirectoryExample
```

以上主线命令要求 JDK 25。`-d` 指定编译结果目录，`-cp` 指定 JVM 到哪里查找类；旧项目的 Java 8 检查是单独的兼容任务。

### 继续串联值与文件处理

- [ValuesExample.java](/assets/01-Java语言与JVM基础/examples/ValuesExample.java)把字符串比较、空值、引用、金额与时间放在同一程序中。金额部分使用 BigDecimal 从十进制字符串构造；时间部分使用 Instant 表示确定时间点。它们是额外的数值与时间 API，不要求为了读懂前面的 List 而提前掌握。
- [DirectoryImportExample.java](/assets/01-Java语言与JVM基础/examples/DirectoryImportExample.java)把文件读取和异常处理组合起来：单行错误记录后继续，整个文件读取失败则报告失败。

运行入口及预期结果：

```bash
java -cp /tmp/study-java01-classes ValuesExample
java -cp /tmp/study-java01-classes DirectoryImportExample examples/directories.txt
```

导入示例应报告第 3、4、5 行错误，最终为 `有效 2 行，无效 3 行`。与最小异常示例比较，说明哪些失败允许继续、哪些失败必须结束本次操作。

## 十三 练习与验收

| 练习 | 先预测什么 | 验收依据 |
| --- | --- | --- |
| 往 Map 中两次 put 同一名字 | get 读到第一次还是第二次的年龄 | 读到第二次；能解释覆盖与拒绝重复是不同规则 |
| 把 Stream 的过滤条件改为大于 3 | 结果列表是什么 | 得到 `[8, 10]`，能逐步说明变化 |
| 给 `Box<String>` 传整数 | 编译能否通过 | 编译失败，能指出类型约束来自哪里 |
| 把 TMP 改为 ACTIVE | 可用目录列表如何变化 | 出现 ODS、ADS、TMP 三个名称 |
| 读取不存在的文件 | 与一行坏数据有何区别 | 能说明读取失败与行校验失败的处理层次 |

用自己的话讲一遍“类型怎样约束数据，集合怎样保存数据，Lambda/Stream 怎样处理数据，异常怎样表达失败”。讲到不清楚的地方，回到对应小节的最小示例验证。

## 参考与验证范围

- **中文主读：** [廖雪峰快速入门](https://liaoxuefeng.com/books/java/quick-start/index.html)、[面向对象](https://liaoxuefeng.com/books/java/oop/index.html)、[泛型](https://liaoxuefeng.com/books/java/generics/index.html)：按当前知识点顺序阅读。
- **中文主读：** [集合](https://liaoxuefeng.com/books/java/collection/index.html)与[函数式编程](https://liaoxuefeng.com/books/java/functional/index.html)：核对最小示例，再进入综合案例。
- **官方补充：** [Dev.java 语言基础](https://dev.java/learn/language/constructs/)、[泛型与模式匹配](https://dev.java/learn/language/fp/)、[集合和 Stream](https://dev.java/learn/api/collections-and-streams/)。
- **兼容查阅：** [Oracle Java 8 教程](https://docs.oracle.com/javase/tutorial/java/)用于旧项目兼容范围核对；主线版本与资料顺序见 [学习版本与中文资料](/参考资料/学习版本与中文资料/)。

验证记录：20 组概念示例已在 OpenJDK 11.0.10 上以 `--release 8` 完成兼容编译和运行，类型错误、final 再赋值、错误泛型参数按预期编译失败。当前主线目标为 Java 25，本机尚未检测到 JDK 25，因此新编译命令、现代写法与 Java 25 运行结果待对应环境核对；原兼容检查不能作为 Java 25 已实测的证明。IDE 操作及生产环境未在这里验证。

[返回本节 Roadmap](/01-Java语言与JVM基础/README/) · [返回总纲](/)
