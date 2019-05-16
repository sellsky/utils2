package tk.bolovsrol.utils;

import tk.bolovsrol.utils.properties.ReadOnlyProperties;
import tk.bolovsrol.utils.textformatter.compiling.InvalidTemplateException;
import tk.bolovsrol.utils.textformatter.compiling.TextFormatCompiler;
import tk.bolovsrol.utils.textformatter.compiling.evaluators.ArrayEvaluator;
import tk.bolovsrol.utils.textformatter.compiling.evaluators.ReadOnlySourceEvaluator;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Свалка инструментов для преобразования строк.
 */
public final class StringUtils {

    private StringUtils() {
    }

    public static final char[] DEFAULT_DELIMITERS = {',', ';'};
    public static final char[] CR_LF = {'\n', '\r'};
    public static final char DEFAULT_MASK = (char) -1;
    public static final char[] DEFAULT_QUOTES = null;
    public static final char[] QUOTES = {'\"', '\''};
    public static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * пустая строка просто для красоты
     */
    public static final String EMPTY_STRING = "";
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static final Map<Character, String> TRANSLITERATIONS;
    public static final Map<Character, String> NORMALIZATIONS;

    static {
        Map<Character, String> transliterations = new TreeMap<>();
        transliterations.put('№', "No");
        transliterations.put('«', "\"");
        transliterations.put('»', "\"");
        transliterations.put('А', "A");
        transliterations.put('Б', "B");
        transliterations.put('В', "V");
        transliterations.put('Г', "G");
        transliterations.put('Д', "D");
        transliterations.put('Е', "E");
        transliterations.put('Ё', "E");
        transliterations.put('Ж', "Zh");
        transliterations.put('З', "Z");
        transliterations.put('И', "I");
        transliterations.put('Й', "J");
        transliterations.put('К', "K");
        transliterations.put('Л', "L");
        transliterations.put('М', "M");
        transliterations.put('Н', "N");
        transliterations.put('О', "O");
        transliterations.put('П', "P");
        transliterations.put('Р', "R");
        transliterations.put('С', "S");
        transliterations.put('Т', "T");
        transliterations.put('У', "U");
        transliterations.put('Ф', "F");
        transliterations.put('Х', "H");
        transliterations.put('Ц', "C");
        transliterations.put('Ч', "Ch");
        transliterations.put('Ш', "Sh");
        transliterations.put('Щ', "Sch");
        transliterations.put('Ъ', "'");
        transliterations.put('Ы', "Y");
        transliterations.put('Ь', "'");
        transliterations.put('Э', "E");
        transliterations.put('Ю', "Yu");
        transliterations.put('Я', "Ya");
        transliterations.put('а', "a");
        transliterations.put('б', "b");
        transliterations.put('в', "v");
        transliterations.put('г', "g");
        transliterations.put('д', "d");
        transliterations.put('е', "e");
        transliterations.put('ё', "e");
        transliterations.put('ж', "zh");
        transliterations.put('з', "z");
        transliterations.put('и', "i");
        transliterations.put('й', "j");
        transliterations.put('к', "k");
        transliterations.put('л', "l");
        transliterations.put('м', "m");
        transliterations.put('н', "n");
        transliterations.put('о', "o");
        transliterations.put('п', "p");
        transliterations.put('р', "r");
        transliterations.put('с', "s");
        transliterations.put('т', "t");
        transliterations.put('у', "u");
        transliterations.put('ф', "f");
        transliterations.put('х', "h");
        transliterations.put('ц', "c");
        transliterations.put('ч', "ch");
        transliterations.put('ш', "sh");
        transliterations.put('щ', "sch");
        transliterations.put('ъ', "'");
        transliterations.put('ы', "y");
        transliterations.put('ь', "'");
        transliterations.put('э', "e");
        transliterations.put('ю', "yu");
        transliterations.put('я', "ya");
        TRANSLITERATIONS = Collections.unmodifiableMap(transliterations);

        Map<Character, String> normalizations = new TreeMap<>();
        normalizations.putAll(transliterations);
        normalizations.put('А', "A");
        normalizations.put('В', "B");
        normalizations.put('Е', "E");
        normalizations.put('И', "U");
        normalizations.put('К', "K");
        normalizations.put('М', "M");
        normalizations.put('Н', "H");
        normalizations.put('О', "O");
        normalizations.put('Р', "P");
        normalizations.put('С', "C");
        normalizations.put('Т', "T");
        normalizations.put('У', "Y");
        normalizations.put('Х', "X");
        normalizations.put('а', "a");
        normalizations.put('в', "b");
        normalizations.put('е', "e");
        normalizations.put('и', "u");
        normalizations.put('к', "k");
        normalizations.put('м', "m");
        normalizations.put('н', "h");
        normalizations.put('о', "o");
        normalizations.put('р', "p");
        normalizations.put('с', "c");
        normalizations.put('т', "t");
        normalizations.put('у', "y");
        normalizations.put('х', "x");
        NORMALIZATIONS = Collections.unmodifiableMap(normalizations);
    }

    /**
     * Режим работы с не-ascii-символами.
     */
    public enum NonAsciiChars {
        /**
         * Удалить.
         */
        KEEP {
            @Override
            void append(StringBuilder target, char ch) {
                target.append(ch);
            }
        },
        /**
         * Удалить.
         */
        REMOVE {
            @Override
            void append(StringBuilder target, char ch) {
            }
        },
        /**
         * Заменить на «?».
         */
        MASK {
            @Override
            void append(StringBuilder target, char ch) {
                target.append('?');
            }
        };

        abstract void append(StringBuilder target, char ch);
    }

    /**
     * Транслирует исходную строку посимвольно, заменяя каждый символ на соответствие,
     * задаваемое картой. В случае отсутствия соответствия оставляет символ как есть.
     * <p>
     * Также может откусывать не-ascii-символы, отсутствующие в карте.
     *
     * @param source        исходная строка
     * @param map           карта замен
     * @param nonAsciiChars режим работы с не-ascii-символами
     * @return оттранслированная строка.
     * @see #normalize(String)
     * @see #transliterate(String)
     */
    public static String translateChars(String source, Map<Character, String> map, NonAsciiChars nonAsciiChars) {
        if (source == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(source.length() * 3);
        for (char ch : source.toCharArray()) {
            String s = map.get(ch);
            if (s != null) {
                sb.append(s);
            } else if ((int) ch >= 32 && (int) ch < 128) {
                sb.append(ch);
            } else {
                nonAsciiChars.append(sb, ch);
            }
        }
        return sb.toString();
    }

    /**
     * Преобразует строку так, чтобы она оказалась набранной латиницей заглавными буквами,
     * а русские буквы стали похожими по начертанию латинскими.
     *
     * @param source исходная строка
     * @return нормализованная строка
     */
    public static String normalize(String source) {
        return translateChars(source, NORMALIZATIONS, NonAsciiChars.REMOVE).toUpperCase();
    }

    /**
     * Преобразует строку так, чтобы она оказалась набранной латиницей.
     *
     * @param source исходная строка
     * @return нормализованная строка
     */
    public static String transliterate(String source) {
        return translateChars(source, TRANSLITERATIONS, NonAsciiChars.REMOVE);
    }

    /**
     * Вместо переводов строки ставит соответствующие значки
     *
     * @param string
     * @return "плоская" строка
     * @see #deflatten(String)
     */
    public static String flatten(String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }
        int length = string.length();
        for (int i = 0; i < length; i++) {
            char ch = string.charAt(i);
            switch (ch) {
                case '\n':
                case '\r':
                case '\t':
                case '\0':
                case '\b':
                case '\f':
                    return flattenHeavy(string, i, length);
            }
        }
        return string;
    }

    /**
     * Сюда попадают строки, в которых точно есть символы для замены.
     * Причём нам уже показывают на символ с наибольшим индексом.
     *
     * @param str
     * @return
     */
    private static String flattenHeavy(String str, int i, int length) {
        StringBuilder sb = new StringBuilder(str.length() * 2).append(str.substring(0, i));
        while (i < length) {
            char ch = str.charAt(i);
            switch (ch) {
                case '\n':
                    sb.append('\\').append('n');
                    break;
                case '\r':
                    sb.append('\\').append('r');
                    break;
                case '\t':
                    sb.append('\\').append('t');
                    break;
                case '\0':
                    sb.append('\\').append('0');
                    break;
                case '\b':
                    sb.append('\\').append('b');
                    break;
                case '\f':
                    sb.append('\\').append('f');
                    break;
                default:
                    sb.append(ch);
            }
            i++;
        }
        return sb.toString();
    }

