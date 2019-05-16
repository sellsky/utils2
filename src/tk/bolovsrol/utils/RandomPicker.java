package tk.bolovsrol.utils;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

/** Выбирает случайный элемент с вероятностью, пропорциональной его весу. */
public class RandomPicker<O> {

    private Random random;

    private final NavigableMap<Integer, O> data = new TreeMap<>();

    /** Создаёт объект со стандартным генератором случайных чисел. */
    public RandomPicker() {
        this(new Random());
    }

    /** Создаёт объект с указанным генератором случайных чисел. */
    public RandomPicker(Random random) {
        this.random = random;
    }

    /**
     * Добавляет в словарь объект с указанным весом.
     *
     * @param o
     * @param weight
     */
    public void add(O o, int weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight cannot be less than 1.");
        }
        if (data.isEmpty()) {
            data.put(weight, o);
        } else {
            data.put(data.lastKey().intValue() + weight, o);
        }
    }

    /**
     * Добавляет в словарь объект с весом, равным 1.
     *
     * @param o
     */
    public void add(O o) {
        add(o, 1);
    }

    /**
     * Выбирает произвольный объект.
     *
     * @return объект
     */
    public O pick() {
        return data.higherEntry(random.nextInt(data.lastKey().intValue())).getValue();
    }
}
