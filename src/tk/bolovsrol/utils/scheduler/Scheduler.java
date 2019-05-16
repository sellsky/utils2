package tk.bolovsrol.utils.scheduler;

import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.log.Log;

import java.util.Date;

/**
 * Шедулер является lightweight-заменой стандартному шедулеру.
 * <p/>
 * Зато этот шедулер можно юзать в виде синглтона или делать себе
 * специальную инстанцию, которой можно задавать
 * название рабочего треда.
 */
public class Scheduler extends Thread {

    private static final class StaticContainer {
        private static final Scheduler STATIC = new Scheduler("StaticScheduler");
    }

    private final Object lock = new Object();

    private SchedulerEntry<Task> top;
    private SchedulerEntry<Task> bottom;

    public Scheduler(String name) {
        this(name, true);
    }

    public Scheduler(String name, boolean startImmediately) {
        super(name);
        this.setDaemon(true);
        if (startImmediately) {
            this.start();
        }
    }

    public static Scheduler getStatic() {
        return StaticContainer.STATIC;
    }

    /**
     * Ставит задание в очередь в указанную дату. Если в качестве даты передан нул, ничего не делает.
     * <p/>
     * Задание будет выполнено один раз.
     *
     * @param task задание, которое надо выполнить
     * @param activationOrNull время, после которого нужно выполнить задание, или нул, если задание выполнять не нужно
     */
    public void scheduleOnce(Runnable task, Date activationOrNull) {
        schedule(Task.once(task), activationOrNull);
    }

    /**
     * Ставит задание в очередь в указанную дату. Если в качестве даты передан нул, ничего не делает.
     *
     * @param task задание, которое надо выполнить
     * @param activationOrNull время, после которого нужно выполнить задание, или нул, если задание выполнять не нужно
     */
    public void schedule(Task task, Date activationOrNull) {
        if (activationOrNull == null) { return; }
        synchronized (lock) {
            SchedulerEntry<Task> newEntry = new SchedulerEntry<Task>(activationOrNull, task);
            if (top == null) {
                top = newEntry;
                bottom = newEntry;
            } else {
                SchedulerEntry<Task> entry = bottom;
                while (true) {
                    if (!entry.getActivation().after(activationOrNull)) {
                        // нашли первый таск с датой запуска меньше нужной нам.
                        // запихнём наш таск за ним
                        if (entry.isLast()) {
                            bottom = newEntry;
                        } else {
                            entry.getNext().setPrev(newEntry);
                            newEntry.setNext(entry.getNext());
                        }
                        entry.setNext(newEntry);
                        newEntry.setPrev(entry);
                        break;
                    }
                    if (entry.isFirst()) {
                        top = newEntry;
                        entry.setPrev(newEntry);
                        newEntry.setNext(entry);
                        break;
                    }
                    entry = entry.getPrev();
                }
            }
            lock.notifyAll();
        }
    }

    public boolean cancel(Task task) {
        synchronized (lock) {
            SchedulerEntry<Task> entry = top;
            while (entry != null) {
                if (entry.getTask() == task) {
                    if (entry.isFirst()) {
                        top = entry.getNext();
                    } else {
                        entry.getPrev().setNext(entry.getNext());
                    }
                    if (entry.isLast()) {
                        bottom = entry.getPrev();
                    } else {
                        entry.getNext().setPrev(entry.getPrev());
                    }
                    return true;
                }
                entry = entry.getNext();
            }
            return false;
        }
    }

    private void removeTop() {
        if (top.isLast()) {
            top = null;
            bottom = null;
        } else {
            top = top.getNext();
            top.setPrev(null);
        }
    }

    @Override public void run() {
        try {
            while (!isInterrupted()) {
                while (!isInterrupted()) {
                    Task task;
                    synchronized (lock) {
                        while (top == null) {
                            lock.wait();
                        }
                        while (true) {
                            long tts = top.getActivation().getTime() - System.currentTimeMillis();
                            if (tts <= 0L) {
                                break;
                            }
                            lock.wait(tts);
                        }
                        task = top.getTask();
                        removeTop();
                    }
                    schedule(task, task.execute());
                }
            }
        } catch (InterruptedException e) {
            // слипы вывалиться могут, если нас кто-нить прервёт
        } catch (Throwable e) {
            Log.exception(e);
        }
    }

    @Override
    public String toString() {
        StringDumpBuilder sdb = new StringDumpBuilder();
        SchedulerEntry<Task> entry = top;
        while (entry != null) {
            sdb.append(entry.toString());
            entry = entry.getNext();
        }
        return sdb.toString();
    }
}
