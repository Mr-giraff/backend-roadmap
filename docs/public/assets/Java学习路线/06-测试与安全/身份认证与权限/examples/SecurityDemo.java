import java.util.Collections;
public class SecurityDemo {
    public static void main(String[] args) {
        AccessPolicy policy = new AccessPolicy();
        Actor reader = new Actor(1L, 10L, Collections.singleton(100L), false);
        Actor editor = new Actor(2L, 10L, Collections.singleton(100L), true);
        Resource own = new Resource(7L, 10L, 100L);
        Resource foreign = new Resource(8L, 20L, 100L);
        System.out.println("reader read own: " + policy.canRead(reader, own));
        System.out.println("reader edit own: " + policy.canEdit(reader, own));
        System.out.println("editor edit own: " + policy.canEdit(editor, own));
        System.out.println("editor read foreign: " + policy.canRead(editor, foreign));
    }
}
