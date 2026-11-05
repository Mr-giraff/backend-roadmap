package study.application;

import study.contract.GreetingRequest;

public final class GreetingCheck {
    public static void main(String[] args) {
        GreetingService service = new GreetingService("Hello");
        String result = service.greet(new GreetingRequest(" Vue veteran "));
        if (!"Hello, Vue veteran".equals(result)) {
            throw new AssertionError("unexpected greeting: " + result);
        }
        try {
            new GreetingRequest(" ");
            throw new AssertionError("blank name should be rejected");
        } catch (IllegalArgumentException expected) {
            if (!"name must not be blank".equals(expected.getMessage())) {
                throw new AssertionError("unexpected rejection", expected);
            }
        }
        System.out.println("GreetingCheck: PASS (normal, trim, blank)");
    }
}
