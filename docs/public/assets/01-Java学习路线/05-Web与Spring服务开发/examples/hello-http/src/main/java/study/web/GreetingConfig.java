package study.web;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class GreetingConfig {
    @Bean
    GreetingFormatter greetingFormatter(@Value("${lesson.prefix:Hello}") String prefix) {
        return new PlainGreetingFormatter(prefix);
    }
}
