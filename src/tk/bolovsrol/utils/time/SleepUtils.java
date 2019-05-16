package tk.bolovsrol.utils.time;

import tk.bolovsrol.utils.ArrayUtils;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.log.LogDome;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/** Методы, связанные со спячкой или ожиданием. */
public final class SleepUtils {
    private SleepUtils() {
    }

    /**
     * Спит до ближайшего указанного часа.
     * <p/>
     * Метод возвращает управление, когда наступит ближйший час
     * из указанных в передаваемом массиве.
     * <p/>
     * Если log не null, то в него будет написан хинт о времени спячки.
     *
     * @param desiredHours отсортированный по возрастанию значений массив чисел --
     *                     часов, когда нужно просыпаться
     * @param log          если лог указан, то в него напишут хинт о времени спячки. Может быть null.
     * @throws InterruptedException
     */
    public static void sleepUntilDesiredHourStart(int[] desiredHours, LogDome log) throws InterruptedException {
        long now = System.currentTimeMillis();
        long scheduledMillis = getDesiredHourMillis(now, desiredHours);
        if (log != null) {
            log.info("Sleeping until " + Spell.get(new Date(scheduledMillis)));
        }
        while (now < scheduledMillis) {
            long tts = scheduledMillis - now;
            Thread.sleep(tts);
            now = System.currentTimeMillis();
        }
    }

    /**
     * Спит до ближайшего указанного часа.
     * <p/>
     * Метод возвращает управление, когда наступит ближйший час
     * из указанных в передаваемом массиве.
     * <p/>
     * В лог будет написан хинт о времени спячки.
     *
     * @param desiredHours отсортированный по возрастанию значений массив чисел --
     *                     часов, когда нужно просыпаться
     * @throws InterruptedException
     */
    public static void sleepUntilDesiredHourStart(int[] desiredHours) throws InterruptedException {
        sleepUntilDesiredHourStart(desiredHours, Log.getInstance());
    }

    public static long getDesiredHourMillis(long now, int[] desiredHours) {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(now);

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int i;
        if (hour > desiredHours[desiredHours.length - 1]) {
            i = 0;
        } else {
            i = desiredHours.length - 1;
            while (i > 0 && (hour >= desiredHours[i] || hour < desiredHours[i - 1])) {
                i--;
            }
        }

        c.set(Calendar.HOUR_OF_DAY, desiredHours[i]);
        TimeUtils.resetMinutes(c);

        long timeInMillis = c.getTimeInMillis();
        return timeInMillis < now ? timeInMillis + TimeUtils.MS_IN_DAY : timeInMillis;
    }

    /**
     * Возвращает управление, когда указанная дата уже наступила.
     *
     * @param wakeDate
     * @throws InterruptedException
     */
    public static void sleepUntil(Date wakeDate) throws InterruptedException {
        if (wakeDate == null) {
            return;
        }
        sleepUntil(wakeDate.getTime());
    }

    /**
     * Возвращает управление, когда указанный момент времени уже наступил.
     *
     * @param wakeMillis
     * @throws InterruptedException
     */
    public static void sleepUntil(long wakeMillis) throws InterruptedException {
        while (true) {
            long now = System.currentTimeMillis();
            if (now >= wakeMillis) {
                return;
            }
            Thread.sleep(wakeMillis - now);
        }
    }

    /**
     * Возвращает управление не ранее, чем пройдёт ближайшее время дня, указанное
     * в одном из переданных интервалов. Время локальное.
     * <p/>
     * Интервалы рассматриваются как совокупность миллисекунд, секунд, минут и часов.
     * Это даёт возможность пользоваться указанными временами и в дни перевода часов.
     * Количество суток игнорируется.
     *
     * @param durations
     * @throws InterruptedException
     */
    public static void sleepUntilTimeOfDay(LogDome log, Duration... durations) throws InterruptedException {
        Date now = new Date();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(TimeUtils.getStartOfDay(now));
        Date wakeDate = null;
        while (true) {
            for (Duration duration : durations) {
                cal.set(Calendar.MILLISECOND, duration.getMillisPart());
                cal.set(Calendar.SECOND, duration.getSecondsPart());
                cal.set(Calendar.MINUTE, duration.getMinutesPart());
                cal.set(Calendar.HOUR_OF_DAY, duration.getHoursPart());
                Date probablyWakeDate = cal.getTime();
                if (probablyWakeDate.after(now) && (wakeDate == null || wakeDate.after(probablyWakeDate))) {
                    wakeDate = probablyWakeDate;
                }
            }
            if (wakeDate != null) {
                break;
            }
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.add(Calendar.DATE, 1);
        }
        if (log != null) {
            log.trace("Sleeping until " + Spell.get(wakeDate));
        }
        sleepUntil(wakeDate);
    }

