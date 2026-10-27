package study.hello;

public class Main {
    public static void main(String[] args) {
        String name = args.length == 0 ? "Vue veteran" : args[0];
        System.out.println("Hello, " + name);
    }
}
