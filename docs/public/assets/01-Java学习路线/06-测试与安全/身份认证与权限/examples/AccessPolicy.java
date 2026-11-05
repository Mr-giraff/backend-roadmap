public final class AccessPolicy {
    public boolean canRead(Actor actor, Resource resource) {
        return actor != null && resource != null
                && actor.tenantId == resource.tenantId
                && actor.projects.contains(resource.projectId);
    }
    public boolean canEdit(Actor actor, Resource resource) {
        return canRead(actor, resource) && actor.canEdit;
    }
}
