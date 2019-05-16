package tk.bolovsrol.utils;

import tk.bolovsrol.utils.io.LineInputStream;
import tk.bolovsrol.utils.io.LineOutputStream;
import tk.bolovsrol.utils.store.Storeable;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Последовательность, возвращающая последовательно увеличивающиеся числа >0L,
 * умеющая в {@link Storeable}.
 * <p/>
 * После переполнения, возвратив 2<sup>63</sup>-2 значений, снова считает с 1.
 */
public class Sequence implements Storeable {

    private final AtomicLong lastIdAl = new AtomicLong(0);

    /**
     * Возвращает следующее значение последовательности.
     *
     * @return
     */
    public long next() {
        long val;
        do {
            // мы хотим только неотрицательные значения
            val = lastIdAl.incrementAndGet() & Long.MAX_VALUE;
        } while (val == 0L);
        return val;
    }

    @Override public void store(LineOutputStream los) throws Exception {
        los.write(Long.toString(lastIdAl.get() & Long.MAX_VALUE));
    }

    @Override public void restore(LineInputStream lis) throws Exception {
        lastIdAl.set(Long.parseLong(lis.readLine()));
    }
}
