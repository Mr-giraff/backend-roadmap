package study.application;

import study.contract.GreetingRequest;

public final class GreetingService {
    private final String prefix;

    public GreetingService(String prefix) {
        this.prefix = prefix;
    }

    public String greet(GreetingRequest request) {
        return prefix + ", " + request.getName();
    }
}
