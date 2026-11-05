---
{"title":"Java 语言与 JVM 基础典型案例"}
---

# Java 语言与 JVM 基础典型案例

## 技能迁移表

| 前端已经会的内容 | Java 中的对应能力 | 学习时重点看什么 |
| --- | --- | --- |
| 变量、字符串、条件和循环 | 值、类型、数据范围与控制流 | 数据能表示什么、空值和相等性怎样定义 |
| 函数、class、对象 | 方法、类、构造方法 | 类型、访问范围、类与实例的关系 |
| 约定对象提供哪些方法 | Java 接口 | “接口”在这里是类型契约，与 HTTP API 分开理解 |
| 可复用的数据结构 | 容器与类型约束 | 元素契约、查询方式和数据规模 |
| Array、Map、Set | 顺序、索引与唯一性 | 容器操作是否符合业务的排序、查找和去重规则 |
| 箭头函数、filter/map | Lambda、Stream | 函数的类型要求，以及结果何时生成 |
| try/catch | 异常处理、资源关闭 | 哪些失败要处理，哪些要向调用方报告 |

本篇是配合主教程使用的代码阅读材料。先从 [Roadmap](/01-Java学习路线/01-语言与开发环境/README/)选择当前业务问题，理解背后的概念，再按需查看这里的 Java 写法与前端迁移差异。代码由自己或 AI 编写，都需要能解释数据、状态和失败如何流转；回到 Java 8 工程时另行核对可用语法和 API。

## 阅读顺序与运行方式

各节按写法索引排列，可根据 Roadmap 的概念入口选择阅读。目录综合案例展示这些概念如何共同实现一条业务规则。学习环境统一使用当前学习 JDK，构建和启动按 [工程构建](/01-Java学习路线/03-工程构建与协作/README/)准备；基础示例保留显式类和 main。

前端的 `console.log` 在这里对应 `System.out.println`。下面这个完整文件可以作为运行短示例的起点，文件名为 `Hello.java`：

```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, Vue");
    }
}
```

在文件所在目录运行 `javac -encoding UTF-8 Hello.java`，再运行 `java Hello`，得到 `Hello, Vue`。第一条命令编译，第二条命令执行。工具的细节在 [工程构建与协作](/01-Java学习路线/03-工程构建与协作/典型案例/)讲解。

后文标为“使用”的 Java 语句放在 Hello 的 main 方法内。普通类、接口和枚举可定义在 Hello 类外，放在同一目录的 Java 文件中；辅助方法按提示放在 Hello 类内、main 外。短示例用于解释概念；需要运行时，避免将不同小节中的同名类型放在同一目录。

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

### 相等比较的选择

`equals()` 是方法名，末尾有 s。根据比较目的选择：

| 目的 | 写法 | 场景 |
| --- | --- | --- |
| 比较基本类型的值 | `==` | int 数量、boolean 开关 |
| 判断两个引用是否指向同一对象 | `==` | 确认是否共用实例；与 null 比较也用它 |
| 按类定义的规则判断相等 | `equals()` | String 比内容；自定义类取决于是否重写 equals |

```java
String a = new String("Hello");
String b = new String("Hello");
System.out.println(a == b);
System.out.println(a.equals(b));
```

依次输出 `false` 和 `true`：两个独立对象，字符串内容相同。特意使用 new 是为了避免字符串常量复用掩盖身份与内容的区别。

`equals()` 并不自动逐字段比较。类没有重写它时，继承自 Object 的默认实现仍判断是否为同一个对象。Integer 等包装类型属于对象，不能依赖 `==` 比数值，缓存可能让部分结果看起来正确。

可能为空的对象可用 `java.util.Objects.equals(a, b)`：双方都为 null 返回 true，一方为 null 返回 false，否则调用 a.equals(b)。直接对 null 调用 equals 会抛出异常。

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

类定义一类对象有什么数据和行为，对象保存某一次创建得到的数据。下面用同一个 Person 比较默认名称与指定名称两种创建方式：

```javascript
class Person {
  // JS 只能声明一个 constructor，用默认参数支持省略名称。
  constructor(name = '匿名') {
    this.name = name;
  }

  sayHi() {
    return '你好 ' + this.name;
  }
}
console.log(new Person().sayHi());       // 你好 匿名
console.log(new Person('张三').sayHi()); // 你好 张三
```

Java 的完整类型定义如下，放在 Hello 类外：