    /**
     * Вместо значков перевода строки и табуляции ставит соответствующие символы.
     *
     * @param str "плоская" строка
     * @return строка
     * @see #flatten(String)
     */
    public static String deflatten(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        int pos = str.indexOf((int) '\\');
        if (pos < 0) {
            return str;
        }

        // do the heavy business
        StringBuilder sb = new StringBuilder(str.length());
        int lengthLessOne = str.length() - 1;
        int lastPos = 0;
        while (true) {
            if (pos < 0 || pos >= lengthLessOne) {
                sb.append(str.substring(lastPos));
                return sb.toString();
            } else {
                sb.append(str.substring(lastPos, pos));
            }

            pos++;
            char ch = str.charAt(pos);
            switch (ch) {
                case 'n':
                    sb.append('\n');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case '0':
                    sb.append('\0');
                    break;
                case 'b':
                    sb.append('\b');
                    break;
                case 'f':
                    sb.append('\f');
                    break;
                default:
                    sb.append(ch);
            }
            lastPos = pos + 1;
            pos = str.indexOf((int) '\\', lastPos);
        }
    }

    // ---- всякие дебагового толка конструкции

    /**
     * Согласует падеж текста с заданным числом
     *
     * @param number - число, с которым надо согласовать текст
     * @param one    - окончание, согласованное с количеством "один" (например: "очко")
     * @param two    - окончание, согласованное с кол-вом "два" ("очка")
     * @param five   - окончание, согласованное с кол-вом "пять" ("очков")
     * @return окончание, подходящее под number
     */
    public static String getNumericCase(int number, String one, String two, String five) {
        if (number < 0) {
            number = 0 - number;
        }
        number %= 100;
        if (number >= 5 && number <= 20) { // отдельно обрабатываем "-надцать"
            return five;
        }
        number %= 10;
        if (number == 1) {
            return one;
        }
        if (number >= 2 && number <= 4) {
            return two;
        }
        return five;
    }

    // -------- Преобразования массивов и коллекций строк

    /**
     * Перечисляет содержимое объектов коллекции <tt>collection</tt>
     * через разделитель <tt>delimiter</tt>. Если коллекция null, вернёт null;
     * если коллекция пуста, вернёт пустую строку. Если элемент коллекции нул,
     * он будет заменён на «(null)».
     *
     * @param collection коллекция, которую надо перечислить
     * @param delimiter  разделитель
     * @return строку-перечисление
     */
    public static String enlistCollection(Collection<?> collection, String delimiter) {
        return enlistCollection(collection, delimiter, delimiter);
    }

    /**
     * Перечисляет содержимое объектов коллекции <tt>collection</tt>
     * через разделитель <tt>delimiter</tt>, кроме последнего элемента, перед которым
     * ставит разделитель <tt>lastDelimiter</tt>. Если коллекция null, вернёт null;
     * если коллекция пуста, вернёт пустую строку. Если элемент коллекции нул,
     * он будет заменён на «(null)».
     *
     * @param collection    коллекция, которую надо перечислить
     * @param delimiter     разделитель между элементами от первого до предпоследнего, если есть
     * @param lastDelimiter разделитель перед последним элементом
     * @return строку-перечисление
     */
    public static String enlistCollection(Collection<?> collection, String delimiter, String lastDelimiter) {
        return enlistCollection(collection, delimiter, lastDelimiter, "(null)");
    }

    /**
     * Перечисляет содержимое объектов коллекции <tt>collection</tt>
     * через разделитель <tt>delimiter</tt>, кроме последнего элемента, перед которым
     * ставит разделитель <tt>lastDelimiter</tt>. Если коллекция null, вернёт null;
     * если коллекция пуста, вернёт пустую строку. Если элемент коллекции нул,
     * он будет заменён на <tt>nullMask</tt>.
     *
     * @param collection    коллекция, которую надо перечислить
     * @param delimiter     разделитель между элементами от первого до предпоследнего, если есть
     * @param lastDelimiter разделитель перед последним элементом
     * @param nullMask      замена нул-значениям
     * @return строку-перечисление
     */
    public static String enlistCollection(Collection<?> collection, String delimiter, String lastDelimiter, String nullMask) {
        if (collection == null) {
            return null;
        } else if (collection.isEmpty()) {
            return EMPTY_STRING;
        } else if (collection.size() == 1) {
            return valueOf(collection.iterator().next(), nullMask);
        }
        StringBuilder sb = new StringBuilder(256);
        Iterator<?> iterator = collection.iterator();
        sb.append(valueOf(iterator.next(), nullMask));
        while (true) {
            Object o = iterator.next();
            if (iterator.hasNext()) {
                sb.append(delimiter).append(valueOf(o, nullMask));
            } else {
                sb.append(lastDelimiter).append(valueOf(o, nullMask));
                return sb.toString();
            }
        }
    }

    /**
     * Перечисляет содержимое объектов массива <tt>strings</tt>
     * через разделитель <tt>delimiter</tt>. Если массив null, вернёт null;
     * если массив пуст, вернёт пустую строку. Если элемент массива нул,
     * он будет заменён на «(null)».
     *
     * @param strings   массив, который надо перечислить
     * @param delimiter разделитель
     * @return строку-перечисление
     */
    public static String enlistArray(Object[] strings, String delimiter) {
        return enlistArray(strings, delimiter, delimiter);
    }

    /**
     * Перечисляет содержимое объектов массива <tt>strings</tt>
     * через разделитель <tt>delimiter</tt>, кроме последнего элемента, перед которым
     * ставит разделитель <tt>lastDelimiter</tt>. Если массив null, вернёт null;
     * если массив пуст, вернёт пустую строку. Если элемент массива нул,
     * он будет заменён на «(null)».
     *
     * @param strings       массив, который надо перечислить
     * @param delimiter     разделитель между элементами от первого до предпоследнего, если есть
     * @param lastDelimiter разделитель перед последним элементом
     * @return строку-перечисление
     */
    public static String enlistArray(Object[] strings, String delimiter, String lastDelimiter) {
        return enlistArray(strings, delimiter, lastDelimiter, "(null)");
    }

    /**
     * /**
     * Перечисляет содержимое объектов массива <tt>strings</tt>
     * через разделитель <tt>delimiter</tt>, кроме последнего элемента, перед которым
     * ставит разделитель <tt>lastDelimiter</tt>. Если массив null, вернёт null;
     * если массив пуст, вернёт пустую строку. Если элемент массива нул,
     * он будет заменён на <tt>nullMask</tt>.
     *
     * @param strings       массив, который надо перечислить
     * @param delimiter     разделитель между элементами от первого до предпоследнего, если есть
     * @param lastDelimiter разделитель перед последним элементом
     * @param nullMask      замена нул-значениям
     * @return строку-перечисление
     */
    public static String enlistArray(Object[] strings, String delimiter, String lastDelimiter, String nullMask) {
        if (strings == null) {
            return null;
        } else if (strings.length == 0) {
            return EMPTY_STRING;
        } else if (strings.length == 1) {
            return valueOf(strings[0], nullMask);
        }
        StringBuilder sb = new StringBuilder(256);
        sb.append(valueOf(strings[0], nullMask));
        for (int i = 1; i < strings.length - 1; i++) {
            sb.append(delimiter).append(valueOf(strings[i], nullMask));
        }
        sb.append(lastDelimiter).append(valueOf(strings[strings.length - 1], nullMask));
        return sb.toString();
    }

    /**
     * Делит строку по {@link #DEFAULT_DELIMITERS запятым или точкам с запятой}
     * и возвращает массив полученных подстрок.
     * <p>
     * Если исходная строка null, вернётся null.
     * В других случаях вернётся массив как минимум из одной строки.
     *
     * @param source исходная строка
     * @return составные части исходной строки
     */
    public static String[] parseDelimited(String source) {
        return parseDelimited(source, DEFAULT_DELIMITERS, DEFAULT_MASK, DEFAULT_QUOTES);
    }

    /**
     * Делит строку по заданным символам-разделителям
     * и возвращает массив полученных подстрок.
     * <p>
     * Если исходная строка null, вернётся null.
     * В других случаях вернётся массив как минимум из одной строки.
     *
     * @param source     исходная строка
     * @param delimiters разделители
     * @return составные части исходной строки
     * @see #parseDelimited(String, char[], char, char[])
     */
    public static String[] parseDelimited(String source, char[] delimiters) {
        return parseDelimited(source, delimiters, DEFAULT_MASK, DEFAULT_QUOTES);
    }

