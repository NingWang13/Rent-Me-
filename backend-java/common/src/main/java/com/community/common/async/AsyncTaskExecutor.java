package com.community.common.async;

import java.util.concurrent.*;

public class AsyncTaskExecutor {

    private final ExecutorService executor;

    public AsyncTaskExecutor() {
        this.executor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                new ThreadFactory() {
                    private int count = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("async-task-" + count++);
                        thread.setDaemon(true);
                        return thread;
                    }
                }
        );
    }

    public void execute(Runnable task) {
        executor.execute(task);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
