package study.hello;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ConfigMain {
    public static void main(String[] args) throws IOException {
        Properties config = new Properties();
        try (InputStream input = ConfigMain.class.getResourceAsStream("/app.properties")) {
            if (input == null) {
                throw new IllegalStateException("app.properties not found on classpath");
            }
            config.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        }
        String prefix = System.getProperty("greeting.prefix", config.getProperty("greeting.prefix"));
        System.out.println(prefix + ", Vue veteran");
    }
}
