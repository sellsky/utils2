package tk.bolovsrol.utils.localcache;

import tk.bolovsrol.utils.log.Log;

import java.lang.ref.ReferenceQueue;

/**
 * Синглтон.
 * <p/>
 * Тред-демон, который чистит ссылки на протухшие вхождения.
 */
class WeakReferenceCleaner extends Thread {

    private static final WeakReferenceCleaner INSTANCE = new WeakReferenceCleaner();

    static {
        INSTANCE.start();
    }

    public static WeakReferenceCleaner getInstance() {
        return INSTANCE;
    }

    /** Очередь, в которой скапливаются ссылки. */
    private final ReferenceQueue<Object> queue = new ReferenceQueue<>();

    private WeakReferenceCleaner() {
        super("LocalCacheWeakReferenceCleaner");
        this.setDaemon(true);
    }

    @Override public void run() {
        try {
            while (true) {
                ((CachedObjectReference<?, ?>) queue.remove()).discard();
            }
        } catch (InterruptedException ignored) {
            // ok
        } catch (Throwable e) {
            Log.exception(e);
        }
    }

    public ReferenceQueue<Object> getQueue() {
        return queue;
    }
}
