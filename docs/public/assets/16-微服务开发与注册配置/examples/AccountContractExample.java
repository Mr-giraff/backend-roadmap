import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AccountContractExample {
    interface AccountQuery {
        AccountView findById(String accountId);
    }

    static final class AccountView {
        final String id;
        final String tenantId;
        final String displayName;

        AccountView(String id, String tenantId, String displayName) {
            this.id = id;
            this.tenantId = tenantId;
            this.displayName = displayName;
        }
    }

    static final class InMemoryAccountQuery implements AccountQuery {
        public AccountView findById(String accountId) {
            return "a-1".equals(accountId)
                    ? new AccountView("a-1", "tenant-a", "Vue") : null;
        }
    }

    static final class Actor {
        final String subjectId;
        final String tenantId;
        final Set<String> permissions;

        Actor(String subjectId, String tenantId, String... permissions) {
            this.subjectId = subjectId;
            this.tenantId = tenantId;
            this.permissions = new HashSet<String>(Arrays.asList(permissions));
        }
    }

    static final class AccountReader {
        private final AccountQuery query;

        AccountReader(AccountQuery query) {
            this.query = query;
        }

        String read(Actor actor, String accountId) {
            if (actor == null) {
                throw new IllegalStateException("UNAUTHENTICATED");
            }
            if (!actor.permissions.contains("account:read")) {
                throw new IllegalStateException("FORBIDDEN");
            }
            AccountView account = query.findById(accountId);
            if (account == null || !account.tenantId.equals(actor.tenantId)) {
                throw new IllegalStateException("FORBIDDEN");
            }
            return account.displayName;
        }
    }

    static void show(AccountReader reader, String label, Actor actor) {
        try {
            System.out.println(label + ": " + reader.read(actor, "a-1"));
        } catch (IllegalStateException e) {
            System.out.println(label + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        AccountQuery query = new InMemoryAccountQuery();
        System.out.println("local-call: " + query.findById("a-1").displayName);
        AccountReader reader = new AccountReader(query);
        show(reader, "allowed", new Actor("u-1", "tenant-a", "account:read"));
        show(reader, "wrong-tenant", new Actor("u-2", "tenant-b", "account:read"));
        show(reader, "missing-permission", new Actor("u-3", "tenant-a"));
        show(reader, "no-identity", null);
    }
}
