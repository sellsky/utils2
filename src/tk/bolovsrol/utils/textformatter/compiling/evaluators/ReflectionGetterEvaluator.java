package tk.bolovsrol.utils.textformatter.compiling.evaluators;

import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.reflectiondump.ExcludeFromReflectionDump;
import tk.bolovsrol.utils.textformatter.compiling.KeywordEvaluator;
import tk.bolovsrol.utils.time.Duration;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Решает ключевые слова, используя публичные геттеры переданного объекта.
 * <p/>
 * Как это работает.
 * <p/>
 * Если установлен режим underscoreToCamel, то ключ «c подчёркиваниями» предварительно
 * преобразуют в «верблюжий» (some_long_name → someLongName).
 * <p/>
 * Первую букву ключа капитализируют. Приписывают получившееся к «get» и «is» (keyword → getKeyword и isKeyword).
 * <p/>
 * Ищут у переданного объекта метод с get-именем без параметров. Если нет такого, то ищут метод с is-именем.
 * Если и такого метода нет, то ключ остаётся не решённым. Иначе используют полученный объект.
 * <p/>
 * Для следующих типов возвращают специальное:<ul>
 * <li>{@link BigDecimal} → {@link BigDecimal#toPlainString()},</li>
 * <li>{@link Duration} → {@link Duration#getMillis()} и</li>
 * <li>{@link Date} → {@link Date#getTime()}.</li>
 * </ul>
 * Для объектов остальных типов возвращают {@link Object#toString()}.
 */
public class ReflectionGetterEvaluator implements KeywordEvaluator {

    private final Object source;
    private final Class<?> sourceClass;
    private final boolean underscoreToCamel;

    @ExcludeFromReflectionDump
    private final Map<String, Method> methodCache = new TreeMap<>();

    /**
     * @param source            исследуемый объект
     * @param underscoreToCamel искать для ключей «c подчёркиваниями» соответствующие «верблюжьи» названия (some_long_name → getSomeLongName).
     */
    public ReflectionGetterEvaluator(Object source, boolean underscoreToCamel) {
        this.source = source;
        this.sourceClass = source.getClass();
        this.underscoreToCamel = underscoreToCamel;
    }

    public ReflectionGetterEvaluator(Object source) {
        this(source, false);
    }

    @Override public String evaluate(String keyword) {
        try {
            Method method = retrieveMethod(keyword);
            return method == null ? null : ReflectionEvaluatorHelper.formatValue(method.invoke(source), method.getReturnType());
        } catch (Exception ignored) {
            // there's nothing we can do
            return null;
        }
    }

    private Method retrieveMethod(String name) throws NoSuchMethodException {
        Method method = methodCache.get(name);
        if (method == null) {
            try {
                method = sourceClass.getMethod("get" + formatName(name));
            } catch (NoSuchMethodException ignored) {
                method = sourceClass.getMethod("is" + formatName(name));
            }
            methodCache.put(name, method);
        }
        return method;
    }

    private String formatName(String name) {
        if (underscoreToCamel) {
            name = StringUtils.underscoreToCamel(name, false);
        }
        switch (name.length()) {
        case 0:
            return "";
        case 1:
            return name.toUpperCase();
        default:
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }

    }

    @Override public String toString() {
        return new StringDumpBuilder()
              .append("source", source)
              .append("sourceClass", sourceClass)
              .append("underscoreToCamel", underscoreToCamel)
              .toString();
    }
}
