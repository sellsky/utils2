package tk.bolovsrol.utils;

import java.util.function.Supplier;

public final class BooleanUtils {
    private BooleanUtils() {
    }

    /**
     * Выясняет наиболее вероятное булево значение строки.
     * <p/>
     * Если <code>source</code> нул, возвращается нул.
     * <p/>
     * Если в <code>source</code> первый символ один из
     * <code>1</code>, <code>T</code>, <code>t</code>, <code>Y</code>, <code>y</code>
     * — считаем значение истинным.
     * В остальных случаях — ложным.
     *
     * @param source исходная строка
     * @return булево значение строки или нул
     */
    public static Boolean parse(String source) {
		return parse(source, (Boolean) null);
	}

    /**
     * Выясняет наиболее вероятное булево значение строки.
     * <p/>
     * Если <code>source</code> нул, возвращается значение по умолчанию.
     * <p/>
     * Если в <code>source</code> первый символ один из
     * <code>1</code>, <code>T</code>, <code>t</code>, <code>Y</code>, <code>y</code>
     * — считаем значение истинным.
     * В остальных случаях — ложным.
     *
     * @param source исходная строка
     * @param defaultValue значение по умолчанию
     * @return булево значение строки или значение по умолчанию
     */
    public static Boolean parse(String source, Boolean defaultValue) {
        if (source == null) {
            return defaultValue;
        }
        if (source.length() == 0) {
            return Boolean.FALSE;
        }
        switch (source.charAt(0)) {
            case '1':
            case 'T':
            case 't':
            case 'Y':
            case 'y':
				return Boolean.TRUE;
		default:
			return Boolean.FALSE;
		}
	}

	/**
	 * Выясняет наиболее вероятное булево значение строки.
	 * <p>
	 * Если <code>source</code> нул, возвращается значение по умолчанию.
	 * <p>
	 * Если в <code>source</code> первый символ один из
	 * <code>1</code>, <code>T</code>, <code>t</code>, <code>Y</code>, <code>y</code>
	 * — считаем значение истинным.
	 * В остальных случаях — ложным.
	 *
	 * @param source исходная строка
	 * @param defaultValueSupplier генератор значения по умолчанию
	 * @return булево значение строки или значение по умолчанию
	 */
	public static Boolean parse(String source, Supplier<Boolean> defaultValueSupplier) {
		if (source == null) {
			return defaultValueSupplier.get();
		}
		if (source.length() == 0) {
			return Boolean.FALSE;
		}
		switch (source.charAt(0)) {
		case '1':
		case 'T':
		case 't':
		case 'Y':
		case 'y':
			return Boolean.TRUE;
		default:
			return Boolean.FALSE;
		}
	}

    /**
     * Выясняет наиболее вероятное булево значение строки.
     * <p/>
     * Если <code>source</code> нул, возвращается значение по умолчанию.
     * <p/>
     * Если в <code>source</code> первый символ один из
     * <code>1</code>, <code>T</code>, <code>t</code>, <code>Y</code>, <code>y</code>
     * — считаем значение истинным.
     * В остальных случаях — ложным.
     *
     * @param source исходная строка
     * @param defaultValue значение по умолчанию
     * @return булево значение строки или значение по умолчанию
     */
    public static boolean parseValue(String source, boolean defaultValue) {
        if (source == null) {
            return defaultValue;
        }
        if (source.length() == 0) {
            return false;
        }
        switch (source.charAt(0)) {
            case '1':
            case 'T':
            case 't':
            case 'Y':
            case 'y':
                return true;
            default:
                return false;
        }
    }
}