    /**
     * Возращает управление не ранее чем наступит нужное вермя одного из нужных дней недели.
     *
     * @param log
     * @param durations
     * @param days
     */
    public static void sleepUntilTimeOfDayOnDay(LogDome log, Duration[] durations, int[] days) throws InterruptedException {
        if (days == null) {
            sleepUntilTimeOfDay(log, durations);
            return;
        }
        Date now = new Date();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(TimeUtils.getStartOfDay(now));
        Date wakeDate = null;
        while (true) {
            if (!ArrayUtils.contains(days, cal.get(Calendar.DAY_OF_WEEK))) {
                cal.add(Calendar.DATE, 1);
                continue;
            }
            for (Duration duration : durations) {
                cal.set(Calendar.MILLISECOND, duration.getMillisPart());
                cal.set(Calendar.SECOND, duration.getSecondsPart());
                cal.set(Calendar.MINUTE, duration.getMinutesPart());
                cal.set(Calendar.HOUR_OF_DAY, duration.getHoursPart());
                Date probablyWakeDate = cal.getTime();
                if (probablyWakeDate.after(now) && (wakeDate == null || wakeDate.after(probablyWakeDate))) {
                    wakeDate = probablyWakeDate;
                }
            }
            if (wakeDate != null) {
                break;
            }
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.add(Calendar.DATE, 1);
        }
        if (log != null) {
            log.trace("Sleeping until " + Spell.get(wakeDate));
        }
        sleepUntil(wakeDate);
    }

    /**
     * Спит по меньшей мере указанное количество миллисекунд.
     * <p/>
     * Старается поспать как можно меньше.
     *
     * @param millis время спячки
     * @throws InterruptedException ололо!
     */
    public static void sleepAtLeast(long millis) throws InterruptedException {
        sleepUntil(new Date(System.currentTimeMillis() + millis));

    }

    /**
     * Спит по меньшей мере указанную продолжительность.
     * <p/>
     * Старается поспать как можно меньше.
     *
     * @param duration продолжительность спячки
     * @throws InterruptedException ололо!
     */
    public static void sleepAtLeast(Duration duration) throws InterruptedException {
        sleepAtLeast(duration.getMillis());
    }

    /**
     * Возвращает управление, когда <code>{@link SleepUtils.Condition#isReady() condition.isReady()}</code> вернёт true
     * или пройдёт <code>maxTime</code>, что случится раньше.
     * <p/>
     * Condition должен сделать notify() себя при изменении состояния.
     * <p/>
     * Метод нужно вызывать в синхронизированном по condition контексте:<br/>
     * <code><br/>
     * {@link SleepUtils.Condition} condition = ...<br/>
     * synchronized(condition){<br/>
     * ... // тут запуск влияющего на condition механизма<br/>
     * TimeUtils.wait(1000L, condition)<br/>
     * }<br/>
     * </code>
     *
     * @param maxTime
     * @param condition
     * @return true, если сработало условие, false, если условие не сработало, но уже истёк таймаут
     */
    public static boolean waitCondition(long maxTime, Condition condition) throws InterruptedException {
        if (condition.isReady()) {
            return true;
        }
        long sleepUntil = System.currentTimeMillis() + maxTime;
        while (true) {
            if (condition.isReady()) {
                return true;
            }
            long timeToSleep = sleepUntil - System.currentTimeMillis();
            if (timeToSleep <= 0L) {
                return false;
            }
            condition.wait(timeToSleep);
        }
    }

    /** Интерфейс для возврата из ожидания из {@link SleepUtils#waitCondition}. */
    public interface Condition {
        boolean isReady();
    }
}
