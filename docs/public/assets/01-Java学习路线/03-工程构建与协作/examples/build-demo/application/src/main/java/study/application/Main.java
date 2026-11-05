package study.application;

import study.contract.GreetingRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class Main {
    public static void main(String[] args) throws IOException {
        Properties config = loadConfig();
        String prefix = setting("greeting.prefix", "GREETING_PREFIX", config);
        String name = args.length == 0 ? "Vue veteran" : args[0];
        GreetingService service = new GreetingService(prefix);
        System.out.println("app=" + config.getProperty("app.name"));
        System.out.println(service.greet(new GreetingRequest(name)));
    }

    private static Properties loadConfig() throws IOException {
        try (InputStream input = Main.class.getResourceAsStream("/app.properties")) {
            if (input == null) {
                throw new IllegalStateException("app.properties not found on classpath");
            }
            Properties config = new Properties();
            config.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            return config;
        }
    }

    private static String setting(String key, String envKey, Properties config) {
        String property = System.getProperty(key);
        if (property != null) {
            return property;
        }
        String environment = System.getenv(envKey);
        return environment == null ? config.getProperty(key) : environment;
    }
}