    /**
     * Делит строку по заданным символам-разделителям
     * и возвращает массив полученных подстрок.
     * <p>
     * Если исходная строка null, вернётся null.
     * В других случаях вернётся массив как минимум из одной строки.
     *
     * @param source    исходная строка
     * @param delimiter разделители
     * @return составные части исходной строки
     * @see #parseDelimited(String, char[], char, char[])
     */
    public static String[] parseDelimited(String source, char delimiter) {
        return parseDelimited(source, new char[]{delimiter}, DEFAULT_MASK, DEFAULT_QUOTES);
    }

    /**
     * Делит строку по заданным символам-разделителям
     * и возвращает массив полученных подстрок.
     * <p>
     * Если исходная строка null, вернётся null.
     * В других случаях вернётся массив как минимум из одной строки.
     * <p>
     * Символы можно маскировать: любой символ после mask
     * воспринимается как часть строки, а не как разделитель.
     *
     * @param source    исходная строка
     * @param delimiter разделитель
     * @param mask      маскировочный символ
     * @param quotes
     * @return составные части исходной строки
     * @see #parseDelimited(String, char[])
     */
    public static String[] parseDelimited(String source, char delimiter, char mask, char[] quotes) {
        return parseDelimited(source, new char[]{delimiter}, mask, quotes);
    }

    /**
     * Делит строку по заданным символам-разделителям
     * и возвращает массив полученных подстрок.
     * <p>
     * Если исходная строка null, вернётся null.
     * В других случаях вернётся массив как минимум из одной строки.
     * <p>
     * Символы можно маскировать: любой символ после mask
     * воспринимается как часть строки, а не как разделитель.
     *
     * @param source       исходная строка
     * @param delimiters   разделители
     * @param mask         маскировочный символ
     * @param quotesOrNull
     * @return составные части исходной строки
     * @see #parseDelimited(String, char[])
     */
    public static String[] parseDelimited(String source, char[] delimiters, char mask, char[] quotesOrNull) {
        if (source == null) {
            return null;
        }
        List<String> result = new ArrayList<>(32);

        boolean delim = false;
        boolean masked = false;
        boolean quoted = false;
        char currentQuote = '\0';
        char[] chars = source.toCharArray();
        int pos = 0;
        StringBuilder sb = new StringBuilder(source.length());
        int len = source.length();
        outer:
        while (pos < len) {
            char ch = chars[pos];
            pos++;
            if (masked) {
                sb.append(ch);
                masked = false;
            } else if (ch == mask) {
                masked = true;
            } else if (quoted) {
                if (ch == currentQuote) {
                    quoted = false;
                } else {
                    sb.append(ch);
                }
            } else {
                if (quotesOrNull != null) {
                    for (char quote : quotesOrNull) {
                        if (ch == quote) {
                            quoted = true;
                            currentQuote = quote;
                            continue outer;
                        }
                    }
                }
                for (char delimiter : delimiters) {
                    if (ch == delimiter) {
                        result.add(sb.toString().trim());
                        sb.setLength(0);
                        delim = true;
                        continue outer;
                    }
                }
                delim = false;
                sb.append(ch);
            }
        }
        if (sb.length() > 0 || delim) {
            result.add(sb.toString().trim());
        }
        return result.toArray(new String[result.size()]);
    }
//    public static String[] parseDelimited(String source, char[] delimiters, char mask, char[] quotes) {
//        if (source == null) {
//            return null;
//        }
//
//        char[] chars = source.toCharArray();
//
//        ArrayList<String> al = new ArrayList<String>();
//        StringBuilder sb = new StringBuilder(source.length());
//        boolean nextMasked = false;
//        boolean wasDelimiter = false;
//        loop:
//        for (char ch : chars) {
//            if (nextMasked) {
//                sb.append(ch);
//                nextMasked = false;
//                wasDelimiter = false;
//                continue;
//            }
//            if (ch == mask) {
//                nextMasked = true;
//                continue;
//            }
//            for (char delimiter : delimiters) {
//                if (ch == delimiter) {
//                    if (!wasDelimiter) {
//                        al.add(sb.toString().trim());
//                        sb.delete(0, sb.length());
//                        wasDelimiter = true;
//                    }
//                    continue loop;
//                }
//            }
//            wasDelimiter = false;
//            sb.append(ch);
//        }
//        al.add(sb.toString().trim());
//        return al.toArray(new String[al.size()]);
//    }

    /**
     * Разбивает строку на подстроки по указанной строке-разделителю.
     * <p>
     * Если source null, вернёт null. В других случаях вернётся массив
     * как минимум из одной строки.
     * <p>
     * Крайние пробелы у подстрок удаляются.
     *
     * @param source
     * @param delimiter
     * @return массив подстрок или null.
     */
    public static String[] parseDelimited(String source, String delimiter) {
        if (source == null) {
            return null;
        }
        ArrayList<String> al = new ArrayList<>(32);
        int from = 0;
        while (true) {
            int to = source.indexOf(delimiter, from);
            if (to == -1) {
                al.add(source.substring(from).trim());
                return al.toArray(new String[al.size()]);
            } else {
                al.add(source.substring(from, to).trim());
                from = to + delimiter.length();
            }
        }
    }

    // ----- выковыривание слов

    /**
     * Интерфейс, определяющий, является ли переданный символ разделителем, для {@link #getWordIndex(String, int, WordDelimiterDetector)}.
     */
    @FunctionalInterface
    public interface WordDelimiterDetector {
        boolean isDelimiter(char ch);
    }

    public static final WordDelimiterDetector WHITESPACE = Character::isWhitespace;
    public static final WordDelimiterDetector LINEFEED = ch -> ch == '\n' || ch == '\r';

    /**
     * Находит индекс первого символа слова с индексом {@code index} в строке {@code original}.
     * Индекс первого слова 0.
     * <p>
     * Слова разделяются символами, для которых переданный детектор возвращает true.
     * Несколько разделителей подряд считаем одним.
     * <p>
     * Если в строке меньше слов, то вернётся длина строки.
     *
     * @param original исходная строка
     * @param index    номер слова
     * @param wdd      детектор разделителей слов
     * @return индекс первого символа или длина строки.
     */
    public static int getWordIndex(String original, int index, WordDelimiterDetector wdd) {
        boolean wasSpace = true;
        int i = -1;
        while (index >= 0 && ++i < original.length()) {
            if (wdd.isDelimiter(original.charAt(i))) {
                wasSpace = true;
            } else {
                if (wasSpace) {
                    index--;
                    wasSpace = false;
                }
            }
        }
        return i;
    }

    /**
     * Возвращает строку с отрезанными первыми по индексу.
     * Индекс первого слова 0.
     * <p>
     * Слова разделяются любыми пробельными символами.
     * Несколько разделителей подряд считаем одним.
     *
     * @param original исходная строка
     * @param from     с какого слова отрезать
     * @return подстрока
     */
    public static String subWords(String original, int from) {
        return subWords(original, from, WHITESPACE);
    }

    /**
     * Возвращает строку с отрезанными первыми по индексу.
     * Индекс первого слова 0.
     * В случае невозможных индексов возвращает пустую строку.
     * <p>
     * Слова разделяются символами, для которых переданный детектор возвращает true.
     * Несколько разделителей подряд считаем одним.
     *
     * @param original исходная строка
     * @param from     с какого слова отрезать
     * @param wdd      детектор разделителей слов
     * @return подстрока
     */
    public static String subWords(String original, int from, WordDelimiterDetector wdd) {
        return original == null || original.isEmpty() ? original : original.substring(getWordIndex(original, from, wdd));
    }

    /**
     * Возвращает подстроку с отрезанными словами с первого символа слова с индексом {@code from}
     * до последнего символа слова с индексом {@code to}-1.
     * Индекс первого слова 0.
     * <p>
     * Слова разделяются любыми пробельными символами.
     * Несколько разделителей подряд считаем одним.
     *
     * @param original исходная строка
     * @param from     с какого слова отрезать
     * @param to       по какое слово отрезать
     * @param wdd      детектор разделителей слов
     * @return подстрока
     */
    public static String subWords(String original, int from, int to, WordDelimiterDetector wdd) {
        if (original == null || original.isEmpty()) {
            return original;
        }
        if (from >= to) {
            return "";
        }
        int u = getWordIndex(original, from, wdd);
        boolean wasNonSpace = false;
        int i = -1;
        while (to > 0 && ++i < original.length()) {
            if (wdd.isDelimiter(original.charAt(i))) {
                if (wasNonSpace) {
                    to--;
                    wasNonSpace = false;
                }
            } else {
                wasNonSpace = true;
            }
        }
        return original.substring(u, i);
    }