```java
class Person {
    private final String name; // 外部不能直接访问，初始化后不能重新赋值。

    // Java 没有参数默认值语法，用不同参数列表的构造方法提供默认值。
    Person() {
        this("匿名"); // 调用下面的带参构造方法，复用初始化逻辑。
    }

    // 构造方法与类同名，不写返回类型；this.name 是字段，name 是参数。
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
System.out.println(new Person().sayHi());       // 你好 匿名
System.out.println(new Person("张三").sayHi()); // 你好 张三
```

两边输出相同。Java 的两个构造方法构成“重载”：参数数量、类型或类型排列顺序不同才算，只有参数名不同不算。

如果 Java 类没有声明任何构造方法，编译器会提供无参构造方法；一旦自己声明了构造方法，就不会再自动补充。因此本例如果删掉 `Person()`，`new Person()` 会编译失败。

### 不写访问修饰符时同包可访问

类中的字段、普通方法和显式声明的构造方法，如果没有写 `public`、`protected` 或 `private`，默认是包级可见（package-private）：同一个包内的其他类可以访问，其他包中的类不能直接访问。这里没有要补写的 `default` 关键字。

包由文件头的 `package` 声明确定，例如都声明 `package com.example;` 的类属于同一个包；`com.example.service` 是另一个包，不因名称有共同前缀就共享包级访问权限。

前面 Person 的 `sayHi()` 和两个构造方法都没有访问修饰符，因此同包内的 Hello 可以调用。后面的 Counter 的 `int value` 也同样是包级可见。Person 的 `private name` 则不能由 Hello 直接访问，即使两者同包。

因此，“没有写 private”还不足以判断权限：写了 public 就按 public，写了 protected 就按 protected，三个都没写才是包级可见。JS class 中未使用 `#` 的普通成员通常可公开访问，不能据此推断 Java 的默认权限。

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

输出 **2**。跟着代码看两个对象和两个变量：

1. `main` 创建对象 A，设置 `A.value = 1`，然后把对象 A 的引用传给 `change`。Java 传递的是引用的副本，所以方法内的参数 `counter` 和 `main` 中的变量是两个变量，但最初都指向对象 A。
2. `counter.value = 2` 修改的是对象 A 的字段。此时 `main` 中的变量仍指向 A，因此也能看到这个修改。
3. `counter = new Counter()` 创建对象 B，并让**方法内的参数**指向 B。`main` 中的变量仍指向 A。
4. `counter.value = 99` 修改的是对象 B，不影响对象 A。方法结束后，`main` 读取的仍是 A 的字段，所以输出 `2`。

`final` 约束的是 `main` 中的变量不能重新指向其他对象，并不禁止修改对象的字段。方法内的参数是另一个变量，可以重新赋值。

