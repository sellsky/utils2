package tk.bolovsrol.utils.syncro;

import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.function.ThrowingRunnable;

import java.util.Collection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Запускает несколько задач параллельно и ждёт, когда они завершатся.
 * <p>
 * Задача может выкинуть исключение, оно будет сохранено.
 *
 * @param <R>
 */
public class ParallelTasks<R extends ThrowingRunnable<? extends Exception>> {
    /**
     * Информация об исключениях, выброшенных задачами.
     *
     * @param <R>
     */
    public static class Failure<R extends ThrowingRunnable<? extends Exception>> {
        private final String name;
        private final R worker;
        private final Exception exception;

        private Failure(String name, R worker, Exception exception) {
            this.name = name;
            this.worker = worker;
            this.exception = exception;
        }

        public String getName() {
            return name;
        }

        public R getWorker() {
            return worker;
        }

        public Exception getException() {
            return exception;
        }

        @Override
        public String toString() {
            return new StringDumpBuilder()
                    .append("name", name)
                    .append("worker", worker)
                    .append("exception", exception)
                    .toString();
        }
    }

    /**
     * Класс, предназначенный для запуска нескольких параллельных тредов и ожидания либо завершения их работы, либо
     */
    private final VersionParking vp = new VersionParking();
    private final ConcurrentLinkedQueue<Thread> participants = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Failure<R>> failures = new ConcurrentLinkedQueue<>();
    private boolean interruptOnFailure = true;

    public ParallelTasks() {
    }

    public ParallelTasks(boolean interruptOnFailure) {
        this.interruptOnFailure = interruptOnFailure;
    }

    public boolean await() throws InterruptedException {
        vp.parkWhile(() -> !participants.isEmpty());
        if (failures.isEmpty()) {
            return true;
        } else {
            if (interruptOnFailure) participants.forEach(Thread::interrupt);
            return false;
        }
    }

    public Collection<Failure<R>> getFailures() {
        return failures;
    }

    public void forEachFailure(Consumer<Failure<R>> consumer) {
        failures.forEach(consumer);
    }

    public void forEachFailureExceptInterrupted(Consumer<Failure<R>> consumer) {
        failures.forEach(f -> {
            if (!(f.exception instanceof InterruptedException)) {
                consumer.accept(f);
            }
        });
    }

    public void launch(String threadName, R worker) {
        if (!failures.isEmpty()) {
            failures.add(new Failure<>(threadName, worker, new CancellationException()));
        } else {
            Thread thread = new Thread(threadName) {
                @Override
                public void run() {
                    try {
                        worker.run();
                    } catch (Exception e) {
                        failures.add(new Failure<>(threadName, worker, e));
                    } finally {
                        participants.remove(this);
                        vp.nextVersion();
                    }
                }
            };
            participants.add(thread);
            thread.start();
        }
    }

}
