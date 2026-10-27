public final class Template {
    public enum Status { DRAFT, PUBLISHED }
    private final long id;
    private final String name;
    private Status status;
    public Template(long id, String name) {
        this.id = id;
        this.name = name;
        this.status = Status.DRAFT;
    }
    public void publish() {
        if (status != Status.DRAFT) {
            throw new IllegalStateException("只有草稿可以发布");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("名称不能为空");
        }
        status = Status.PUBLISHED;
    }
    public long getId() { return id; }
    public Status getStatus() { return status; }
}
