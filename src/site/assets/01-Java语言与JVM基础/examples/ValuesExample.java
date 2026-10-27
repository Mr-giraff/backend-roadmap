import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;

public class ValuesExample {
    static final class Counter {
        int value;
    }

    static void change(Counter counter) {
        counter.value = 2;
        counter = new Counter();
        counter.value = 99;
    }

    public static void main(String[] args) {
        String first = new String("ADS");
        String second = new String("ADS");
        System.out.println("同一对象：" + (first == second));
        System.out.println("内容相同：" + first.equals(second));

        Integer count = null;
        try {
            int value = count;
            System.out.println(value);
        } catch (NullPointerException e) {
            System.out.println("空包装值不能自动拆箱");
        }

        final Counter counter = new Counter();
        counter.value = 1;
        change(counter);
        System.out.println("调用后的值：" + counter.value);

        BigDecimal sum = new BigDecimal("0.10").add(new BigDecimal("0.20"));
        System.out.println("十进制合计：" + sum.toPlainString());
        BigDecimal a = new BigDecimal("1.0");
        BigDecimal b = new BigDecimal("1.00");
        System.out.println("金额 equals：" + a.equals(b));
        System.out.println("数值 compareTo：" + (a.compareTo(b) == 0));

        long id = 9007199254740993L;
        System.out.println("接口编号文本：" + Long.toString(id));
        Instant createdAt = Instant.parse("2026-10-26T01:00:00Z");
        System.out.println("上海时间：" + createdAt.atZone(ZoneId.of("Asia/Shanghai")));
    }
}
