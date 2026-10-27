import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyExamples {
    static final class Counter {
        int value;
    }

    static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    static int lostUpdate() throws InterruptedException {
        Counter counter = new Counter();
        CountDownLatch bothRead = new CountDownLatch(2);
        CountDownLatch write = new CountDownLatch(1);
        Runnable task = () -> {
            int old = counter.value;
            bothRead.countDown();
            await(write);
            counter.value = old + 1;
        };
        Thread first = new Thread(task);
        Thread second = new Thread(task);
        first.start();
        second.start();
        bothRead.await();
        write.countDown();
        first.join();
        second.join();
        return counter.value;
    }

    static int atomicCount() throws InterruptedException {
        AtomicInteger count = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Thread thread = new Thread(() -> {
                await(start);
                for (int n = 0; n < 1000; n++) {
                    count.incrementAndGet();
                }
            });
            threads.add(thread);
            thread.start();
        }
        start.countDown();
        for (Thread thread : threads) {
            thread.join();
        }
        return count.get();
    }

    static boolean boundedPool() throws InterruptedException {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 0L,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1),
                new ThreadPoolExecutor.AbortPolicy());
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        boolean rejected = false;
        try {
            pool.execute(() -> { started.countDown(); await(release); });
            started.await();
            pool.execute(() -> { });
            try {
                pool.execute(() -> { });
            } catch (RejectedExecutionException e) {
                rejected = true;
            }
        } finally {
            release.countDown();
            pool.shutdown();
            if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        }
        return rejected;
    }

    static void timeoutAndCancellation() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch cleaned = new CountDownLatch(1);
        try {
            Future<String> future = pool.submit(() -> {
                started.countDown();
                try {
                    release.await();
                    return "done";
                } finally {
                    cleaned.countDown();
                }
            });
            started.await();
            try {
                future.get(20, TimeUnit.MILLISECONDS);
                throw new AssertionError("task should still be waiting");
            } catch (TimeoutException expected) {
                System.out.println("timeout: taskDone=" + future.isDone());
            }
            future.cancel(true);
            if (!cleaned.await(2, TimeUnit.SECONDS)) {
                throw new AssertionError("cooperative task did not clean up");
            }
            System.out.println("future cancellation: cleanup=true");
        } finally {
            release.countDown();
            pool.shutdownNow();
            pool.awaitTermination(2, TimeUnit.SECONDS);
        }
    }

    static void completableCancellation() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(1);
        try {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                started.countDown();
                try {
                    await(release);
                    return "done";
                } finally {
                    finished.countDown();
                }
            }, pool);
            started.await();
            future.cancel(true);
            System.out.println("completable cancellation: running=" + (finished.getCount() == 1));
            release.countDown();
            if (!finished.await(2, TimeUnit.SECONDS)) {
                throw new AssertionError("underlying task did not finish");
            }
        } finally {
            release.countDown();
            pool.shutdownNow();
            pool.awaitTermination(2, TimeUnit.SECONDS);
        }
    }

    static void contextCleanup() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        ThreadLocal<String> tenant = new ThreadLocal<>();
        try {
            pool.submit(() -> {
                tenant.set("tenant-A");
                try {
                    if (!"tenant-A".equals(tenant.get())) {
                        throw new AssertionError("context missing");
                    }
                } finally {
                    tenant.remove();
                }
            }).get();
            String next = pool.submit(() -> tenant.get()).get();
            System.out.println("next task context=" + next);
        } finally {
            pool.shutdownNow();
            pool.awaitTermination(2, TimeUnit.SECONDS);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("lost update=" + lostUpdate());
        System.out.println("atomic count=" + atomicCount());
        System.out.println("bounded pool rejected=" + boundedPool());
        timeoutAndCancellation();
        completableCancellation();
        contextCleanup();
    }
}