    /**
     * Возвращает подстроку с отрезанными словами с первого символа слова {@code from}
     * до последнего символа слова {@code to}-1.
     * Индекс первого слова 0.
     *
     * @param original исходная строка
     * @param from     с какого слова отрезать
     * @param to       по какое слово отрезать
     * @return подстрока
     */
    public static String subWords(String original, int from, int to) {
        return subWords(original, from, to, WHITESPACE);
    }


    // ----- Замены подстрок

    /**
     * Форматирует строку при помощи {@link tk.bolovsrol.utils.textformatter.compiling.ProxyingCompiledFormatter текстового форматирователя}.
     * <p>
     * В качестве разделителей используются значения по умолчанию, описанные в {@link TextFormatCompiler}.
     * <p>
     * Этот метод прост и удобен для простых случаев,
     * а в сложных лучше работать непосредственно с форматирователем.
     *
     * @param template шаблон текста
     * @param pp       значения для подстановок
     * @return строка с выполненными подстановками
     * @see tk.bolovsrol.utils.textformatter.compiling.ProxyingCompiledFormatter
     * @see TextFormatCompiler
     */
    public static String format(String template, ReadOnlyProperties pp) throws InvalidTemplateException {
        if (template == null || template.isEmpty()) {
            return template;
        }
        return new TextFormatCompiler().compile(template).format(new ReadOnlySourceEvaluator(pp));
    }

    /**
     * Заменяет в строке все вхождения ключевого слова на значение.
     *
     * @param original строка
     * @param keyword  ключевое слово
     * @param value    значение
     * @return изменённая строка
     */
    public static String substitute(String original, String keyword, String value) {
        return substitute(original, new String[]{keyword}, new String[]{value});
    }

    /**
     * Заменяет ключевые слова, встречающиеся в строке, их значениями.
     * Нужно задать массивы ключевых слов и значений равной длины,
     * и для каждого встреченного вхождения ключевого слова (как подстроки)
     * будет взято значение с соответствующим индексом.
     * <p>
     * Ключевые слова перебираются в порядке их расположения в массиве.
     *
     * @param original строка
     * @param keywords ключевые слова
     * @param values   значения
     * @return строка, в которой вместо ключевых слов расставлены соответствующие значения
     * @see #format(String, ReadOnlyProperties)
     */
    public static String substitute(String original, String[] keywords, String[] values) throws IllegalArgumentException {
        if (original == null || original.isEmpty()) {
            return original;
        }

        if (keywords == null || values == null || keywords.length != values.length) {
            throw new IllegalArgumentException("Either keywords or values is null or keyword and values lengths do not match, keywords: " + Spell.get(keywords) + ", values: " + Spell.get(values));
        }

        if (keywords.length == 0) {
            return original;
        }

        StringBuilder sb = new StringBuilder(original.length() * 2);
        sb.append(original);
        boolean changed = false;

        for (int i = 0; i < keywords.length; i++) {
            String keyword = keywords[i];
            int po = sb.indexOf(keyword);
            if (po != -1) {
                String value = values[i] == null ? EMPTY_STRING : values[i];
                do {
                    sb.replace(po, po + keyword.length(), value);
                    changed = true;
                    po = sb.indexOf(keyword, po + value.length());
                } while (po != -1);
            }
        }
        return changed ? sb.toString() : original;
    }

    /**
     * Заменяет подстроки, формируемые в виде {<индекс значения>} (индекс значения в фигурных скобках)
     * на соответстувющие values.
     * <p>
     * Т.е. {0} заменяется на значение value[0], {1} -- на значение value[1] и т.д.
     * Индексы перебираются от меньших к большим.
     * <p>
     * Для замен используется {@link tk.bolovsrol.utils.textformatter.compiling.ProxyingCompiledFormatter} c {@link ArrayEvaluator}-ом.
     *
     * @param template
     * @param values
     * @return исправленная строка
     * @see #format(String, ReadOnlyProperties)
     */
    public static String substitute(String template, String[] values) throws InvalidTemplateException {
        if (template == null || template.isEmpty() || values == null || values.length == 0) {
            return template;
        }
        return new TextFormatCompiler().compile(template).format(new ArrayEvaluator(values));
    }

    /**
     * @see #format(String, ReadOnlyProperties)
     */
    public static String substitute(String original, String value) throws InvalidTemplateException {
        return substitute(original, new String[]{value});
    }

