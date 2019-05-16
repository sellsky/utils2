package tk.bolovsrol.utils.log;

import tk.bolovsrol.utils.ErrorReportingWriter;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.log.providers.LogWriterProvider;
import tk.bolovsrol.utils.properties.Cfg;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Накапливает данные в буфере, возвращая управление до того,
 * как фактически информация будет записана в файл.
 * <p/>
 * Записывает лог отдельным тредом.
 * <p/>
 * Нормально держит поток лога закрытым. Открывает для того,
 * чтобы сбросить из буфера накопившуюся информацию.
 * <p/>
 * Поэтому лог-файлы можно удалять прямо из-под программы.
 */
class ThreadedLogWriter extends Thread implements LogWriter {

    /**
     * Закрывать ли поток лога в паузах.
     * <p/>
     * В обычном режиме поток лога закрывается только при завершении работы.
     * <p/>
     * При использовании виндовс-систем и записи лога в файл установка этого параметра в true
     * даст возможность убирать файл с логом из-под работающего приложения.
     * Не рекомендуется к использованию, так как вносит дополнительный оверхед.
     */
	private static final boolean CLOSE_WHEN_IDLE = Cfg.getBoolean("log.writer.closeWhenIdle", false);

	/**
	 * Максимальный размер очереди строк лога для записи,
	 * при превышении желающие записать в лог будут ждать,
     * а в логе появится соответствующее примечание.
     */
	private static final int MAX_BUFFER_SIZE = Cfg.getInteger("log.writer.maxBufferSize", 65536, null);

    /** Отладочный параметр, добавляет в новый поток строчку с параметрами. */
	private static final boolean PRINT_SETTINGS = Cfg.getBoolean("log.writer.printSettings", false);

	/** Очередь, в которую кладут сообщения для печати. */
	private final Queue<LogData> queue = new ConcurrentLinkedQueue<>();

    /**
     * Счётчик записей, которые нужно поместить в очередь.
     * <p/>
     * Так, если счётчик превышает {@link #MAX_BUFFER_SIZE}, значит происходит переполнение.
     */
    private final AtomicInteger queueCounter = new AtomicInteger();

    /** Замок, которым мониторят изменение {@link #queueCounter} c 0 на 1. */
    private final Object emptyLock = new Object();

    /** Замок, которым мониторят изменение {@link #queueCounter} свыше {@link #MAX_BUFFER_SIZE}. */
    private final Object overflowLock = new Object();

    /**
     * Признак, что о переполнении сообщили.
     * Чтобы не забивать лог постоянными сообщениями о переполнении, используем этот флажок.
     */
    private boolean overflowReported = false;

    /** Источник потока для вывода. */
    private final LogWriterProvider writerProvider;

    /**
     * В режиме завершения работы основной тред может быть убит,
     * и сообщения печатаются в вызывающих тредах.
     */
    private volatile boolean shutdown = false;

    /** Синхронизатор печати */
    private final Object printLock = new Object();

    public ThreadedLogWriter(LogWriterProvider writerProvider) {
        super("Log-" + writerProvider.getCaption());
        this.writerProvider = writerProvider;
        setDaemon(true);
        Runtime.getRuntime().addShutdownHook(
              new Thread("LogKiller-" + writerProvider.getCaption()) {
                  @Override public void run() {
                      shutdown = true;
                      ThreadedLogWriter.this.interrupt();
                      synchronized (printLock) {
                          printThenClose();
                      }
                  }
              }
        );
        start();
    }

    @Override
    public void run() {
        synchronized (printLock) {
            try {
                while (!isInterrupted()) {
                    awaitThenPrint();
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public void write(LogData ald) {
        boolean interrupted = false;
        while (true) {
            try {
                int newQueueSize = queueCounter.incrementAndGet();

                // переполнение? будем ждать
                while (newQueueSize > MAX_BUFFER_SIZE) {
                    synchronized (overflowLock) {
                        newQueueSize = queueCounter.get();
                        while (queueCounter.get() > MAX_BUFFER_SIZE) {
                            overflowLock.wait();
                        }
                    }
                }

                queue.add(ald);

                // первый элемент? надо пнуть тред-печататель
                if (newQueueSize == 1) {
                    synchronized (emptyLock) {
                        //noinspection NakedNotify
                        emptyLock.notifyAll();
                    }
                }

                break;
            } catch (InterruptedException ignored) {
                // по историческим причинам бросить исключение мы не можем; при выходе из метода прервём тред снова
                interrupted = true;
            }
        }

        if (shutdown) {
            synchronized (printLock) {
                printThenClose();
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Если очередь не пуста, то печатает текущее содержимое очереди
     * и затем закрывает поток.
     */
    private void printThenClose() {
        if (queueCounter.get() > 0) {
            print();
        }
        closeProvider();
    }

    /**
     * Дожидается появления в очереди хотя бы одного элемента,
     * печатает текущее содержимое очереди и  возвращает управление, когда очередь опустеет.
     */
    private void awaitThenPrint() throws InterruptedException {
        overflowReported = false;
        synchronized (emptyLock) {
            while (queueCounter.get() == 0) {
                emptyLock.wait();
            }
        }
        print();
        if (CLOSE_WHEN_IDLE) {
            closeProvider();
        }
    }

    private void closeProvider() {
        try {
            writerProvider.close();
        } catch (IOException e) {
            System.err.println("Error closing Stream Provider " + writerProvider.getCaption());
            e.printStackTrace(System.err);
        }
    }

    /**
     * Печатает переданный элемент и за ним текущее содержимое очереди.
     * Возвращает управление, когда очередь опустеет.
     * <p/>
     * Предусловие: очередь не пуста.
     */
    private void print() {
        try {
            @SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
            PrintWriter w = new PrintWriter(new ErrorReportingWriter(writerProvider.getWriter()));
            if (writerProvider.isNewWriter()) {
                if (PRINT_SETTINGS) {
                    w.println("-- log.writer.maxBufferSize=" + MAX_BUFFER_SIZE + " log.writer.closeWhenIdle=" + CLOSE_WHEN_IDLE);
                }
            }
            while (true) {
                LogData ald = queue.poll();
                if (ald == null) {
                    w.flush();
                    return;
                }

                ald.print(w);
                int newQueueSize = queueCounter.decrementAndGet();

                if (!overflowReported && newQueueSize > MAX_BUFFER_SIZE) {
                    w.println("-- log queue overflow detected");
                    System.err.println("Log queue overflow detected at thread " + Spell.get(getName()));
                    overflowReported = true;
                }
                if (newQueueSize == MAX_BUFFER_SIZE) {
                    synchronized (overflowLock) {
                        //noinspection NakedNotify
                        overflowLock.notifyAll();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Logging I/O failed.");
            e.printStackTrace(System.err);
        }
    }

    @Override public String toString() {
        return new StringDumpBuilder()
              .append("writerProvider", writerProvider)
              .append("shutdown", shutdown)
              .toString();
    }
}
