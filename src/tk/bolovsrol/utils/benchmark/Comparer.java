package tk.bolovsrol.utils.benchmark;

/**
 * Сравнивает производительность двух {@link Repeater}`ов.
 * <p/>
 * Сначала нужно запустить тест методом run(),
 * а затем можно снимать сливки.
 *
 * @see Repeater
 */
public class Comparer {
    private final Repeater older;
    private final Repeater newer;

    private long[] olderResults;
    private long[] newerResults;

    public Comparer(Repeater older, Repeater newer) {
        this.older = older;
        this.newer = newer;
    }

    public long getNewerAverage() {
        return getAverage(newerResults);
    }

    public long getNewerTotal() {
        return getTotal(newerResults);
    }

    public long getOlderAverage() {
        return getAverage(olderResults);
    }

    public long getOlderTotal() {
        return getTotal(olderResults);
    }

    private static long getAverage(long[] array) {
        return getTotal(array) / (long) array.length;
    }

    private static long getTotal(long[] array) {
        long avg = 0L;
        for (long anArray : array) {
            avg += anArray;
        }
        return avg;
    }

    public long getAverageDiff() {
        return getOlderAverage() - getNewerAverage();
    }

    public long getTotalDiff() {
        return getOlderTotal() - getNewerTotal();
    }

    /**
     * Выполняет итерацию каждого Repeater`а по count раз.
     * <p/>
     * То же, что run(count, 1);
     *
     * @param count счётчик
     */
    public void run(int count) {
        run(count, 1);
    }

    /**
     * Выполняет итерацию каждого Repeater`а по count раз.
     * <p/>
     * Повторяет этот цикл iterations раз.
     *
     * @param count      счётчик
     * @param iterations счётчик
     */
    public void run(int count, int iterations) {
        olderResults = new long[iterations];
        newerResults = new long[iterations];
        for (int i = 0; i < iterations; i++) {
            olderResults[i] = older.run(count);
            newerResults[i] = newer.run(count);
        }
    }

    public long[] getNewerResults() {
        return copyArray(newerResults);
    }

    public long[] getOlderResults() {
        return copyArray(olderResults);
    }

    private static long[] copyArray(long[] array) {
        long[] copy = new long[array.length];
        System.arraycopy(array, 0, copy, 0, array.length);
        return copy;
    }

    public String getAverageReport() {
        return "older avg=" + getOlderAverage() + " nanos, newer avg=" + getNewerAverage() +
                " ms, avg diff=" + getAverageDiff() + " nanos, " +
                (double) Math.round(((double) getOlderAverage() / (double) getNewerAverage()) * 100.0) / 100.0 + " times";
    }

    public String getTotalReport() {
        return "older=" + getOlderTotal() + " nanos, newer=" + getNewerTotal() +
                " ms, diff=" + getTotalDiff() + " nanos, " +
                (double) Math.round(((double) getOlderTotal() / (double) getNewerTotal()) * 100.0) / 100.0 + " times";
    }
}
