package tk.bolovsrol.utils.time;

import java.util.Date;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Содержит пул времени.
 * <p/>
 * Из пула можно запросить нужное количество времени относительно даты,
 * и пул вернёт ближайшее возможное время так, чтобы никому больше это время не досталось.
 * <p/>
 * Можно также возвращать время обратно в пул.
 */
public class TimePool {

    private final NavigableMap<Long, Long> d = new TreeMap<Long, Long>();

    /** Создаёт пустой пул без доступного времени. */
    public TimePool() {
    }

    /**
     * Создаёт пул с указанным интервалом времени
     * от указанной даты.
     *
     * @param since
     * @param length
     * @see #getSinceDate(java.util.Date)
     */
    public TimePool(Date since, Duration length) {
        this(since.getTime(), length.getMillis());
    }

    /**
     * Создаёт пул с указанным интервалом времени
     * от указанной даты..
     *
     * @param sinceMillis дата в миллисекундах
     * @param lengthMillis интервал в миллисекундах
     * @see #getSinceMillis(long)
     */
    public TimePool(long sinceMillis, long lengthMillis) {
        if (sinceMillis + lengthMillis < 0) {
            throw new IllegalArgumentException("Length is too big.");
        }
        d.put(sinceMillis, lengthMillis);
    }

    /**
     * Удаляет доступное время до указанной даты.
     * <p/>
     * Можно использовать для оптимизации использования памяти,
     * удаляя время, которое уже не будет использовано.
     *
     * @param until
     */
    public void dropUntil(Date until) {
        drop(Long.MIN_VALUE, until.getTime());
    }

    /**
     * Удаляет доступное время в указанном интервале дат.
     * <p/>
     * Можно использовать для оптимизации использования памяти,
     * удаляя время, которое уже не будет использовано.
     *
     * @param since
     * @param until
     */
    public void dropRange(Date since, Date until) {
        drop(since.getTime(), until.getTime());
    }

    /**
     * Удаляет доступное время в указанном интервале дат.
     * <p/>
     * Можно использовать для оптимизации использования памяти,
     * удаляя время, которое уже не будет использовано.
     *
     * @param sinceMillis
     * @param untilMillis
     */
    public void drop(long sinceMillis, long untilMillis) {
        pierce(sinceMillis);
        pierce(untilMillis);
        d.subMap(sinceMillis, true, untilMillis, false).clear();
    }

    /**
     * делает так, чтобы через указанную точку не проходил отрезок
     *
     * @param millis
     */
    private void pierce(long millis) {
        Map.Entry<Long, Long> entry = d.floorEntry(millis);
        if (entry == null) {
            return;
        }
        long end = entry.getKey().longValue() + entry.getValue().longValue();
        if (end > millis) {
            d.put(entry.getKey(), millis - entry.getKey().longValue());
            d.put(millis, end - millis);
        }
    }

    /**
     * Возвращает начало самого старшего отрезка времени
     * требуемой длины не старше указанной даты.
     * <p/>
     * Если таких нет, возвращает нул.
     *
     * @param since
     * @param length
     * @return
     */
    public Date retrieve(Date since, Duration length) {
        Long result = retrieve(since.getTime(), length.getMillis());
        return result == null ? null : new Date(result.longValue());
    }

    /**
     * Возвращает начало самого старшего отрезка времени
     * требуемой длины не старше указанной даты.
     * <p/>
     * Если таких нет, возвращает нул.
     *
     * @param sinceMillis
     * @param lengthMillis
     * @return
     */
    public Long retrieve(long sinceMillis, long lengthMillis) {
        pierce(sinceMillis);
        while (true) {
            Map.Entry<Long, Long> entry = d.ceilingEntry(sinceMillis);
            if (entry == null) {
                return null;
            }
            long restMillis = entry.getValue().longValue() - lengthMillis;
            if (restMillis >= 0) {
                // нашли подходящее
                d.remove(entry.getKey());
                if (restMillis > 0) {
                    d.put(entry.getKey().longValue() + lengthMillis, restMillis);
                }
                return entry.getKey();
            }
            sinceMillis = entry.getKey().longValue() + entry.getValue().longValue();
        }
    }

    /**
     * Указывает, что время с указанной даты указанной продолжительности
     * доступно для использования.
     *
     * @param since
     * @param length
     */
    public void put(Date since, Duration length) {
        put(since.getTime(), length.getMillis());
    }

    /**
     * Указывает, что время с указанной даты указанной продолжительности
     * доступно для использования.
     *
     * @param sinceMillis
     * @param lengthMillis
     */
    public void put(long sinceMillis, long lengthMillis) {
        if (sinceMillis + lengthMillis < 0) {
            throw new IllegalArgumentException("Length is too big.");
        }

        // смотрим на соседние отрезки к потенциальному
        // сначала нижний: если есть пересечения, будем оперировать суммарным
        long actualSinceMillis;
        Map.Entry<Long, Long> floorEntry = d.lowerEntry(sinceMillis);
        if (floorEntry != null && floorEntry.getKey().longValue() + floorEntry.getValue().longValue() >= sinceMillis) {
            lengthMillis = sinceMillis + lengthMillis - floorEntry.getKey().longValue();
            actualSinceMillis = floorEntry.getKey().longValue();
        } else {
            actualSinceMillis = sinceMillis;
        }

        // теперь верхний
        Map.Entry<Long, Long> ceilingEntry = d.ceilingEntry(sinceMillis);
        if (ceilingEntry == null || ceilingEntry.getKey().longValue() > sinceMillis + lengthMillis) {
            // верхнего отрезка нет либо он слишком высоко
            d.put(actualSinceMillis, lengthMillis);
        } else {
            // есть пересечение с верхним отрезком
            d.remove(ceilingEntry.getKey());
            d.put(actualSinceMillis, ceilingEntry.getValue().longValue() + ceilingEntry.getKey().longValue() - actualSinceMillis);
        }
    }

    /**
     * Создаёт пул от указанной даты
     * и до максимально возможной.
     *
     * @param since
     * @return
     */
    public static TimePool getSinceDate(Date since) {
        return new TimePool(since, new Duration(Long.MAX_VALUE - since.getTime()));
    }

    /**
     * Создаёт пул от указанной даты
     * и до максимально возможной.
     *
     * @param sinceMillis
     * @return
     */
    public static TimePool getSinceMillis(long sinceMillis) {
        return new TimePool(sinceMillis, Long.MAX_VALUE - sinceMillis);
    }

}
