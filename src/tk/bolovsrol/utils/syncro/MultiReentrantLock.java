package tk.bolovsrol.utils.syncro;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Содержит замки {@link ReentrantLock} для каждого из используемых идентификаторов так, чтобы в одно время для одного идентификатора существовал только один замок.
 * <p>
 * А когда замок не нужен, ликвидирует его.
 * <p>
 * Все методы аналогичны методам ReentrantLock, но с указанием идентификатора.
 *
 * @param <L>
 * @see WeakProvider
 * @see LockQueuedSynchronizer
 */
public class MultiReentrantLock<L> {

    private final WeakProvider<L, ReentrantLock> idToLock;

    public MultiReentrantLock() {
        idToLock = new WeakProvider<>(id -> new ReentrantLock());
    }

    public MultiReentrantLock(boolean fair) {
        idToLock = new WeakProvider<>(id -> new ReentrantLock(fair));
    }

    public ReentrantLock get(L id) {
        return idToLock.get(id);
    }

    public void lock(L id) {get(id).lock();}

    public void lockInterruptibly(L id) throws InterruptedException {get(id).lockInterruptibly();}

    public boolean tryLock(L id) {return get(id).tryLock();}

    public boolean tryLock(L id, long timeout, TimeUnit unit) throws InterruptedException {return get(id).tryLock(timeout, unit);}

    public void unlock(L id) {get(id).unlock();}

    public Condition newCondition(L id) {return get(id).newCondition();}

    public int getHoldCount(L id) {return get(id).getHoldCount();}

    public boolean isHeldByCurrentThread(L id) {return get(id).isHeldByCurrentThread();}

    public boolean isLocked(L id) {return get(id).isLocked();}

    public boolean isFair(L id) {return get(id).isFair();}

    public boolean hasQueuedThreads(L id) {return get(id).hasQueuedThreads();}

    public boolean hasQueuedThread(L id, Thread thread) {return get(id).hasQueuedThread(thread);}

    public int getQueueLength(L id) {return get(id).getQueueLength();}

    public boolean hasWaiters(L id, Condition condition) {return get(id).hasWaiters(condition);}

    public int getWaitQueueLength(L id, Condition condition) {return get(id).getWaitQueueLength(condition);}


}
