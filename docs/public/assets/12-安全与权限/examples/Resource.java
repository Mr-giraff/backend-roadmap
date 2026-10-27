public final class Resource {
    public final long id;
    public final long tenantId;
    public final long projectId;
    public Resource(long id, long tenantId, long projectId) {
        this.id = id;
        this.tenantId = tenantId;
        this.projectId = projectId;
    }
}
