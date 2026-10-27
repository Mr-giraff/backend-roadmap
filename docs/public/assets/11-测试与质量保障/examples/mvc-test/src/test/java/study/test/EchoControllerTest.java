package study.test;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EchoControllerTest {
    @RestController
    public static class EchoController {
        @GetMapping("/echo/{id}")
        public String echo(@PathVariable("id") Long id) {
            return id.toString();
        }
    }

    @Test
    void acceptsNumericId() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new EchoController()).build();
        mvc.perform(get("/echo/12"))
                .andExpect(status().isOk())
                .andExpect(content().string("12"));
    }

    @Test
    void rejectsNonNumericId() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new EchoController()).build();
        mvc.perform(get("/echo/abc"))
                .andExpect(status().isBadRequest());
    }
}
