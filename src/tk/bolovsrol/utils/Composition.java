package tk.bolovsrol.utils;

import java.util.Map;

/**
 * Композиция нескольких объектов.
 * <p/>
 * Удобно использовать в качестве составного ключика для {@link Map}.
 * <p/>
 * Лучше сделать наследника, в котором определить конструктор
 * с явно заданным перечнем элементов, а также при необходимости
 * сделать геттеры/сеттеры.
 * <p/>
 * Композиция будет {@link Comparable}, если все элементы Comparable,
 * иначе при попытке сравнения вывалится {@link ClassCastException}.
 * Сравнение считает, что null-элементы меньше любых значений,
 * а также считает, что соответствующие null-элементы равны.
 */
public class Composition implements Comparable<Composition> {

    protected final Object[] items;

    public Composition(Object... items) {
        this.items = items;
    }

    /**
     * Возвращает элемент в указанной позиции.
     *
     * @param index интересующая позиция
     * @return содержимое позиции
     * @throws ArrayIndexOutOfBoundsException позиции не существует
     */
    protected Object get(int index) throws ArrayIndexOutOfBoundsException {
        if (0 > index || index >= items.length) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return items[index];
    }

    /**
     * Устанавливает значение указанной поизции.
     *
     * @param index интересующая позиция
     * @return прежнее содержимое позиции
     * @throws ArrayIndexOutOfBoundsException позиции не существует
     */
    protected Object set(int index, Object object) {
        if (0 > index || index >= items.length) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        Object previous;
        synchronized (items) {
            previous = items[index];
            items[index] = object;
        }
        return previous;
    }

    public int size() {
        return items.length;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (Object item : items) {
            result = (result << 5) - result;
            if (item != null) {
                result += item.hashCode();
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Composition && equals((Composition) obj));
    }

    public boolean equals(Composition that) {
        if (this.items == that.items) {
            return true;
        }
        if (this.items.length != that.items.length) {
            return false;
        }
        for (int i = 0; i < items.length; i++) {
            Object thisItem = this.items[i];
            if (thisItem == null) {
                if (that.items[i] != null) {
                    return false;
                }
            } else {
                if (!thisItem.equals(that.items[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringDumpBuilder sb = new StringDumpBuilder();
        for (Object item : items) {
            sb.append(Spell.get(item));
        }
        return sb.toString();
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public int compareTo(Composition that) {
        if (that == this) {
            return 0;
        }
        int i = 0;
        while (true) {
            if (this.items.length == i) {
                return this.items.length == that.items.length ? 0 : -1;
            }
            if (that.items.length == i) {
                return 1;
            }
            Object thisItem = this.items[i];
            Object thatItem = that.items[i];
            if (thisItem == null) {
                if (thatItem != null) {
                    return -1;
                }
            } else {
                if (thatItem == null) {
                    return 1;
                }
                int itemCompared = ((Comparable<Object>) thisItem).compareTo(thatItem);
                if (itemCompared != 0) {
                    return itemCompared;
                }
            }
            i++;
        }
    }
}
