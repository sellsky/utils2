package tk.bolovsrol.utils;

/**
 * Используется во всяких объектах в нумерациях, чтобы каждый раз заново не писать.
 * <p/>
 * По сути, дублирует {@link Boolean}, но, в отличие от него, является енумом.
 */
public enum Flag {
    YES(true),
    NO(false);

    private final boolean value;

    Flag(boolean value) {
        this.value = value;
    }

    /**
     * Возвращает подходящее булевое значение.
     * <ul>
     * <li>true → {@link Flag#YES},
     * <li>false → {@link Flag#NO}.
     * </ul>
     * <p/>
     * Возможно, лучше использовать выражение <code>flag == {@link Flag#YES}</code>.
     *
     * @return булевое значение флага
     */
    public boolean booleanValue() {
        return value;
    }

    /**
     * Подбирает соответствующий булевому значению флаг.
     * <ul>
     * <li>true → {@link Flag#YES},
     * <li>false → {@link Flag#NO}.
     * </ul>
     *
     * @param value булевое значение
     * @return соответствующий флаг
     */
    public static Flag pickBoolean(boolean value) {
        return value ? YES : NO;
    }

    /**
     * Подбирает соответствующий булевому объекту флаг.
     * <ul>
     * <li>null → null,
     * <li>{@link Boolean#TRUE} → {@link Flag#YES},
     * <li>{@link Boolean#FALSE} → {@link Flag#NO}.
     * </ul>
     *
     * @param bool булевый объект
     * @return соответствующий флаг
     */
    public static Flag pickBoolean(Boolean bool) {
        return bool == null ? null : bool.booleanValue() ? YES : NO;
    }

    /**
     * Возвращает соответствующий булевый объект.
     * <ul>
     * <li>null → null,
     * <li>{@link Flag#YES} → {@link Boolean#TRUE},
     * <li>{@link Flag#NO} → {@link Boolean#FALSE}.
     * </ul>
     *
     * @param flag интересующий флаг
     * @return соответствующий булевый тип
     */
    public static Boolean toBoolean(Flag flag) {
        return flag == null ? null : flag == YES ? Boolean.TRUE : Boolean.FALSE;
    }
}