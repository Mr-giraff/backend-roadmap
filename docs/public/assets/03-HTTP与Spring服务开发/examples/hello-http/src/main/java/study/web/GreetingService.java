package study.web;
import org.springframework.stereotype.Service;
@Service
public class GreetingService {
    private final GreetingFormatter formatter;
    public GreetingService(GreetingFormatter formatter) { this.formatter = formatter; }
    public String greet(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return formatter.format(name.trim());
    }
}
