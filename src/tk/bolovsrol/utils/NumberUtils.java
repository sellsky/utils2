package tk.bolovsrol.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/** Утилиты для нематематических преобразований Number-объектов. */
public final class NumberUtils {

    public static final Integer[] EMPTY_INTEGER_ARRAY = new Integer[0];
    public static final Long[] EMPTY_LONG_ARRAY = new Long[0];

    private static final ThreadLocal<DecimalFormat> DECIMAL_FORMAT_TL = ThreadLocal.withInitial(NumberUtils::newDecimalFormat);

    private NumberUtils() {
    }

    /**
     * Преобразует объект, хранящий числовое значение,
     * в строку с записью этого числа.
     * <p/>
     * А если объект null, то возвращает тоже null
     *
     * @param source
     * @return строка с записью числового значения или null
     */
    public static String getString(Number source) {
        return source == null ? null : DECIMAL_FORMAT_TL.get().format(source);
    }

    // ====== Парсинг.
    // ------ int

    public static int parseIntValue(String source) throws NumberFormatException {
        return Integer.parseInt(source.trim());
    }

    public static int parseIntValue(String source, int defaultValue) throws NumberFormatException {
        return source == null ? defaultValue : parseIntValue(source);
    }

    public static int[] parseIntValues(String[] strings) throws NumberFormatException {
        if (strings == null) {
            return null;
        }
        int[] values = new int[strings.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = parseIntValue(strings[i]);
        }
        return values;
    }


    // ------ long

    public static long parseLongValue(String source) throws NumberFormatException {
        return Long.parseLong(source.trim());
    }

    public static long parseLongValue(String source, long defaultValue) throws NumberFormatException {
        return source == null ? defaultValue : parseLongValue(source);
    }

    public static long[] parseLongValues(String[] strings) throws NumberFormatException {
        if (strings == null) {
            return null;
        }
        long[] values = new long[strings.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = parseLongValue(strings[i]);
        }
        return values;
    }


    // ------ Integer

    /**
     * Преобразует строку с записью числа в объект типа Integer,
     * хранящий соответствующее числовое значение.
     * <p/>
     * А если строка null или не число, то возвращает -1.
     *
     * @param source
     * @return объект с числовым представлением или null
     */
    public static Integer parseInteger(String source, int radix) throws NumberFormatException {
        return parseInteger(source, radix, null);
    }

    public static Integer parseInteger(String source) throws NumberFormatException {
        return parseInteger(source, 10, null);
    }

    public static Integer parseInteger(String source, int radix, Integer defaultValue) throws NumberFormatException {
        return source == null ? defaultValue : Integer.valueOf(source.trim(), radix);
    }

    public static Integer parseInteger(String source, Integer defaultValue) throws NumberFormatException {
        return parseInteger(source, 10, defaultValue);
    }

    public static Integer[] parseIntegers(String[] strings, int radix) throws NumberFormatException {
        if (strings == null) {
            return null;
        }
        Integer[] values = new Integer[strings.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = parseInteger(strings[i], radix);
        }
        return values;
    }

    public static Integer[] parseIntegers(String[] strings) throws NumberFormatException {
        return parseIntegers(strings, 10);
    }

    public static List<Integer> parseIntegers(Collection<String> strings) throws NumberFormatException {
        if (strings == null) {
            return null;
        }
        ArrayList<Integer> result = new ArrayList<>(strings.size());
        for (String string : strings) {
            result.add(parseInteger(string));
        }
        return result;
    }

    // ------ Long

    /**
     * Преобразует строку с записью числа в объект типа Long,
     * хранящий соответствующее числовое значение.
     * <p/>
     * А если переданная строка null, возвращает нул.
     * Если не число, выкидывает исключение.
     *
     * @param source
     * @return объект с числовым представлением или null
     */
    public static Long parseLong(String source) throws NumberFormatException {
        return parseLong(source, null);
    }

    /**
     * Преобразует строку с записью числа в объект типа Long,
     * хранящий соответствующее числовое значение.
     * <p>
     * А если переданная строка null, возвращает defaultValue.
     * Если не число, выкидывает исключение.
     *
     * @param source
     * @param defaultValue
     * @return объект с числовым представлением или null
     */
    public static Long parseLong(String source, Long defaultValue) throws NumberFormatException {
        return source == null ? defaultValue : Long.valueOf(source.trim());
    }

    public static Long[] parseLongs(String[] strings) throws NumberFormatException {
        if (strings == null) {
            return null;
        }
        Long[] values = new Long[strings.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = parseLong(strings[i]);
        }
        return values;
    }

