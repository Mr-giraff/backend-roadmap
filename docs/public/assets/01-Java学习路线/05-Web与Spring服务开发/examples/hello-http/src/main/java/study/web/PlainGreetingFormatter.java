package study.web;
public class PlainGreetingFormatter implements GreetingFormatter {
    private final String prefix;
    public PlainGreetingFormatter(String prefix) { this.prefix = prefix; }
    public String format(String name) { return prefix + ", " + name; }
}
