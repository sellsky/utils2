package tk.bolovsrol.utils.benchmark;

/**
 * Повторятель. Умеет повторять iteration заданное количество раз
 * и рассказывать, сколько времени это заняло.
 * <p/>
 * Нужно понаследовать этот класс и определить в методе {@link #iteration()}
 * интересующее действие.
 *
 * @see Comparer
 */
public abstract class Repeater {

    /** Выполняет действие, скорость работы которого предполагается замерять. */
    public abstract void iteration();

    public long run(int count) {
        long started = System.nanoTime();
        for (int i = 0; i < count; i++) {
            iteration();
        }
        return System.nanoTime() - started;
    }
}
