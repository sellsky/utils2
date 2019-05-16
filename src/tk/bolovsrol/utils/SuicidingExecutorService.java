package tk.bolovsrol.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Передаёт сервису-делегату задачи до тех пор, когда ни одной активной задачи не останется,
 * после чего автоматически останавливается.
 */
public class SuicidingExecutorService implements ExecutorService {

    private final AtomicInteger activeTaskCount = new AtomicInteger(0);
    private final ExecutorService executorService;

    private final Object joinLock = new Object();

    public final class WatchedRunnable implements Runnable {
        private final Runnable delegate;

        public WatchedRunnable(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override public void run() {
            delegate.run();
            taskDone();
        }

        public Runnable getDelegate() {
            return delegate;
        }
    }

    public final class WatchedCallable<V> implements Callable<V> {
        private final Callable<V> delegate;

        public WatchedCallable(Callable<V> delegate) {
            this.delegate = delegate;
        }

        @Override public V call() throws Exception {
            V result = delegate.call();
            taskDone();
            return result;
        }

        public Callable<V> getDelegate() {
            return delegate;
        }
    }

    public SuicidingExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Блокирует выполнение до тех пор, когда когда все задачи выполнены,
     * а эксекутор-сервис умер.
     *
     * @throws InterruptedException
     */
    public void join() throws InterruptedException {
        synchronized (joinLock) {
            while (!executorService.isShutdown()) {
                joinLock.wait();
            }
        }
    }

    private void taskDone() {
        if (activeTaskCount.decrementAndGet() == 0) {
            synchronized (joinLock) {
                executorService.shutdown();
                joinLock.notifyAll();
            }
        }
    }

    @Override public void shutdown() {
        executorService.shutdown();
    }

    @Override public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }

    @Override public boolean isShutdown() {
        return executorService.isShutdown();
    }

    @Override public boolean isTerminated() {
        return executorService.isTerminated();
    }

    @Override public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

    @Override public <T> Future<T> submit(Callable<T> task) {
        activeTaskCount.incrementAndGet();
        return executorService.submit(new WatchedCallable<>(task));
    }

    @Override public <T> Future<T> submit(Runnable task, T result) {
        activeTaskCount.incrementAndGet();
        return executorService.submit(new WatchedRunnable(task), result);
    }

    @Override public Future<?> submit(Runnable task) {
        activeTaskCount.incrementAndGet();
        return executorService.submit(new WatchedRunnable(task));
    }

    private <T> Collection<WatchedCallable<T>> wrapWatched(Collection<? extends Callable<T>> tasks) {
        Collection<WatchedCallable<T>> watchedCallables = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            watchedCallables.add(new WatchedCallable<>(task));
        }
        return watchedCallables;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        activeTaskCount.addAndGet(tasks.size());
        return executorService.invokeAll(wrapWatched(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        activeTaskCount.addAndGet(tasks.size());
        return executorService.invokeAll(wrapWatched(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        activeTaskCount.addAndGet(tasks.size());
        return executorService.invokeAny(wrapWatched(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        activeTaskCount.addAndGet(tasks.size());
        return executorService.invokeAny(wrapWatched(tasks), timeout, unit);
    }

    @Override public void execute(Runnable command) {
        activeTaskCount.incrementAndGet();
        executorService.execute(new WatchedRunnable(command));
    }
}
