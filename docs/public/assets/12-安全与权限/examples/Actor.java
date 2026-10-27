import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
public final class Actor {
    public final long userId;
    public final long tenantId;
    public final Set<Long> projects;
    public final boolean canEdit;
    public Actor(long userId, long tenantId, Set<Long> projects, boolean canEdit) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.projects = Collections.unmodifiableSet(new HashSet<>(projects));
        this.canEdit = canEdit;
    }
}