    /**
     * Проверяет, что одна из строк массива совпадает с паттерном.
     * <p>
     * Тупо перебирает, так что лучше передавать небольшой список.
     * А для больших лучше пользоваться методом contains() какой-нить
     * специально обученной коллекции.
     *
     * @param pattern
     * @param values
     * @return true, если таки да.
     */
    public static boolean contains(String pattern, String[] values) {
        if (values == null || values.length == 0) {
            return false;
        }

        if (pattern == null) {
            for (String value : values) {
                if (value == null) {
                    return true;
                }
            }
        } else {
            for (String value : values) {
                if (pattern.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Возвращает строковое представление переданного аргумента, если он не нул, а иначе нул.
     *
     * @param object
     * @return
     */
    public static String valueOf(Object object) {
        return object == null ? null : object.toString();
    }

    /**
     * Возвращает строковое представление переданного аргумента, если он не нул, а иначе nullMask.
     *
     * @param object
     * @param nullMask
     * @return
     */
    public static String valueOf(Object object, String nullMask) {
        return object == null ? nullMask : object.toString();
    }

    /**
     * Возвращает строковое представление переданного аргумента, если он не нул, а иначе nullMask.
     *
     * @param object
     * @param nullMaskSupplier
     * @return
     */
    public static String valueOf(Object object, Supplier<String> nullMaskSupplier) {
        return object == null ? nullMaskSupplier.get() : object.toString();
    }

    /**
     * Возвращает числовое значение символа-цифры.
     * <p>
     * Примерно как Character.digit(), но потупее.
     *
     * @param ch
     * @return число
     */
    public static int getDigitValue(char ch) {
        return ch >= 'a' ? (int) ch - (int) 'a' + 10 : ch >= 'A' ? (int) ch - (int) 'A' + 10 : (int) ch - (int) '0';
    }

    /**
     * Возвращает символ-циферку для числового значения.
     * <p>
     * Примерно как Character.forDigit(), но потупее.
     *
     * @param digit число
     * @return символ-цифра, соответствующий числу
     */
    public static char getCharForDigit(int digit) {
        return (char) ((digit < 10 ? (int) '0' : (int) 'a' - 10) + digit);
    }

    /**
     * Создаёт строку с шестнадцатеричным дампом массива байтов.
     * <p>
     * Так, каждый байт получается представлен двумя символами --
     * шестнадцатиричными цифрами.
     *
     * @param bytes
     * @return дамп массива
     */
    public static String getHexDump(byte[] bytes) {
        return getHexDump(bytes, 0, bytes.length);
    }

    /**
     * Создаёт строку с шестнадцатеричным дампом фрагмента массива байтов.
     * <p>
     * Так, каждый байт получается представлен двумя символами --
     * шестнадцатиричными цифрами, например:
     * <pre>
     *     0001020304050607080a0b0c0d0e0f1011
     *  </pre>
     * — и так далее.
     *
     * @param bytes
     * @param from  с какого байта начинать, включительно
     * @param to    до какого байта дампить, исключительно
     * @return дамп массива
     */
    public static String getHexDump(byte[] bytes, int from, int to) {
        char[] buf = new char[to * 2];
        int bufPos = 0;
        for (int i = from; i < to; i++) {
            buf[bufPos++] = getCharForDigit((bytes[i] >> 4) & 0xf);
            buf[bufPos++] = getCharForDigit(bytes[i] & 0xf);
        }
        return new String(buf);
    }

    /**
     * Создаёт строку с шестнадцатеричным дампом массива байтов.
     * <p>
     * Так, каждый байт получается представлен двумя символами --
     * шестнадцатиричными цифрами. Байты отделены друг от друга пробелами и точками,
     * после каждого 4-го байта точка, после прочих — пробелы, например:
     * <pre>
     * 00 01 02 03.04 05 06 07.08 09 0a 0b.0c 0d 0e 0f.10 11
     * </pre>
     * — и так далее.
     *
     * @param bytes
     * @return дамп массива
     */
    public static String getDelimitedHexDump(byte[] bytes) {
        return getDelimitedHexDump(bytes, 0, bytes.length);
    }

    /**
     * Создаёт строку с шестнадцатеричным дампом фрагмента массива байтов.
     * <p>
     * Так, каждый байт получается представлен двумя символами --
     * шестнадцатиричными цифрами. Байты отделены друг от друга пробелами и точками,
     * после каждого 4-го байта точка, после прочих — пробелы, например:
     * <pre>
     * 00 01 02 03.04 05 06 07.08 09 0a 0b.0c 0d 0e 0f.10 11
     * </pre>
     * — и так далее.
     * <p>
     * Если нужно дампить нул, вернёт нул. Если нужно дампить 0 байтов или меньше, вернёт пустую строку.
     *
     * @param bytes
     * @param from  с какого байта начинать, включительно
     * @param to    до какого байта дампить, исключительно
     * @return дамп массива
     */
    public static String getDelimitedHexDump(byte[] bytes, int from, int to) {
        if (bytes == null) {
            return null;
        }
        int len = to - from;
        if (len <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder((len * 3) - 1);
        appendDelimitedHexDump(bytes, from, to, sb);
        return sb.toString();
    }

    /**
     * Добавляет билдеру строку с шестнадцатеричным дампом массива байтов.
     * <p>
     * Так, каждый байт получается представлен двумя символами --
     * шестнадцатиричными цифрами. Байты отделены друг от друга пробелами и точками,
     * после каждого 4-го байта точка, после прочих — пробелы, например:
     * <pre>
     * 00 01 02 03.04 05 06 07.08 09 0a 0b.0c 0d 0e 0f.10 11
     * </pre>
     * — и так далее.
     *
     * @param bytes
     * @return дамп массива
     */
    public static void appendDelimitedHexDump(byte[] bytes, StringBuilder sb) {
        appendDelimitedHexDump(bytes, 0, bytes.length, sb);
    }

    /**
     * Добавляет билдеру строку с шестнадцатеричным дампом фрагмента массива байтов.
     * <p>
     * Так, каждый байт получается представлен двумя символами --
     * шестнадцатиричными цифрами. Байты отделены друг от друга пробелами и точками,
     * после каждого 4-го байта точка, после прочих — пробелы, например:
     * <pre>
     * 00 01 02 03.04 05 06 07.08 09 0a 0b.0c 0d 0e 0f.10 11
     * </pre>
     * — и так далее.
     *
     * @param bytes
     * @param from  с какого байта начинать, включительно
     * @param to    до какого байта дампить, исключительно
     * @return дамп массива
     */
    public static void appendDelimitedHexDump(byte[] bytes, int from, int to, StringBuilder sb) {
        to--;
        for (int i = from; i < to; i++) {
            sb.append(getCharForDigit((bytes[i] >> 4) & 0xf))
                    .append(getCharForDigit(bytes[i] & 0xf));
            if ((i & 3) == 3) {
                sb.append('.');
            } else {
                sb.append(' ');
            }
        }
        sb.append(getCharForDigit((bytes[to] >> 4) & 0xf))
                .append(getCharForDigit(bytes[to] & 0xf));
    }


    /**
     * Преобразует переданноую строчку в ASCII-дамп.
     *
     * @param str Source string
     * @return Hex string
     */
    public static String getHexDumpForAscii(String str) {
        return getHexDump(str, "ISO-8859-1");
    }

    /**
     * Возвращает дамп строки в указанной кодировке.
     *
     * @param str
     * @param charsetName
     * @return шестнадцатиричный дамп
     */
    public static String getHexDump(String str, String charsetName) {
        try {
            return getHexDump(str, Charset.forName(charsetName));
        } catch (UnsupportedCharsetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Возвращает дамп строки в указанной кодировке.
     *
     * @param str
     * @param charset
     * @return шестнадцатиричный дамп
     */
    public static String getHexDump(String str, Charset charset) {
        return getHexDump(str.getBytes(charset));
    }

    /**
     * Возвращает ASCII-содержимое массива байтов,
     * символы с кодами меньше 0x20 или больше 0x7f
     * заменяются на указанный маскировочный символ.
     *
     * @param bytes
     * @param maskChar
     * @return строка
     */
    public static String getAsciiPrintable(byte[] bytes, char maskChar) {
        return getAsciiPrintable(bytes, 0, bytes.length, maskChar);
    }

    /**
     * Возвращает ASCII-содержимое фрагмента массива байтов,
     * символы с кодами меньше 0x20 или больше 0x7f
     * заменяются на указанный маскировочный символ.
     *
     * @param bytes
     * @param from
     * @param len
     * @param maskChar
     * @return строка
     */
    public static String getAsciiPrintable(byte[] bytes, int from, int len, char maskChar) {
        char[] chars = new char[len];
        int pos = 0;
        int to = from + len;
        for (int i = from; i < to; i++) {
            byte val = bytes[i];
            if (val < (byte) 0x20 || val > (byte) 0x7f) {
                chars[pos++] = maskChar;
            } else {
                chars[pos++] = (char) val;
            }
        }
        return new String(chars);
    }

    /**
     * Создаёт массив байтов из текстового дампа.
     * <p>
     * Проверяет, что дамп содержит чётное количество символов
     * и что все символы - шестнадцатиричные цифры
     * либо разделители — пробел и точка — которые игнорируются.
     * <p>
     * Этот метод примет дамп, сделанный {@link Spell#get(byte[])}.
     *
     * @param hexDump
     * @return массив байтов, сделанный из дампа
     * @throws UnexpectedBehaviourException строка не прошла проверку
     * @see StringUtils#validateHexDump(String)
     * @see StringUtils#getBytesForHexDumpUnchecked(String)
     */
    public static byte[] getBytesForHexDump(String hexDump) throws UnexpectedBehaviourException {
        validateHexDump(hexDump);
        return getBytesForHexDumpUnchecked(hexDump);
    }

    /**
     * Проверяет, что дамп содержит чётное количество символов
     * и что все символы - шестнадцатиричные цифры
     * либо разделители — пробел и точка.
     *
     * @param hexDump
     * @throws UnexpectedBehaviourException строка не прошла проверку
     * @see StringUtils#getBytesForHexDump(String)
     */
    public static void validateHexDump(String hexDump) throws UnexpectedBehaviourException {
        if (hexDump == null) {
            throw new UnexpectedBehaviourException("Hex dump is null.");
        }
        int digits = 0;
        for (int i = 0; i < hexDump.length(); i++) {
            char ch = hexDump.charAt(i);
            if (ch == '.' || ch <= ' ') {
                continue;
            }
            digits++;
            if ((ch < '0' || ch > '9') &&
                    (ch < 'A' || ch > 'F') &&
                    (ch < 'a' || ch > 'f')) {
                throw new UnexpectedBehaviourException("Invalid character [" + ch + "] at pos " + i + " for hex dump: " + Spell.get(hexDump));
            }
        }
        if ((digits & 1) != 0) {
            throw new UnexpectedBehaviourException("Invalid odd length " + digits + " for hex dump " + Spell.get(hexDump));
        }
    }

    /**
     * Создаёт массив байтов из текстового дампа.
     * <p>
     * Дамп не проверяет никак.
     *
     * @param hexDump
     * @return массив байтов, сделанный из дампа
     * @see StringUtils#getBytesForHexDump(String)
     */
    @SuppressWarnings("resource")
    public static byte[] getBytesForHexDumpUnchecked(String hexDump) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(hexDump.length() / 2);
        char[] chars = hexDump.toCharArray();
        int charCounter = 0;
        while (charCounter < chars.length) {
            char chh, chl;
            do {
                chh = chars[charCounter++];
            } while (chh == '.' || chh <= ' ');
            do {
                chl = chars[charCounter++];
            } while (chl == '.' || chl <= ' ');
            baos.write(getDigitValue(chh) << 4 | getDigitValue(chl));
        }
        return baos.toByteArray();
    }

    /**
     * Прокрустово ложе. Делает так, чтобы длина текста
     * не превышала указанной максимальной длины.
     * <p>
     * Всё, что превышает, будет откушено. Откушано, ага.
     *
     * @param text
     * @param maxLength
     * @return текст, не превышающий максимальной длины.
     */
    public static String truncate(String text, int maxLength) {
        if (text != null && text.length() > maxLength) {
            return text.substring(0, maxLength);
        } else {
            return text;
        }
    }

    /**
     * Прокрустово ложе. Делает так, чтобы длина текста
     * не превышала указанной максимальной длины.
     * <p>
     * Если длина строки выше, то будет откушено столько,
     * чтобы в нужную длину вместить суффикс, который
     * будет добавлен после обрезанного.
     * <p>
     * Длина суффикса не должна превышать maxLength.
     *
     * @param text
     * @param maxLength
     * @param suffix
     * @return текст, не превышающий максимальной длины.
     */
    public static String truncate(String text, int maxLength, String suffix) {
        if (text != null && text.length() > maxLength) {
            return text.substring(0, maxLength - suffix.length()) + suffix;
        } else {
            return text;
        }
    }

    /**
     * Делает так, чтобы длина текста была не менее minLen,
     * добавляя к text минимально необходимое количество fill слева или справа.
     * <p>
     * Если длина fill 1 символ и длина text меньше minLen, то результирующий текст
     * будет ровно minLen символов длиной.
     * <p>
     * Если text null, то вернётся тоже null.
     *
     * @param text    исходный текст
     * @param minLen  минимально необходимая длина
     * @param fill    строка-наполнитель
     * @param padding определяет сторону, с которой дополнять
     * @return текст длиной не менее minLen
     */
    public static String pad(String text, int minLen, String fill, Padding padding) {
        if (text == null || text.length() >= minLen || fill == null || fill.length() < 1) {
            return text;
        }
        int paddingLen = minLen - text.length();
        StringBuilder sb = new StringBuilder(paddingLen);
        while (sb.length() < paddingLen) {
            sb.append(fill);
        }
        return padding.join(text, sb.toString());
    }

    /**
     * Сторона дополнения.
     */
    public enum Padding {
        /**
         * Дополнять слева.
         */
        LEFT {
            @Override
            String join(String text, String padding) {
                return padding + text;
            }
        },
        /**
         * Дополнять справа.
         */
        RIGHT {
            @Override
            String join(String text, String padding) {
                return text + padding;
            }
        };

        abstract String join(String text, String padding);
    }

    /**
     * Проверяет равенство двух строк.
     * <p>
     * Строки равны, если обе null либо str1.equals(str2).
     *
     * @param str1
     * @param str2
     * @return true, если равны, иначе false
     */
    public static boolean equals(String str1, String str2) {
        return Objects.equals(str1, str2);
    }

    /**
     * Проверяет равенство двух строк, пофигу на регистр.
     * <p>
     * Строки равны, если обе null либо str1.equalsIgnoreCase(str2).
     *
     * @param str1
     * @param str2
     * @return true, если равны, иначе false
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        return (str1 == str2) || (str1 != null && str1.equalsIgnoreCase(str2));
    }

    /**
     * Возвращает произвольным образом выбранный фрагмент текста,
     * ограниченный началом или концом строки либо указанным разделителем.
     *
     * @param text
     * @param delimiter
     * @return фрагмент строки
     */
    public static String getRandomPart(String text, String delimiter) {
        if (text == null || !text.contains(delimiter)) {
            return text;
        }

        int arraySizeHint = (text.length() / delimiter.length()) >> 1;
        List<Integer> from = new ArrayList<>(arraySizeHint);
        List<Integer> to = new ArrayList<>(arraySizeHint);
        int pos = 0;
        while (true) {
            from.add(pos);
            pos = text.indexOf(delimiter, pos);
            if (pos == -1) {
                to.add(text.length());
                break;
            }
            to.add(pos);
            pos += delimiter.length();
            if (pos >= text.length()) {
                break;
            }
        }

        int piece = RandomizeUtils.getInt(from.size());
        text = text.substring(from.get(piece), to.get(piece));
        return text;
    }

    /**
     * Направление обрезания для {@link StringUtils#trim(String, TrimFilter, TrimMode)}.
     */
    public enum TrimMode {
        BOTH(true, true),
        FROM_LEFT(true, false),
        FROM_RIGHT(false, true);

        final boolean fromLeft;
        final boolean fromRight;

        TrimMode(boolean fromLeft, boolean fromRight) {
            this.fromLeft = fromLeft;
            this.fromRight = fromRight;
        }
    }

    /**
     * Интерфейс для обрезателя {@link StringUtils#trim(String, TrimFilter, TrimMode)}.
     */
    @FunctionalInterface
    public interface TrimFilter {
        /**
         * Определяет, можно ли убирать указанный символ.
         *
         * @param ch        символ, который хочется убрать
         * @param source    исходная строка
         * @param pos       положение исследуемого символа в строке
         * @param direction направление обрезания (может быть {@link TrimMode#FROM_LEFT} или {@link TrimMode#FROM_RIGHT})
         * @return true, если символ можно убирать, иначе false
         */
        boolean allowTrim(char ch, String source, int pos, TrimMode direction);
    }

    /**
     * Фильтр для {@link StringUtils#trim(String, TrimFilter, TrimMode)}, обрезающий все пробельные символы.
     *
     * @see Character#isWhitespace(char)
     */
    public static final TrimFilter WHITESPACE_FILTER = (ch, source, pos, direction) -> Character.isWhitespace(ch);

    /**
     * Фильтр для {@link StringUtils#trim(String, TrimFilter, TrimMode)}, обрезающий
     * пробелы, переводы строк и табуляцию.
     */
    public static final TrimFilter SPACE_FILTER = (ch, source, pos, direction) -> ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t';

    /**
     * Фильтр для {@link StringUtils#trim(String, TrimFilter, TrimMode)}, обрезающий
     * одинарные и двойные кавычки.
     */
    public static final TrimFilter QUOTE_FILTER = (ch, source, pos, direction) -> ch == '\"' || ch == '\'';

    /**
     * Фильтр для {@link StringUtils#trim(String, TrimFilter, TrimMode)}, обрезающий символ «0».
     */
    public static final TrimFilter ZERO_FILTER = (ch, source, pos, direction) -> ch == '0';

    /**
     * Обрезает сначала и с конца строки симолы, одобренные фильтром.
     * <p>
     * Если в качестве исходной строки передан null, вернётся тоже null.
     *
     * @param source   строка
     * @param filter   фильтр, разрешающий или не разрешающий удалять символы
     * @param trimMode направление обрезания
     * @return обрезанную строку
     */
    public static String trim(String source, TrimFilter filter, TrimMode trimMode) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        char[] chars = source.toCharArray();
        int to = chars.length;
        boolean unchanged = true;
        if (trimMode.fromRight) {
            while (to > 0 && filter.allowTrim(chars[to - 1], source, to - 1, TrimMode.FROM_RIGHT)) {
                to--;
                unchanged = false;
            }
        }
        int from = 0;
        if (trimMode.fromLeft) {
            while (from < to && filter.allowTrim(chars[from], source, from, TrimMode.FROM_LEFT)) {
                from++;
                unchanged = false;
            }
        }
        if (unchanged) {
            return source;
        } else if (from == to) {
            return "";
        } else {
            return source.substring(from, to);
        }
    }

    /**
     * Удаляет из строки пробелы. Крайние пробелы (с начала и с конца) удаляются совсем.
     * Повторющиеся пробелы между словами сокращаются до одиночных.
     *
     * @param source исходная строка
     * @return строка с минимумом пробелов
     */
    public static String removeExtraWhitespace(String source) {
        StringBuilder sb = new StringBuilder(source.length());
        sb.append(source.trim());
        boolean afterspace = false;
        for (int i = sb.length() - 1; i >= 0; i--) {
            if (Character.isWhitespace(sb.charAt(i))) {
                if (afterspace) {
                    sb.deleteCharAt(i);
                } else {
                    afterspace = true;
                }
            } else {
                afterspace = false;
            }

        }
        return sb.toString();
    }

    /**
     * Находит закрывающую последовательность, соответствующую открывающей.
     * Внутри может быть ещё несколько вложенных открывающих+закрывающих
     * последовательностей.
     * <p>
     * Предполагается, что from указывает на первый символ после открывающей последовательности.
     * <p>
     * Открывающая и закрывающая последовательности могут совпадать,
     * это будет работать, но ни о какой вложенности речи, понятное дело, не идёт.
     *
     * @param source        строка для исследования
     * @param openSequence  открывающая последовательность
     * @param closeSequence закрывающая последовательность
     * @param from          ме
     * @return индекс начала соответствующей закрывающей последовательности или -1
     */
    public static int getClosingPosition(String source, String openSequence, String closeSequence, int from) {
        int nearestClosePos = source.indexOf(closeSequence, from);
        if (nearestClosePos == -1) {
            return -1;
        }
        if (openSequence.equals(closeSequence)) {
            return nearestClosePos;
        }
        int nearestOpenPos = from;
        try {
            while (true) {
                nearestOpenPos = source.indexOf(openSequence, nearestOpenPos);
                if (nearestOpenPos == -1 || nearestOpenPos > nearestClosePos) {
                    return nearestClosePos;
                }
                nearestClosePos = source.indexOf(closeSequence, nearestClosePos + closeSequence.length());
                nearestOpenPos += openSequence.length();
            }
        } catch (IndexOutOfBoundsException ignored) {
            return -1;
        }
    }

    /**
     * Находит закрывающий символ, соответствующий открывающему
     * Внутри может быть ещё несколько вложенных открывающих+закрывающих
     * символов.
     * <p>
     * Параметр <code>from</code> должен указывать
     * на первый символ исследуемой последовательности (сразу за открывающим символом).
     * <p>
     * Открывающий и закрывающий символы могут совпадать,
     * это будет работать, но ни о какой вложенности речи, понятное дело, не идёт.
     * <p>
     * В процессе поиска символ, следующий за символом <code>mask</code>, игнорируется.
     * <p>
     * Между двумя одинаковыми символами из массива <code>quotes</code>
     * учитывается только символ <code>mask</code>.
     *
     * @param source       строка для исследования
     * @param openChar     открывающий символ
     * @param closeChar    закрывающий символ
     * @param pos          указатель на первый символ исследуемой последовательности
     * @param mask         маскирующий символ
     * @param quotesOrNull символы, которые ограждают неинтерпретируемую часть последовательности
     * @return индекс соответствующего закрывающего символа или -1, если такого символа не найдено
     */
    public static int getClosingPosition(String source, char openChar, char closeChar, int pos, char mask, char[] quotesOrNull) {
        boolean masked = false;
        boolean quoted = false;
        char currentQuote = '\0';
        int level = 1;
        int len = source.length();
        while (pos < len) {
            char ch = source.charAt(pos);
            if (masked) {
                masked = false;
            } else if (ch == mask) {
                masked = true;
            } else if (quoted) {
                if (ch == currentQuote) {
                    quoted = false;
                }
            } else if (ch == closeChar) {
                level--;
                if (level == 0) {
                    return pos;
                }
            } else if (ch == openChar) {
                level++;
            } else if (quotesOrNull != null) {
                for (char quote : quotesOrNull) {
                    if (ch == quote) {
                        quoted = true;
                        currentQuote = quote;
                        break;
                    }
                }
            }
            pos++;
        }
        return -1;
    }

    /**
     * Находит закрывающий символ, соответствующий открывающему
     * Внутри может быть ещё несколько вложенных открывающих+закрывающих
     * символов.
     * <p>
     * Параметр <code>from</code> должен указывать
     * на первый символ исследуемой последовательности (сразу за открывающим символом).
     * <p>
     * Открывающий и закрывающий символы могут совпадать,
     * это будет работать, но ни о какой вложенности речи, понятное дело, не идёт.
     * <p>
     * В процессе поиска символ, следующий за символом <code>mask</code>, игнорируется.
     * <p>
     * Между двумя одинаковыми символами из массива <code>quotes</code>
     * учитывается только символ <code>mask</code>.
     *
     * @param source       массив символов для исследования
     * @param openChar     открывающий символ
     * @param closeChar    закрывающий символ
     * @param pos          указатель на первый символ исследуемой последовательности
     * @param mask         маскирующий символ
     * @param quotesOrNull символы, которые ограждают неинтерпретируемую часть последовательности
     * @return индекс соответствующего закрывающего символа или -1, если такого символа не найдено
     */
    public static int getClosingPosition(char[] source, char openChar, char closeChar, int pos, char mask, char[] quotesOrNull) {
        boolean masked = false;
        boolean quoted = false;
        char currentQuote = '\0';
        int level = 1;
        int len = source.length;
        while (pos < len) {
            char ch = source[pos];
            if (masked) {
                masked = false;
            } else if (ch == mask) {
                masked = true;
            } else if (quoted) {
                if (ch == currentQuote) {
                    quoted = false;
                }
            } else if (ch == closeChar) {
                level--;
                if (level == 0) {
                    return pos;
                }
            } else if (ch == openChar) {
                level++;
            } else if (quotesOrNull != null) {
                for (char quote : quotesOrNull) {
                    if (ch == quote) {
                        quoted = true;
                        currentQuote = quote;
                        break;
                    }
                }
            }
            pos++;
        }
        return -1;
    }

    /**
     * Находит закрывающий символ, соответствующий открывающему
     * Внутри может быть ещё несколько вложенных открывающих+закрывающих
     * символов.
     * <p>
     * Параметр <code>from</code> должен указывать на символ,
     * открывающий последовательность в начале последовательности.
     * <p>
     * Открывающий и закрывающий символы могут совпадать,
     * это будет работать, но ни о какой вложенности речи, понятное дело, не идёт.
     * <p>
     * В процессе поиска символ, следующий за символом {@link #DEFAULT_MASK}, игнорируется.
     * <p>
     * Между двумя одинаковыми символами из массива {@link #DEFAULT_QUOTES}
     * учитывается только символ {@link #DEFAULT_MASK}.
     *
     * @param source    строка для исследования
     * @param openChar  открывающий символ
     * @param closeChar закрывающий символ
     * @param from      указатель на первый символ исследуемой последовательности
     * @return индекс соответствующего закрывающего символа или -1, если такого символа не найдено
     */
    public static int getClosingPosition(String source, char openChar, char closeChar, int from) {
        return getClosingPosition(source, openChar, closeChar, from, DEFAULT_MASK, DEFAULT_QUOTES);
    }

    /**
     * «Маскирует» символы: перед каждым символом из переданного набора, а также перед самой маской
     * ставит символ маски.
     *
     * @param source       исходная строка
     * @param mask         маска
     * @param maskingChars символы, которые надо замаскировать, помимо самой маски
     * @return исправленная строка
     * @see #unmask(String, char, char[])
     */
    public static String mask(String source, char mask, char[] maskingChars) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        // задом наперёд!
        for (int i = source.length() - 1; i >= 0; i--) {
            char ch = source.charAt(i);
            if (ch == mask) {
                return maskHeavy(source, mask, maskingChars, i);
            }
            for (char maskingChar : maskingChars) {
                if (ch == maskingChar) {
                    return maskHeavy(source, mask, maskingChars, i);
                }
            }
        }
        return source;
    }

    /**
     * Когда выяснили, что в строке есть чего помаскировать, маскируем уж по-полной.
     *
     * @param source       строка для маскировки
     * @param mask         маска
     * @param maskingChars маскируемые символы
     * @param i            последний символ, который надо замаскировать (перебираем от конца к началу)
     * @return замаскированная строка
     */
    private static String maskHeavy(String source, char mask, char[] maskingChars, int i) {
        StringBuilder sb = new StringBuilder(source.length() * 2).append(source);
        while (i >= 0) {
            char ch = sb.charAt(i);
            if (ch == mask) {
                sb.insert(i, mask);
            } else {
                for (char maskingChar : maskingChars) {
                    if (ch == maskingChar) {
                        sb.insert(i, mask);
                        break;
                    }
                }
            }
            i--;
        }
        return sb.toString();
    }

    /**
     * Удаляет из строки символы <code>mask</code> и не маскированные <code>quotesOrNull</code>.
     *
     * @param source       исходная строка
     * @param mask         маска
     * @param quotesOrNull кавычки
     * @return исправленная строка
     */
    public static String unmask(String source, char mask, char[] quotesOrNull) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        StringBuilder sb = new StringBuilder(source);
        int pos = 0;
        outer:
        while (pos < sb.length()) {
            char ch = sb.charAt(pos);
            if (ch == mask) {
                sb.deleteCharAt(pos);
                pos++;
                continue;
            }
            if (quotesOrNull != null) {
                for (char quote : quotesOrNull) {
                    if (ch == quote) {
                        sb.deleteCharAt(pos);
                        continue outer;
                    }
                }
            }
            pos++;
        }
        return sb.toString();
    }

    /**
     * Декодирует переданные байты как строку с указанным чарсетом,
     * если переданные байты для этого чарсета имеют смысл. Иначе выкидывает ошибку.
     *
     * @param bytes
     * @param charset
     * @return
     * @throws CharacterCodingException
     * @see #decodeOrDie(java.nio.ByteBuffer, java.nio.charset.Charset)
     */
    public static String decodeOrDie(byte[] bytes, int offset, int length, Charset charset) throws CharacterCodingException {
        return decodeOrDie(ByteBuffer.wrap(bytes, offset, length), charset);
    }

    /**
     * Декодирует переданные байты как строку с указанным чарсетом,
     * если переданные байты для этого чарсета имеют смысл. Иначе выкидывает ошибку.
     *
     * @param bytes
     * @param charset
     * @return
     * @throws CharacterCodingException
     * @see #decodeOrDie(java.nio.ByteBuffer, java.nio.charset.Charset)
     */
    public static String decodeOrDie(byte[] bytes, Charset charset) throws CharacterCodingException {
        return decodeOrDie(ByteBuffer.wrap(bytes), charset);
    }

    /**
     * Декодирует переданные байты как строку UTF-8,
     * если они имеют смысл для UTF-8. Иначе выкидывает ошибку.
     *
     * @param bytes
     * @return
     * @throws CharacterCodingException
     * @see #decodeOrDie(java.nio.ByteBuffer, java.nio.charset.Charset)
     */
    public static String decodeOrDie(byte[] bytes) throws CharacterCodingException {
        return decodeOrDie(ByteBuffer.wrap(bytes), UTF8);
    }

    /**
     * Декодирует переданный буфер как строку UTF-8,
     * если буфер для UTF-8 имеет смысл. Иначе выкидывает ошибку.
     *
     * @param byteBuffer
     * @return
     * @throws CharacterCodingException
     */
    public static String decodeOrDie(ByteBuffer byteBuffer) throws CharacterCodingException {
        return decodeOrDie(byteBuffer, UTF8);
    }

    /**
     * Декодирует переданный буфер как строку с указанным чарсетом,
     * если буфер для этого чарсета имеет смысл. Иначе выкидывает ошибку.
     *
     * @param byteBuffer
     * @param charset
     * @return
     * @throws CharacterCodingException
     */
    public static String decodeOrDie(ByteBuffer byteBuffer, Charset charset) throws CharacterCodingException {
        CharsetDecoder charsetDecoder = charset.newDecoder();
        charsetDecoder.onMalformedInput(CodingErrorAction.REPORT);
        charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        return charsetDecoder.decode(byteBuffer).toString();
    }

    /**
     * Если <code>string</code> нул, возвращает нул. Иначе возвращает подстроку,
     * находящуюся между <code>openSeq</code> и <code>closeSeq</code>.
     * <p>
     * При этом, если <code>openSeq</code> нул, возвращает подстроку с начала строки,
     * если <code>closeSeq</code> нул, возвращает подстроку до конца строки.
     * Если не-нульные openSeq или closeSeq не найдены, вовращает нул.
     *
     * @param string
     * @param openSeq
     * @param closeSeq
     * @return подстрока
     */
    public static String substring(String string, String openSeq, String closeSeq) {
        if (string == null) {
            return null;
        }
        int from;
        if (openSeq == null) {
            from = 0;
        } else {
            from = string.indexOf(openSeq);
            if (from < 0) {
                return null;
            }
            from += openSeq.length();
        }
        int to;
        if (closeSeq == null) {
            to = string.length();
        } else {
            to = string.indexOf(closeSeq, from);
            if (to < 0) {
                return null;
            }
        }
        return string.substring(from, to);
    }

    /**
     * Возвращает строку, состоящую из <code>count</code> копий <code>sequence</code>.
     *
     * @param sequence последовательность для размножения
     * @param count    множитель
     * @return <code>count</code> копий строки <code>string</code>.
     */
    public static String copies(CharSequence sequence, int count) {
        return appendCopiesTo(new StringBuilder(sequence.length() * count), sequence, count);
    }

    /**
     * Возвращает строку, состоящую из <code>count</code> копий последовательности <code>sequence</code>,
     * между которыми находится последовательность <code>delimiter</code>.
     *
     * @param sequence  последовательность для размножения
     * @param delimiter последовательность для разделения соседних sequence
     * @param count     множитель
     * @return <code>count</code> копий строки <code>string</code>.
     */
    public static String copies(CharSequence sequence, CharSequence delimiter, int count) {
        return appendCopiesTo(new StringBuilder((sequence.length() + delimiter.length()) * count), sequence, delimiter, count);
    }

    /**
     * Добавляет в переданный стрингбилдер <code>count</code> копий последовательности <code>sequence</code>,
     * между которыми вставляет последовательность <code>delimiter</code>.
     *
     * @param target    приёмник копий
     * @param sequence  последовательность для размножения
     * @param delimiter последовательность для разделения соседних sequence
     * @param count     множитель
     * @return <code>count</code> копий строки <code>string</code>.
     */
    public static String appendCopiesTo(StringBuilder target, CharSequence sequence, CharSequence delimiter, int count) {
        if (count > 0) {
            target.append(sequence);
            count--;
            for (; count > 0; count--) {
                target.append(delimiter);
                target.append(sequence);
            }
        }
        return target.toString();
    }

    /**
     * Добавляет в переданный стрингбилдер <code>count</code> копий последовательности <code>sequence</code> встык.
     *
     * @param target   приёмник копий
     * @param sequence последовательность для размножения
     * @param count    множитель
     * @return <code>count</code> копий строки <code>string</code>.
     */
    public static String appendCopiesTo(StringBuilder target, CharSequence sequence, int count) {
        for (; count > 0; count--) {
            target.append(sequence);
        }
        return target.toString();
    }

    /**
     * Преобразует джавные идентификаторы в строчные с подчёркиванием.
     * <p>
     * EverybodyLovesHypnotoad  → everybody_loves_hypnotoad<br/>
     * killAllHumans → kill_all_humans<br/>
     * и т. п.
     *
     * @param src
     * @return
     */
    public static String camelToUnderscore(String src) {
        StringBuilder sb = new StringBuilder(src.length() << 1);
        for (char ch : src.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                if (sb.length() > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * Преобразует строчные с подчёркиванием идентификаторы в джавные.
     * <p>
     * everybody_loves_hypnotoad → EverybodyLovesHypnotoad <br/>
     * kill_all_humans → killAllHumans<br/>
     * и т. п.
     * <p>
     * Лидирующие подчёркивания игнорируются.
     *
     * @param src
     * @param capitalizeFirst делать заглавной первую букву или нет
     * @return
     */
    public static String underscoreToCamel(String src, boolean capitalizeFirst) {
        boolean capitalizeNext = capitalizeFirst;
        StringBuilder sb = new StringBuilder(src.length());
        for (char ch : src.toCharArray()) {
            if (ch == '_') {
                if (sb.length() > 0) {
                    capitalizeNext = true;
                }
            } else if (capitalizeNext) {
                sb.append(Character.toUpperCase(ch));
                capitalizeNext = false;
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * Сравнивает две строки символ за символом.
     * <p>
     * Строка с меньшим символом в соответствующей позиции меньше.
     * Короткая строка меньше, чем более длинная, если начало длинной равно короткой строке.
     *
     * @param s1
     * @param s2
     * @return -1, 0 или 1, если с1 меньше, равна или больше s2 соответственно
     */
    public static int compareCharByChar(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        char[] chars1 = s1.toCharArray();
        char[] chars2 = s2.toCharArray();
        int i = 0;
        while (true) {
            if (i >= len1) {
                return i >= len2 ? 0 : -1;
            }
            if (i >= len2) {
                return 1;
            }
            char ch1 = chars1[i];
            char ch2 = chars2[i];
            if (ch1 < ch2) {
                return -1;
            }
            if (ch1 > ch2) {
                return 1;
            }
            i++;
        }
    }

    /**
     * Выкидывает незначащие нули из строкового представления числа.
     * <p>
     * 00.0000 → 0<br/>
     * 10 → 10<br/>
     * 010 → 10<br/>
     * 010.000 → 10<br/>
     * 1.100 → 1.1<br/>
     * 1.120 → 1.12<br/>
     * 1.123 → 1.123<br/>
     * и т. д.
     *
     * @param number
     * @return
     */
    public static String dropNonsignificantZeros(String number) {
        if (number == null || number.isEmpty()) {
            return number;
        }
        number = trim(
                number,
                ZERO_FILTER,
                number.indexOf('.') < 0 ? TrimMode.FROM_LEFT : TrimMode.BOTH
        );
        if (number.isEmpty() || ".".equals(number)) {
            return "0";
        } else if (number.charAt(number.length() - 1) == '.') {
            return number.substring(0, number.length() - 1);
        } else {
            return number;
        }
    }

    /**
     * Если source заканчивается на suffix, возвращает source без этого суффикса. Иначе возвращает исходную строку.
     *
     * @param source
     * @param suffix
     * @return
     */
    public static String withoutSuffix(String source, String suffix) {
        if (source.endsWith(suffix)) {
            return source.substring(0, source.length() - suffix.length());
        } else {
            return source;
        }
    }

}