package tk.bolovsrol.utils.containers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Имплементит основные методы самым очевидным способом.
 * .
 *
 * @param <V> класс значения
 */
public abstract class AbstractValueContainer<V> implements ValueContainer<V> {

    protected V value;
    protected V committedValue;

    @Override public V getValue() { return value; }

    @Override public void setValue(V value) { this.value = value; }

    @Override public void dropValue() { this.value = null; }

    @Override public V getCommittedValue() { return committedValue; }

    @Override public void valueCommitted() { this.committedValue = value; }

    @Override public void rollbackValue() { this.value = this.committedValue; }

    @Override public boolean isValueChanged() { return !Objects.equals(committedValue, value); }

    @Override public boolean isValueNull() { return value == null; }

    @Override public void copyValueFrom(ValueContainer<V> source) throws ClassCastException, ObjectCopyException {
        this.value = source.getValue();
        this.committedValue = source.getCommittedValue();
    }

    @SuppressWarnings("unchecked") private Class<V> getComponentTypeInternal(Class<?> cl) {
        // Здесь мы проворачиваем нехитрый трюк.
        // Данный класс абстрактный, а все наследники явно определяют тип его дженерика,
        // а этот тип и является искомым классом. Находим среди предков класса данный и возвращаем его дженерик-класс.
        // Мы рассчитываем, что женерик будет единственным в декларации, иначе мы ничего не найдём.
        if (cl == AbstractValueContainer.class) {
            return null;
        }
        Class<V> result = getComponentTypeInternal(cl.getSuperclass());
        if (result != null) {
            return result;
        }

        Type genericSuperclass = cl.getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType)) { return null; }

        Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
        if (actualTypeArguments.length != 1) { return null; }

        Type type = actualTypeArguments[0];
        if (!(type instanceof Class)) { return null; }

        return (Class<V>) type;
    }

    @SuppressWarnings("unchecked") @Override public Class<V> getComponentType() {
        return getComponentTypeInternal(getClass());
    }

    @Override public String toString() {
        if (Objects.equals(value, committedValue)) {
            return String.valueOf(value);
        } else if (committedValue == null) {
            return '→' + String.valueOf(value);
        } else {
            return String.valueOf(committedValue) + '→' + String.valueOf(value);
        }
    }
}
