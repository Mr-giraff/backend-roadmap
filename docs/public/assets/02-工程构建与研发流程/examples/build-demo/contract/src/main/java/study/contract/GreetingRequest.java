package study.contract;

public final class GreetingRequest {
    private final String name;

    public GreetingRequest(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }
}
