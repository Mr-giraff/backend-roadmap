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

本篇是配合主教程使用的代码阅读材料。先从 [Roadmap](/01-Java语言与JVM基础/README/)选择当前业务问题，理解背后的概念，再按需查看这里的 Java 写法与前端迁移差异。代码由自己或 AI 编写，都需要能解释数据、状态和失败如何流转；回到 Java 8 工程时另行核对可用语法和 API。

## 阅读顺序与运行方式

各节按写法索引排列，可根据 Roadmap 的概念入口选择阅读。目录综合案例展示这些概念如何共同实现一条业务规则。学习环境统一使用当前学习 JDK，构建和启动按 [02 工程构建](/02-工程构建与研发流程/README/)准备；基础示例保留显式类和 main。

前端的 `console.log` 在这里对应 `System.out.println`。下面这个完整文件可以作为运行短示例的起点，文件名为 `Hello.java`：

```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, Vue");
    }
}
```

在文件所在目录运行 `javac -encoding UTF-8 Hello.java`，再运行 `java Hello`，得到 `Hello, Vue`。第一条命令编译，第二条命令执行。工具的细节在 [02](/02-工程构建与研发流程/典型案例/)讲解。

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

访问范围先掌握：public 允许公开访问，private 限于本类；不写修饰符通常表示同包可访问，protected 还涉及子类访问规则。

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

本例先掌握程序如何开始执行、调用如何进入方法、对象怎样被引用。堆、线程栈、类加载、GC 与 JIT 的深入机制按实际运行问题继续查 [Roadmap](/01-Java语言与JVM基础/README/)。

## 现代 Java 写法补充

前面的显式类型、普通类和 main 仍是当前学习 JDK 的合法写法。读到现代写法时，重点确认它是否改变类型约束、状态可变性或执行行为，具体语法随用随查：

| 已学概念 | 中文主读 | 要掌握的差异 |
| --- | --- | --- |
| 局部变量与类型 | [var 类型推断](https://liaoxuefeng.com/books/java/quick-start/basic/var-type/index.html) | 编译器从初始化值推断类型，变量并未变成动态类型 |
| 条件与分支 | [switch 表达式](https://liaoxuefeng.com/books/java/quick-start/flow/switch/index.html) | 分支可以产生一个结果；与旧式 switch 的语句写法区分 |
| 类与数据字段 | [record 记录类](https://liaoxuefeng.com/books/java/oop/core/record/index.html) | 用于简单数据载体，访问器是字段同名方法，组件引用不自动使内容深度不可变 |
| List、Set、Map | [中文集合教程](https://liaoxuefeng.com/books/java/collection/index.html)与 [Dev.java](https://dev.java/learn/api/collections-and-streams/) | 认识 List.of、Set.of、Map.of 的不可修改结果及空值约束；可变元素仍可能变化 |

这些写法可在遵循学习基线的独立工程中使用。迁移到 Java 8 兼容代码时，需分别采用显式类型、普通类、旧式分支和该版本可用的集合构造方式；`--release 8` 会拒绝不兼容的语法和 API。

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

在章节目录中使用当前学习 JDK 执行以下命令；`--release 21` 固定本节的语言、API 与字节码目标：

```bash
cd ./01-Java语言与JVM基础
mkdir -p /tmp/study-java01-classes
javac --release 21 -encoding UTF-8 -d /tmp/study-java01-classes examples/*.java
java -cp /tmp/study-java01-classes DirectoryExample
```

以上主线命令要求当前学习 JDK。`-d` 指定编译结果目录，`-cp` 指定 JVM 到哪里查找类；旧项目的 Java 8 检查是单独的兼容任务。

### 继续串联值与文件处理

- [ValuesExample.java](/assets/01-Java语言与JVM基础/examples/ValuesExample.java)把字符串比较、空值、引用、金额与时间放在同一程序中。金额部分使用 BigDecimal 从十进制字符串构造；时间部分使用 Instant 表示确定时间点。它们是额外的数值与时间 API，不要求为了读懂前面的 List 而提前掌握。
- [DirectoryImportExample.java](/assets/01-Java语言与JVM基础/examples/DirectoryImportExample.java)把文件读取和异常处理组合起来：单行错误记录后继续，整个文件读取失败则报告失败。

运行入口及预期结果：

```bash
java -cp /tmp/study-java01-classes ValuesExample
java -cp /tmp/study-java01-classes DirectoryImportExample examples/directories.txt
```

导入示例应报告第 3、4、5 行错误，最终为 `有效 2 行，无效 3 行`。与最小异常示例比较，说明哪些失败允许继续、哪些失败必须结束本次操作。

## 十三 业务验证

改动业务逻辑时，检查它是否兑现输入、状态和失败处理的约定。下面的目录与导入程序只演示内存处理和文件校验，不能证明真实服务的权限、数据库事务或远端调用正确。

| 业务情况 | 应确认的结果 | 对应材料 |
| --- | --- | --- |
| 返回可用目录 | 只返回 ACTIVE 目录，名称、编号与状态符合约定 | DirectoryExample 的过滤与索引逻辑 |
| 同一编号重复出现 | 明确拒绝，不静默覆盖原记录 | DirectoryExample 的 putIfAbsent 与错误处理 |
| 导入中包含不合法行 | 按约定记录行号与原因，合法行和失败行统计正确 | DirectoryImportExample 的行校验 |
| 整个文件无法读取 | 报告整体失败，不能当作空文件导入成功 | DirectoryImportExample 的读取边界 |
| 真实服务中的越权、写入失败和远端重试 | 检查允许与拒绝、持久化状态、外部副作用，以及是否重复执行 | 结合 [06 事务](/06-事务与数据一致性/README/)、[12 权限](/12-安全与权限/README/)、[09 服务协作](/09-分布式系统与服务协作/README/)检查实际需求 |

保留当前需求的输入、预期、实际结果与必要日志。需要处理金额、时间、共享状态或资源限制时，再补对应边界。验证范围由当前需求的规则、状态与副作用决定。

## 参考与验证范围

- **中文主读：** [廖雪峰快速入门](https://liaoxuefeng.com/books/java/quick-start/index.html)、[面向对象](https://liaoxuefeng.com/books/java/oop/index.html)、[泛型](https://liaoxuefeng.com/books/java/generics/index.html)：按当前知识点顺序阅读。
- **中文主读：** [集合](https://liaoxuefeng.com/books/java/collection/index.html)与[函数式编程](https://liaoxuefeng.com/books/java/functional/index.html)：核对最小示例，再进入综合案例。
- **官方补充：** [Dev.java 语言基础](https://dev.java/learn/language/constructs/)、[泛型与模式匹配](https://dev.java/learn/language/fp/)、[集合和 Stream](https://dev.java/learn/api/collections-and-streams/)。
- **兼容查阅：** [Oracle Java 8 教程](https://docs.oracle.com/javase/tutorial/java/)用于旧项目兼容范围核对；主线版本与资料顺序见 [学习版本与中文资料](/参考资料/学习版本与中文资料/)。

验证范围与执行记录见[示例验证说明](/参考资料/示例验证说明/)。参考源码、正文片段和真实业务分别核验；读懂示例后，仍要检查实际需求的规则、状态与副作用。

[返回本节 Roadmap](/01-Java语言与JVM基础/README/) · [返回总纲](/)