    public static List<Long> parseLongs(Collection<String> strings) throws NumberFormatException {
        if (strings == null) {
            return null;
        }
        ArrayList<Long> result = new ArrayList<>(strings.size());
        for (String string : strings) {
            result.add(parseLong(string));
        }
        return result;
    }

    // ------ BigDecimal

    /** @return форматирователь для чисел типа BigDecimal, выкидывающий незначащие нули и с десятичным разделителем «.». */
    public static DecimalFormat newDecimalFormat() {
        DecimalFormat decimalFormat = new DecimalFormat();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        decimalFormat.setDecimalFormatSymbols(dfs);
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setMaximumFractionDigits(Integer.MAX_VALUE);
        decimalFormat.setMaximumIntegerDigits(Integer.MAX_VALUE);
        decimalFormat.setMinimumFractionDigits(0);
        decimalFormat.setMinimumIntegerDigits(1);
        return decimalFormat;
    }

    /**
     * Преобразует строку с записью числа в объект типа BigDecimal,
     * хранящий соответствующее числовое значение.
     * <p/>
     * А если строка null или не число, то возвращает -1.
     *
     * @param source
     * @return объект с числовым представлением или null
     */
    public static BigDecimal parseBigDecimal(String source) throws NumberFormatException {
        return parseBigDecimal(source, null);
    }

    /**
     * Преобразует строку с записью числа в объект типа BigDecimal,
     * хранящий соответствующее числовое значение.
     * <p/>
     * Предварительно заменяет в строке запятые на точки, выбрасывает «_», «`», «'» и пробелы,
     * <p>
     * А если строка null, возвращает defaultValue.
     * Если строка не число, выкидывает NumberFormatException.
     *
     * @param source
     * @param defaultValue значение на случай, если source нул
     * @return объект с числовым представлением или defaultValue
     */
    public static BigDecimal parseBigDecimal(String source, BigDecimal defaultValue) throws NumberFormatException {
        if (source == null) {
            return defaultValue;
        } else {
            int pos = 0;
            StringBuilder sb = null;
            char[] chars = source.toCharArray();
            while (pos < source.length()) {
                char ch = chars[pos];
                if (ch == ',') {
                    if (sb == null) { sb = new StringBuilder(source.substring(0, pos)); }
                    sb.append('.');
                } else if (ch == '_' || ch == '`' || ch == '\'' || Character.isWhitespace(ch)) {
                    if (sb == null) { sb = new StringBuilder(source.substring(0, pos)); }
                } else if (sb != null) {
                    sb.append(ch);
                }
                pos++;
            }
            return new BigDecimal(sb == null ? source : sb.toString());
        }
    }

    public static BigDecimal[] parseBigDecimals(String[] strings) throws NumberFormatException {
        if (strings == null) {
            return null;
        }
        BigDecimal[] values = new BigDecimal[strings.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = parseBigDecimal(strings[i]);
        }
        return values;
    }

    public static BigDecimal[] parseBigDecimals(Collection<String> strings) throws NumberFormatException {
        if (strings == null) {
            return null;
        }
        BigDecimal[] values = new BigDecimal[strings.size()];
        Iterator<String> it = strings.iterator();
        for (int i = 0; i < values.length; i++) {
            values[i] = parseBigDecimal(it.next());
        }
        return values;
    }


    // ====== Обёртки.
    // ------ int

    /**
     * Оборачивает в массиве int каждый элемент в объект Integer.
     *
     * @param ints
     * @return "обёрнутый" массив.
     */
    public static Integer[] wrapIntValues(int[] ints) {
        Integer[] result = new Integer[ints.length];
        for (int i = 0; i < ints.length; i++) {
            result[i] = ints[i];
        }
        return result;
    }

    /**
     * Преобразует коллекцию врапперов в массив примитивов типа int.
     *
     * @param numbers коллекция врапперов Number
     * @return массив примитивов
     */
    public static int[] unwrapIntValues(Collection<? extends Number> numbers) {
        int counter = 0;
        int[] result = new int[numbers.size()];
        for (Number number : numbers) {
            result[counter] = number.intValue();
            counter++;
        }
        return result;
    }

    /**
     * Разворачивает массив Integer в массив int
     *
     * @param ints
     * @return "развёрнутый" массив.
     */
    public static int[] unwrapIntValues(Number[] ints) {
        int[] result = new int[ints.length];
        for (int i = 0; i < ints.length; i++) {
            result[i] = ints[i].intValue();
        }
        return result;
    }

    /**
     * Оборачивает в массиве long каждый элемент в объект Long.
     *
     * @param longs
     * @return "обёрнутый" массив.
     */
    public static Long[] wrapLongValues(long[] longs) {
        Long[] result = new Long[longs.length];
        for (int i = 0; i < longs.length; i++) {
            result[i] = longs[i];
        }
        return result;
    }

