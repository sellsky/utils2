package tk.bolovsrol.utils.socket;

import tk.bolovsrol.utils.conf.AutoConfiguration;
import tk.bolovsrol.utils.conf.Param;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.reflectiondump.ReflectionDump;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * Для каждого входящего соединения создаёт новый тред,
 * в котором запускает переданный в конструкторе процессор.
 * <p/>
 * Во избежание излишней нагрузки можно определить таймаут,
 * который должен пройти между двумя соседними запусками тредов,
 * и/или максимальное количество активных тредов.
 */
public class ProcessorSocketListener extends AbstractSocketListener {
    private final SocketProcessor socketProcessor;
    private final Conf conf;

    private final Semaphore activeThreadSem ;

    public static class Conf extends AutoConfiguration {
        @Param(desc = "максимальное количество одновременно обслуживаемых соединений")
        public int maxActiveThreadCount = 1024;

        @Param(desc = "показывать треддамп всех активных тредов при достижении maxActiveThreadCount")
        public boolean dumpThreadsAtWaiting = false;
    }

    // это для дебага
    private final Queue<ThreadContainer> activeThreads;

    private static class ThreadContainer {
        final Date created;
        final Thread thread;

        public ThreadContainer(Date created, Thread thread) {
            this.created = created;
            this.thread = thread;
        }

        @Override public String toString() {
            return ReflectionDump.getFor(this);
        }

        @Override public boolean equals(Object that) {
            return that instanceof ThreadContainer && this.thread.equals(((ThreadContainer) that).thread);
        }

        @Override public int hashCode() {
            return this.thread.hashCode();
        }
    }

    /**
     * Создаёт процессор и назначает его указанному.
     *
     * @param log
     * @param socketEndpoint
     * @param socketProcessor
     */
    public ProcessorSocketListener(LogDome log, SocketEndpoint socketEndpoint, SocketProcessor socketProcessor, Conf conf) {
        super("PSL-" + socketEndpoint.getBindSocketAddress() + '-' + socketEndpoint.getSocketFactory().getCaption(), log, socketEndpoint.getBindSocketAddress(), socketEndpoint.getSocketFactory());
        this.socketProcessor = socketProcessor;
        this.conf = conf;
        activeThreadSem = new Semaphore(conf.maxActiveThreadCount,true);
        log.info(conf.toString());
        activeThreads = conf.dumpThreadsAtWaiting ? new ConcurrentLinkedQueue<ThreadContainer>() : null;
    }

    @Override protected void accept(final Socket socket) {
        try {
            //noinspection ObjectToString
            final String connectionCaption = socket.getRemoteSocketAddress() + " -> " + socket.getLocalSocketAddress();
            log.hint("Processing incoming connection " + connectionCaption);

            // проверим ограничение количества тредов-обработчиков
            log.trace("Processor threads: " + (conf.maxActiveThreadCount - activeThreadSem.availablePermits()) + " active, " + activeThreadSem.getQueueLength() + " waiting");
            if(!activeThreadSem.tryAcquire()){
                log.warning("Max active thread count limit " + conf.maxActiveThreadCount + " reached, waiting...");
                if (activeThreads != null) {
                    dumpActiveThreadsToLog();
                }
                activeThreadSem.acquire();
            }

            // новый тред
            //noinspection ObjectToString
            final Thread thread = new SocketThread(socket, connectionCaption);
            if (activeThreads != null) {
                activeThreads.add(new ThreadContainer(new Date(), thread));
            }
            thread.start();

        } catch (InterruptedException ignored) {
            try {
                socket.close();
            } catch (IOException ee) {
                // ignore
            }
        }
    }

    private void dumpActiveThreadsToLog() {
        log.hint("+++++ Active threads thread dump start (in order of appearance) +++++");
        for (ThreadContainer at : activeThreads) {
            log.hint(at.toString());
            for (StackTraceElement ste : at.thread.getStackTrace()) {
                log.hint("  " + ste.toString());
            }
        }
        log.hint("===== Active threads thread dump end =====");
    }

    public SocketProcessor getSocketProcessor() {
        return socketProcessor;
    }

    private class SocketThread extends Thread {
        private final Socket socket;
        private final String connectionCaption;

        @SuppressWarnings("ObjectToString")
        public SocketThread(Socket socket, String connectionCaption) {
            super("Socket-" + socket.getRemoteSocketAddress());
            this.socket = socket;
            this.connectionCaption = connectionCaption;
        }

        @Override public void run() {
            boolean finallyClose = true;
            try {
                log.trace("Starting processor...");
                finallyClose = socketProcessor.process(socket);
            } catch (Throwable e) {
                log.exception(e);
                finallyClose = true;
            } finally {
                if (finallyClose) {
                    log.trace("Closing incoming connection " + connectionCaption);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        log.trace("Error closing socket (we merely ignore it). ", e);
                    }
                    log.hint("Closed incoming connection " + connectionCaption);
                }
                if (activeThreads != null) {
                    activeThreads.remove(new ThreadContainer(null, this));
                }
                activeThreadSem.release();
            }
        }
    }

//    public static void main(String[] args) {
//        Conf conf = new Conf();
//        conf.maxActiveThreadCount = 1024;
//        conf.debugDumpThreadsAtWaiting = true;
//        SocketEndpoint socketEndpoint = new HttpEndpoint(PlainServerSocketFactory.getStatic(), InetSocketAddress.createUnresolved("localhost", 9000), Method.GET, "/");
//        ProcessorSocketListener psl = new ProcessorSocketListener(Log.getInstance(), socketEndpoint, null, conf);
//
//        Thread thread = new Thread("Zuka"){
//            @Override public void run() {
//                try {
//                    Thread.sleep(1000000L);
//                } catch (InterruptedException e) {
//                    Log.exception(e);
//                }
//            }
//        };
//        thread.start();
//        ThreadContainer dc = new ThreadContainer(new Date(), thread);
//        psl.activeThreads.add(dc);
//        Log.hint(psl.activeThreads.size());
//        psl.dumpActiveThreadsToLog();
//              psl.activeThreads.remove(new ThreadContainer(null, thread));
//        Log.hint(psl.activeThreads.size());
//    }
}
