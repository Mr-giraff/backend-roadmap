import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class OpsDemo {
    public static void main(String[] args) throws IOException {
        String version = System.getProperty("demo.version", "v1");
        boolean ready = Boolean.parseBoolean(System.getProperty("demo.ready", "true"));
        int delay = Integer.parseInt(System.getProperty("demo.delayMs", "0"));
        if (delay < 0 || delay > 5000) {
            throw new IllegalArgumentException("demo.delayMs must be between 0 and 5000");
        }
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 18080), 8);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 0L,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(4));
        server.setExecutor(executor);
        server.createContext("/", exchange -> {
            long started = System.nanoTime();
            String path = exchange.getRequestURI().getPath();
            String id = exchange.getRequestHeaders().getFirst("X-Request-Id");
            if (id == null || !id.matches("[A-Za-z0-9_-]{1,64}")) {
                id = UUID.randomUUID().toString();
            }
            int status = 404;
            String body = "NOT_FOUND";
            try {
                if ("/healthz".equals(path)) {
                    status = 200;
                    body = "ALIVE";
                } else if ("/readyz".equals(path)) {
                    status = ready ? 200 : 503;
                    body = ready ? "READY" : "NOT_READY";
                } else if ("/work".equals(path)) {
                    status = ready ? 200 : 503;
                    body = ready ? "version=" + version : "NOT_READY";
                    if (ready) {
                        Thread.sleep(delay);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                status = 503;
                body = "INTERRUPTED";
            }
            try {
                reply(exchange, status, body);
            } finally {
                exchange.close();
                long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
                System.out.println("requestId=" + id + " path=" + path + " status=" + status
                        + " elapsedMs=" + elapsed + " version=" + version);
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop(1);
            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
        }));
        server.start();
        System.out.println("listening=127.0.0.1:18080 version=" + version + " ready=" + ready);
    }

    private static void reply(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = (body + "\n").getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
