public final class NameChecker {
    private final NameCatalog catalog;
    public NameChecker(NameCatalog catalog) { this.catalog = catalog; }
    public String requireAvailable(String raw) {
        String name = NameRules.normalize(raw);
        if (catalog.exists(name)) {
            throw new IllegalStateException("名称已存在");
        }
        return name;
    }
}