    /**
     * Преобразует коллекцию врапперов в массив примитивов типа long.
     *
     * @param numbers коллекция врапперов Number
     * @return массив примитивов
     */
    public static long[] unwrapLongValues(Collection<? extends Number> numbers) {
        int counter = 0;
        long[] result = new long[numbers.size()];
        for (Number number : numbers) {
            result[counter] = number.longValue();
            counter++;
        }
        return result;
    }

    /**
     * Разворачивает массив Long в массив long
     *
     * @param longs
     * @return "развёрнутый" массив
     */
    public static long[] unwrapLongValues(Number[] longs) {
        long[] result = new long[longs.length];
        for (int i = 0; i < longs.length; i++) {
            result[i] = longs[i].longValue();
        }
        return result;
    }


    // ====== Распечатыватели

    /**
     * Из массива {@code intValues} составляет строку, в которой элементы массива
     * перечислены через {@code delimiter}.
     * <p/>
     * Если массив null, то возвращается {@code nullString}.
     * <p/>
     * Если массив пуст, то возвращается {@code emptyString}.
     *
     * @param intValues   массив для перечисления
     * @param delimiter   разделитель
     * @param nullString  строка на случай, если массив null
     * @param emptyString строка на случай, если массив пуст
     * @return перечисление
     */
    public static String enlistIntValues(int[] intValues, String delimiter, String nullString, String emptyString) {
        if (intValues == null) {
            return nullString;
        }
        if (intValues.length == 0) {
            return emptyString;
        }
        StringDumpBuilder sb = new StringDumpBuilder(delimiter);
        for (int i : intValues) {
            sb.append(String.valueOf(i));
        }
        return sb.toString();
    }

    /**
     * Из массива {@code intValues} составляет строку, в которой элементы массива
     * перечислены через {@code delimiter}.
     * <p/>
     * Если массив null, то возвращается null.
     * <p/>
     * Если массив пуст, то возвращается пустая строка.
     *
     * @param intValues массив для перечисления
     * @param delimiter разделитель
     * @return перечисление
     */
    public static String enlistIntValues(int[] intValues, String delimiter) {
        return enlistIntValues(intValues, delimiter, null, "");
    }

    /**
     * Из массива {@code longValues} составляет строку, в которой элементы массива
     * перечислены через {@code delimiter}.
     * <p/>
     * Если массив null, то возвращается {@code nullString}.
     * <p/>
     * Если массив пуст, то возвращается {@code emptyString}.
     *
     * @param longValues  массив для перечисления
     * @param delimiter   разделитель
     * @param nullString  строка на случай, если массив null
     * @param emptyString строка на случай, если массив пуст
     * @return перечисление
     */
    public static String enlistLongValues(long[] longValues, String delimiter, String nullString, String emptyString) {
        if (longValues == null) {
            return nullString;
        }
        if (longValues.length == 0) {
            return emptyString;
        }
        StringDumpBuilder sb = new StringDumpBuilder(delimiter);
        for (long i : longValues) {
            sb.append(String.valueOf(i));
        }
        return sb.toString();
    }

    /**
     * Из массива {@code longValues} составляет строку, в которой элементы массива
     * перечислены через {@code delimiter}.
     * <p/>
     * Если массив null, то возвращается null.
     * <p/>
     * Если массив пуст, то возвращается пустая строка.
     *
     * @param longValues массив для перечисления
     * @param delimiter  разделитель
     * @return перечисление
     */
    public static String enlistLongValues(long[] longValues, String delimiter) {
        return enlistLongValues(longValues, delimiter, null, "");
    }

    /**
     * Из массива {@code numbers} составляет строку, в которой элементы массива
     * перечислены через {@code delimiter}.
     * <p/>
     * Если массив null, то возвращается {@code nullString}.
     * <p/>
     * Если массив пуст, то возвращается {@code emptyString}.
     *
     * @param numbers     массив для перечисления
     * @param delimiter   разделитель
     * @param nullString  строка на случай, если массив null
     * @param emptyString строка на случай, если массив пуст
     * @return перечисление
     */
    public static String enlistNumbers(Number[] numbers, String delimiter, String nullString, String emptyString) {
        if (numbers == null) {
            return nullString;
        }
        if (numbers.length == 0) {
            return emptyString;
        }
        StringDumpBuilder sb = new StringDumpBuilder(delimiter);
        for (Number number : numbers) {
            sb.append(DECIMAL_FORMAT_TL.get().format(number));
        }
        return sb.toString();
    }

