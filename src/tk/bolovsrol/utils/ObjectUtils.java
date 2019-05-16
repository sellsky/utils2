package tk.bolovsrol.utils;

import tk.bolovsrol.utils.objectsize.ObjectSizeCalculator;

import java.util.Objects;

public final class ObjectUtils {
    private ObjectUtils() {
    }

    /**
     * Сравнивает два объекта. Они равны, если оба нул либо если o1 не нул и o1.equals(o2).
     *
     * @param o1
     * @param o2
     * @return true, если объекты равны, иначе false
     * @deprecated
     * @see Objects#equals(Object, Object)
     *
     */
    @Deprecated
    public static boolean equals(Object o1, Object o2) {
        return Objects.equals(o1, o2);
    }

    /**
     * Создаёт объект указанного класса.
     *
     * @param className спецификация класса
     * @param <T> ожидаемый класс объекта
     * @return объект класса
     * @throws UnexpectedBehaviourException создать объект не удалось.
     * @see #create(Class)
     */
    public static <T> T create(String className) throws UnexpectedBehaviourException {
        return create(ObjectUtils.<T>pickClass(className));
    }

    /**
     * Находит класс по указанному имени.
     * <p>
     * Если передан параметр нул, возвратит нул.
     *
     * @param className спецификация класса
     * @param <T> тип ожидаемого класса
     * @return класс
     * @throws UnexpectedBehaviourException
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> pickClass(String className) throws UnexpectedBehaviourException {
        if (className == null) {
            return null;
        }
        try {
            return (Class<T>) Class.forName(className);
        } catch (Exception e) {
            throw new UnexpectedBehaviourException("Cannot retrieve class by name " + Spell.get(className), e);
        }
    }

    /**
     * Создаёт объект указанного класса.
     * <p>
     * Если передан параметр нул, возвратит нул.
     *
     * @param type класс объекта
     * @param <T> ожидаемый класс
     * @return объект класса
     * @throws UnexpectedBehaviourException
     * @see #create(String)
     */
    public static <T> T create(Class<T> type) throws UnexpectedBehaviourException {
        if (type == null) {
            return null;
        }
        T result;
        try {
            result = type.getConstructor().newInstance();
        } catch (Exception e) {
            throw new UnexpectedBehaviourException("Cannot instantinate class " + Spell.get(type), e);
        }
        return result;
    }

    /**
     * Возвращает актуальный размер объекта в байтах.
     * <p>
     * Враппер для недокументированного класса, вычисляющего размер объекта, чтобы не потерять этот класс.
     *
     * @param o объект
     * @return размер объекта
     */
    public static long getObjectSize(Object o) {
        return ObjectSizeCalculator.getObjectSize(o);
    }

}
