package tk.bolovsrol.utils.scheduler;

import tk.bolovsrol.utils.StringDumpBuilder;

import java.util.Date;

/**
 *
 */
public class SchedulerEntry<T extends Task> {
    private final Date activation;
    private final T task;

    private SchedulerEntry<T> prev;
    private SchedulerEntry<T> next;

    public SchedulerEntry(Date activation, T task) {
        this.activation = activation;
        this.task = task;
    }

    public Date getActivation() {
        return activation;
    }

    public T getTask() {
        return task;
    }

    boolean isFirst() {
        return prev == null;
    }

    SchedulerEntry<T> getPrev() {
        return prev;
    }

    void setPrev(SchedulerEntry<T> prev) {
        this.prev = prev;
    }

    boolean isLast() {
        return next == null;
    }

    SchedulerEntry<T> getNext() {
        return next;
    }

    void setNext(SchedulerEntry<T> next) {
        this.next = next;
    }

    @Override
    public String toString() {
        return new StringDumpBuilder()
                .append("activation", activation)
                .append("task", task)
                .toString();
    }
}