    /**
     * Из массива {@code numbers} составляет строку, в которой элементы массива
     * перечислены через {@code delimiter}.
     * <p/>
     * Если массив null, то возвращается null.
     * <p/>
     * Если массив пуст, то возвращается пустая строка.
     *
     * @param numbers   массив для перечисления
     * @param delimiter разделитель
     * @return перечисление
     */
    public static String enlistNumbers(Number[] numbers, String delimiter) {
        return enlistNumbers(numbers, delimiter, null, "");
    }

    /**
     * Из коллекции {@code numbers} составляет строку, в которой элементы коллекции
     * перечислены через {@code delimiter}.
     * <p/>
     * Если коллекция null, то возвращается {@code nullString}.
     * <p/>
     * Если коллекция пуста, то возвращается {@code emptyString}.
     *
     * @param numbers     коллекция для перечисления
     * @param delimiter   разделитель
     * @param nullString  строка на случай, если коллекция null
     * @param emptyString строка на случай, если коллекция пуста
     * @return перечисление
     */
    public static String enlistNumbers(Collection<? extends Number> numbers, String delimiter, String nullString, String emptyString) {
        if (numbers == null) {
            return nullString;
        }
        if (numbers.isEmpty()) {
            return emptyString;
        }
        StringDumpBuilder sb = new StringDumpBuilder(delimiter);
        for (Number number : numbers) {
            sb.append(DECIMAL_FORMAT_TL.get().format(number));
        }
        return sb.toString();
    }

    /**
     * Из коллекции {@code numbers} составляет строку, в которой элементы коллекции
     * перечислены через {@code delimiter}.
     * <p/>
     * Если коллекция null, то возвращается null.
     * <p/>
     * Если коллекция пуста, то возвращается пустая строка.
     *
     * @param numbers   коллекция для перечисления
     * @param delimiter разделитель
     * @return перечисление
     */
    public static String enlistNumbers(Collection<? extends Number> numbers, String delimiter) {
        return enlistNumbers(numbers, delimiter, null, "");
    }


    // ====== Фильтрователи

    /**
     * Возвращает значение, помещающееся в пределы [{@code notLessThan}, {@code lessThan}), а иначе null.
     * <p/>
     * Граница диапазона может быть null, в таком случае эта граница не проверяется.
     *
     * @param source
     * @param notLessThan
     * @param lessThan
     * @return
     */
    public static Integer retainInRange(Integer source, Integer notLessThan, Integer lessThan) {
        return retainInRange(source, notLessThan, lessThan, null);
    }

    /**
     * Возвращает значение, помещающееся в диапазон [{@code notLessThan}, {@code lessThan}),
     * а иначе {@code elseValue}.
     * <p/>
     * Граница диапазона может быть null, в таком случае эта граница не проверяется.
     *
     * @param source
     * @param notLessThan
     * @param lessThan
     * @param elseValue
     * @return
     */
    public static Integer retainInRange(Integer source, Integer notLessThan, Integer lessThan, Integer elseValue) {
        if (source == null) {
            return elseValue;
        }
        if (notLessThan != null && source.intValue() < notLessThan.intValue()) {
            return elseValue;
        }
        if (lessThan != null && source.intValue() >= lessThan.intValue()) {
            return elseValue;
        }
        return source;
    }

    public static Integer retainPositive(Integer source, Integer elseValue) {
        return retainInRange(source, 1, null, elseValue);
    }

    public static Integer retainPositive(Integer source) {
        return retainInRange(source, 1, null, null);
    }

    public static Integer retainNonNegative(Integer source, Integer elseValue) {
        return retainInRange(source, 0, null, elseValue);
    }

    public static Integer retainNonNegative(Integer source) {
        return retainInRange(source, 0, null, null);
    }

    /**
     * Проверяет равенство двух чисел.
     * <p/>
     * Числа равны, если оба null либо num1.equals(num2).
     *
     * @param num1
     * @param num2
     * @return true, если равны, иначе false
     */
    public static boolean equals(Number num1, Number num2) {
        return num1 == null ? num2 == null : num2 != null && num1.equals(num2);
    }

	/** Проверяет, есть ли в строке указанное число ограниченное любыми нецифровыми символами.
	 * @param source — исходная строка, в которой выполняется поиск числа;
	 * @param target — число, которое ищется в исходной строке;
	 * @return true, если source и target не null, а также в source есть число target. */
	public static boolean containsNumber(@Nullable String source, @Nullable Long target) {
		return source != null && target != null
				&& source.matches("(?:^|.*\\D)" + target + "(?:$|\\D.*)"); }
}


