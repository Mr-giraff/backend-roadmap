package study.web;
import java.util.Collections;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
public class GreetingController {
    private final GreetingService service;
    public GreetingController(GreetingService service) { this.service = service; }
    @GetMapping("/hello")
    public Map<String, String> hello(@RequestParam(name = "name", defaultValue = "Vue") String name) {
        return Collections.singletonMap("message", service.greet(name));
    }
    @GetMapping("/echo/{id}")
    public Map<String, String> echo(@PathVariable("id") Long id) {
        return Collections.singletonMap("id", id.toString());
    }
    @PostMapping(value = "/greetings", consumes = "application/json")
    public Map<String, String> create(@Valid @RequestBody GreetingRequest request) {
        return Collections.singletonMap("message", service.greet(request.getName()));
    }
}
