public final class NameRules {
    private NameRules() {}
    public static String normalize(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("名称不能为空");
        }
        return raw.trim();
    }
}