延伸阅读：[廖雪峰 Java 教程：方法（参数绑定）](https://liaoxuefeng.com/books/java/oop/basic/method/index.html)；[Dev.java：调用方法和构造方法（Passing Reference Data Type Arguments）](https://dev.java/learn/language/oop/classes-objects/calling-methods-constructors/)。

## 五 继承、接口与枚举

### 1. 继承与组合表达不同的关系

先看业务对象之间是什么关系，再选择代码写法：继承表示“属于同一种类型”，组合表示“通过持有其他对象协作”。只因为两个类有相似字段，就让它们继承同一个父类，容易把无关的职责绑在一起。

前端已有的 class 经验可以帮助理解 `extends` 与方法重写：

```javascript
class Animal { sound() { return '声音'; } }
class Dog extends Animal { sound() { return '汪'; } }
console.log(new Dog().sound());
```

```java
class Animal {
    String sound() { return "声音"; }
}
class Dog extends Animal {
    @Override
    String sound() { return "汪"; }
}
```

`new Dog().sound()` 返回 `汪`。`@Override` 让编译器检查重写关系，实际 Dog 对象提供自己的行为；调用者可以通过父类型约定使用它。在业务代码中，重点看调用方依赖什么契约、实现负责哪些行为、共享了哪些状态。

| 概念 | 阅读 Java 代码时要理解什么 |
| --- | --- |
| 继承 | 子类型需要满足父类型的约定；Java 类最多有一个直接父类，Object 没有父类 |
| 组合 | 一个对象持有另一个对象并调用其能力；例如 Dog 持有 Collar，狗并不是一种项圈 |
| 封装 | private 字段不能由独立子类直接读取；通过方法控制状态访问，protected 还涉及同包与跨包访问规则 |
| 构造与生命周期 | 构造方法负责初始化，`this(...)` 调用本类构造方法，`super(...)` 调用父类构造方法；构造方法不会被子类继承 |
| 多态与类型转换 | 变量类型约束可以调用哪些方法，实际对象决定重写实例方法的实现；强制转换不会改变对象本身，错误转换可能在运行时失败 |

完整语义按需读[继承](https://liaoxuefeng.com/books/java/oop/basic/inherit/index.html)与[多态](https://liaoxuefeng.com/books/java/oop/basic/polymorphic/index.html)。读 AI 生成的代码时，也要检查是否为复用少量代码引入了不合理的继承、暴露了内部状态，或假定某个对象一定具有特定子类型。

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

接口重点约定能力，抽象类还可以承载共享实现与状态；默认方法则让接口提供部分实现。具体语法按需查教程，阅读业务代码时先确定调用方依赖什么、替换实现是否仍满足约定。

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

[六 泛型](/01-Java学习路线/02-常用语言能力与标准库/典型案例/#六-泛型)

## 十一 编译与 JVM 的基本关系

现在回看最开始的两条命令：

```text
Hello.java  -- javac -->  Hello.class  -- java -->  执行 main
```

JDK 提供编译器等开发工具及运行能力；JVM 执行字节码。源码写错可能在编译阶段被拒绝，文件不存在等问题则发生在运行阶段。

本例先掌握程序如何开始执行、调用如何进入方法、对象怎样被引用。堆、线程栈、类加载、GC 与 JIT 的深入机制按实际运行问题继续查 [Roadmap](/01-Java学习路线/01-语言与开发环境/README/)。

[反射与运行时类型信息](/01-Java学习路线/02-常用语言能力与标准库/典型案例/#反射与运行时类型信息)

## 十二 综合案例 将前面知识用于目录处理

现在再看 [完整目录程序](/assets/01-Java学习路线/01-语言与开发环境/examples/DirectoryExample.java)。它把上面已经出现的概念组合起来，得到三个结果：

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

在章节目录中使用当前学习 JDK 执行以下命令；`--release 21` 固定本节的语言、API 与字节码目标：

```bash
cd ./01-Java学习路线/01-语言与开发环境
mkdir -p /tmp/study-java01-classes
javac --release 21 -encoding UTF-8 -d /tmp/study-java01-classes examples/*.java
java -cp /tmp/study-java01-classes DirectoryExample
```

以上主线命令要求当前学习 JDK。`-d` 指定编译结果目录，`-cp` 指定 JVM 到哪里查找类；旧项目的 Java 8 检查是单独的兼容任务。

### 继续串联值与文件处理

- [ValuesExample.java](/assets/01-Java学习路线/01-语言与开发环境/examples/ValuesExample.java)把字符串比较、空值、引用、金额与时间放在同一程序中。金额部分使用 BigDecimal 从十进制字符串构造；时间部分使用 Instant 表示确定时间点。它们是额外的数值与时间 API，不要求为了读懂前面的 List 而提前掌握。
- [DirectoryImportExample.java](/assets/01-Java学习路线/01-语言与开发环境/examples/DirectoryImportExample.java)把文件读取和异常处理组合起来：单行错误记录后继续，整个文件读取失败则报告失败。

运行入口及预期结果：

```bash
java -cp /tmp/study-java01-classes ValuesExample
java -cp /tmp/study-java01-classes DirectoryImportExample examples/directories.txt
```

导入示例应报告第 3、4、5 行错误，最终为 `有效 2 行，无效 3 行`。与最小异常示例比较，说明哪些失败允许继续、哪些失败必须结束本次操作。

### 在 Cursor 中调试 ValuesExample

安装 Extension Pack for Java 后，打开 [ValuesExample.java](/assets/01-Java学习路线/01-语言与开发环境/examples/ValuesExample.java)，等待 Java 语言服务加载完成。这个示例是独立 Java 文件，不需要 Maven。

1. 点击 `change(counter);` 左侧的行号边栏，设置红色断点。
2. 点击 `main` 上方的 **Debug** 启动当前文件。Run 只运行程序；仓库已有的 F5 配置指向 exercises 中的其他入口，调试本文件时优先使用这里的 Debug。
3. 暂停后，在 Variables（变量）查看 `counter.value`，此时为 1。断点停在该行执行之前。
4. 使用 Step Into（单步进入，通常为 F11）进入 `change`；使用 Step Over（单步跳过，通常为 F10）逐行执行，观察值从 1 变成 2。
5. 执行 `counter = new Counter()` 后，方法参数指向新对象；执行下一行后，新对象的 value 为 99。返回 main 后，调用方仍指向原对象，value 为 2。可以在 Call Stack（调用栈）切换 main 与 change，比较各自的变量。

也可以在 `int value = count;` 设置断点，观察 null 自动拆箱时如何进入 catch；在金额比较处观察 `a.equals(b)` 与 `a.compareTo(b)`。如果启用了捕获异常断点，程序可能先停在 NullPointerException，继续执行即可进入本例预设的 catch。

如果没有红色断点或 Debug 入口，按顺序检查：

1. 扩展页确认已安装并启用 Extension Pack for Java，特别是 Language Support for Java by Red Hat 和 Debugger for Java；仓库的扩展推荐列表不会自动安装它们。
2. 确认右下角语言模式为 Java，点击的是行号左侧的边栏。也可把光标放在可执行语句上，使用“运行 → 切换断点”（通常为 F9，Mac 可能需要 fn+F9）。
3. 扩展安装后，在命令面板执行 `Developer: Reload Window`，等待 Java 语言服务就绪再试。若边栏隐藏，在设置中搜索 `editor.glyphMargin` 并启用。

仍无法识别时，可用 Cursor 单独打开 `01-Java学习路线/01-语言与开发环境/examples` 文件夹，再打开本文件。

调试入口与变量查看能力见 [Java 调试扩展说明](https://code.visualstudio.com/docs/java/java-debugging)。本机 JDK 21 已编译运行此例，确认调用后的值为 2、十进制合计为 0.30；编辑器内断点操作需在 Cursor 中执行。

## 十三 业务验证

改动业务逻辑时，检查它是否兑现输入、状态和失败处理的约定。下面的目录与导入程序只演示内存处理和文件校验，不能证明真实服务的权限、数据库事务或远端调用正确。

| 业务情况 | 应确认的结果 | 对应材料 |
| --- | --- | --- |
| 返回可用目录 | 只返回 ACTIVE 目录，名称、编号与状态符合约定 | DirectoryExample 的过滤与索引逻辑 |
| 同一编号重复出现 | 明确拒绝，不静默覆盖原记录 | DirectoryExample 的 putIfAbsent 与错误处理 |
| 导入中包含不合法行 | 按约定记录行号与原因，合法行和失败行统计正确 | DirectoryImportExample 的行校验 |
| 整个文件无法读取 | 报告整体失败，不能当作空文件导入成功 | DirectoryImportExample 的读取边界 |
| 真实服务中的越权、写入失败和远端重试 | 检查允许与拒绝、持久化状态、外部副作用，以及是否重复执行 | 结合 [事务](/01-Java学习路线/04-数据库与持久化/数据变更与事务/README/)、[权限](/01-Java学习路线/06-测试与安全/身份认证与权限/README/)、[服务协作](/01-Java学习路线/08-缓存、消息与服务协作/服务间调用/README/)检查实际需求 |

保留当前需求的输入、预期、实际结果与必要日志。需要处理金额、时间、共享状态或资源限制时，再补对应边界。验证范围由当前需求的规则、状态与副作用决定。

## 参考与验证范围

- **中文主读：** [廖雪峰快速入门](https://liaoxuefeng.com/books/java/quick-start/index.html)、[面向对象](https://liaoxuefeng.com/books/java/oop/index.html)、[泛型](https://liaoxuefeng.com/books/java/generics/index.html)：按当前知识点顺序阅读。
- **中文主读：** [集合](https://liaoxuefeng.com/books/java/collection/index.html)与[函数式编程](https://liaoxuefeng.com/books/java/functional/index.html)：核对最小示例，再进入综合案例。
- **官方补充：** [Dev.java 语言基础](https://dev.java/learn/language/constructs/)、[泛型与模式匹配](https://dev.java/learn/language/fp/)、[集合和 Stream](https://dev.java/learn/api/collections-and-streams/)。
- **兼容查阅：** [Oracle Java 8 教程](https://docs.oracle.com/javase/tutorial/java/)用于旧项目兼容范围核对；主线版本与资料顺序见 [学习版本与中文资料](/参考资料/学习版本与中文资料/)。

验证范围与执行记录见[示例验证说明](/参考资料/示例验证说明/)。参考源码、正文片段和真实业务分别核验；读懂示例后，仍要检查实际需求的规则、状态与副作用。

[返回本节 Roadmap](/01-Java学习路线/01-语言与开发环境/README/) · [返回总纲](/)
