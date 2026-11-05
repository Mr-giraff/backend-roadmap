import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class VirtualThreadExample {
    public static void main(String[] args) throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<Boolean> result = executor.submit(() -> Thread.currentThread().isVirtual());
            System.out.println("virtual=" + result.get());
        }
    }
}
