package tk.bolovsrol.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Утилитки, связанные с регулярными выражениями. */
public final class RegexUtils {
    private RegexUtils() {
    }

    /**
     * Проверяет, матчится ли sample по паттерну.
     * <p/>
     * Сравнение происходит по подстроке. Матчинг производится без учёта регистра, и «.» матчится в т.ч. на перевод строки.
     * <p/>
     * Если хотя бы один из аргументов нул, вернётся false.
     *
     * @param patternString
     * @param sample
     * @return true, если матчится.
     */
    public static boolean matches(String patternString, String sample) throws PatternCompileException {
        return patternString != null && sample != null && getMatcher(patternString, sample).find();
    }

    /**
     * Проверяет, матчится ли sample по паттерну.
     * <p/>
     * Сравнение происходит по подстроке. Матчинг производится без учёта регистра, и «.» матчится в т.ч. на перевод строки.
     * <p/>
     * Если хотя бы один из аргументов нул, вернётся false.
     *
     * @param pattern
     * @param sample
     * @return true, если матчится.
     */
    public static boolean matches(Pattern pattern, String sample) {
		return pattern != null && sample != null && pattern.matcher(sample).find();
	}

    /**
     * Возвращает массив строк, распарсенный из групп регекспа.
     * <p/>
     * 0-й элемент соответстувет первой группе, и т.д.
     * <p/>
     * Сравнение происходит по подстроке. Матчинг производится без учёта регистра,
     * «.» матчится в т.ч. на перевод строки.
     * <p/>
     * Если паттерн is null или не матчится, то возвращается пустой массив.
     *
     * @param patternString
     * @param sample
     * @return массив распарсенных подстрок
     * @see #getFirstMatch(String, String)
     */
    public static String[] parse(String patternString, String sample) throws PatternCompileException {
        if (patternString != null) {
            return parse(compilePattern(patternString), sample);
        }
        return StringUtils.EMPTY_STRING_ARRAY;
    }

    /**
     * Возвращает массив строк, распарсенный из групп регекспа.
     * <p/>
     * 0-й элемент соответстувет первой группе, и т.д.
     * <p/>
     * Сравнение происходит по подстроке. Матчинг производится без учёта регистра,
     * «.» матчится в т.ч. на перевод строки.
     * <p/>
     * Если паттерн is null или не матчится, то возвращается пустой массив.
     * <p/>
     * Если sample is null, возвращается null.
     *
     * @param pattern
     * @param sample
     * @return массив распарсенных подстрок
     * @see #getFirstMatch(String, String)
     */
    public static String[] parse(Pattern pattern, String sample) {
        if (sample == null) {
            return null;
        }
        Matcher matcher = getMatcher(pattern, sample);
        if (matcher.find()) {
            String[] result = new String[matcher.groupCount()];
            for (int i = 0; i < result.length; i++) {
                result[i] = matcher.group(i + 1);
            }
            return result;
        }
        return StringUtils.EMPTY_STRING_ARRAY;
    }

    /**
     * Упрощённый вариант {@link #parse(String, String)}. Парсит строку и возвращает
     * содержимое первой группы или null, не заматчилось.
     * <p/>
     * Если хотя бы один из аргументов нул, вернётся нул.
     *
     * @param patternString
     * @param sample
     * @return содержимое первой группы.
     * @see #parse(String, String)
     */
    public static String getFirstMatch(String patternString, String sample) throws PatternCompileException {
        return patternString == null || sample == null ? null : getFirstMatch(compilePattern(patternString), sample);
    }

    /**
     * Упрощённый вариант {@link #parse(Pattern, String)}. Парсит строку и возвращает
     * содержимое первой группы или null, не заматчиловь.
     * <p/>
     * Если хотя бы один из аргументов нул, вернётся нул.
     *
     * @param pattern
     * @param sample
     * @return содержимое первой группы.
     * @see #parse(String, String)
     */
    public static String getFirstMatch(Pattern pattern, String sample) {
        if (pattern == null || sample == null) {
            return null;
        }
        Matcher matcher = getMatcher(pattern, sample);
        if (matcher.find() && matcher.groupCount() > 0) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    /**
     * Генерирует матчер для регекспных поисковых методов
     * на основе {@link #compilePattern(String)}.
     *
     * @param patternString
     * @param sample
     * @return матчер
     * @see # compilePattern (String)
     */
    public static Matcher getMatcher(String patternString, String sample) throws PatternCompileException {
        return getMatcher(compilePattern(patternString), sample);
    }

    /**
     * Генерирует матчер для регекспных поисковых методов.
     *
     * @param pattern
     * @param sample
     * @return матчер
     * @see #compilePattern(String)
     */
    public static Matcher getMatcher(Pattern pattern, String sample) {
        return pattern == null || sample == null ? null : pattern.matcher(sample);
    }

    /**
     * Компилирует паттерн для регекспных поисковых  методов.
     * <p/>
     * Паттерн компилируется без различия регистра, допускает перевод строки в качестве «.».
     * <p/>
     * Если строка null, вернётся null.
     *
     * @param patternString строка для компиляции
     * @return паттерн или нул
     */
    public static Pattern compilePattern(String patternString) throws PatternCompileException {
        if (patternString == null) {
            return null;
        } else {
            try {
                return Pattern.compile(patternString, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);
            } catch (Exception e) {
                throw new PatternCompileException("Error compiling pattern " + Spell.get(patternString), e);
            }
        }
    }

    /**
     * Компилирует паттерн для регекспных поисковых методов.
     * Строка паттерна должна быть задана в DOS Wildcards-синтаксисе:
     * символ «*» обозначает любое количество любых символов, а символ «?» — любой один символ.
     * <p/>
     * Паттерн компилируется без различия регистра, допускает перевод строки.
     *
     * @param dosWildcardString
     * @return матчер
     */
    public static Pattern compileDosWildcard(String dosWildcardString) {
        try {
            return compilePattern(dosWildcardToPatternString(dosWildcardString));
        } catch (PatternCompileException e) {
            // этого не должно происходить, потому что паттерн мы готовим сами и аккуратно.
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a windows wildcard pattern to a regex pattern
     *
     * @param dosWildcardString - Wildcard pattern containing * and ?
     * @return - a regex pattern that is equivalent to the windows wildcard pattern
     */
    private static String dosWildcardToPatternString(String dosWildcardString) {
        if (dosWildcardString == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(dosWildcardString.length() * 2);
        sb.append('^');
        for (char ch : dosWildcardString.toCharArray()) {
            switch (ch) {
            case '*':
                sb.append(".*");
                break;
            case '?':
                sb.append('.');
                break;
            case '+': // prefix all metacharacters with backslash
            case '(':
            case ')':
            case '^':
            case '$':
            case '.':
            case '{':
            case '}':
            case '[':
            case ']':
            case '|':
            case '\\':
                sb.append('\\');
                // fallthrough
            default:
                sb.append(ch);
            }
        }
        sb.append('$');
        return sb.toString();
    }
}
