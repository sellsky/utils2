package tk.bolovsrol.utils.textformatter.compiling.evaluators;

import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.textformatter.compiling.KeywordEvaluator;
import tk.bolovsrol.utils.time.Duration;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Решает ключевые слова при помощи метода {@link Object#toString()} содержимого
 * одноимённых полей переданного объекта.
 * <p/>
 * Может при необходимости искать для ключей «c подчёркиваниями» соответствующие
 * «верблюжьи» названия (some_long_name → someLongName).
 * <p/>
 * Специальным образом обрабатываются:<ul>
 * <li>{@link BigDecimal} — возвращается {@link BigDecimal#toPlainString()},</li>
 * <li>{@link Duration} — {@link Duration#getMillis()} и</li>
 * <li>{@link Date} — {@link Date#getTime()}.</li>
 * </ul>
 */
public class ReflectionFieldEvaluator implements KeywordEvaluator {

    private final Object source;
    private final Class<?> sourceClass;
    private final Map<String, Field> fieldCache = new TreeMap<String, Field>();
    private final boolean underscoreToCamel;

    /**
     * @param source            исследуемый объект
     * @param underscoreToCamel искать для ключей «c подчёркиваниями» соответствующие «верблюжьи» названия (some_long_name → someLongName).
     */
    public ReflectionFieldEvaluator(Object source, boolean underscoreToCamel) {
        this.source = source;
        this.sourceClass = source.getClass();
        this.underscoreToCamel = underscoreToCamel;
    }

    public ReflectionFieldEvaluator(Object source) {
        this(source, false);
    }

    @Override public String evaluate(String keyword) {
        try {
            Field field = retrieveField(keyword);
            return field == null ? null : ReflectionEvaluatorHelper.formatValue(field.get(source), field.getType());
        } catch (Exception ignored) {
            // there's nothing we can do
            return null;
        }
    }

    private Field retrieveField(String name) {
        Field field = fieldCache.get(name);
        if (field == null) {
            Class<?> cl = this.sourceClass;
            while (cl != null) {
                try {
                    field = cl.getDeclaredField(underscoreToCamel ? StringUtils.underscoreToCamel(name, false) : name);
                    field.setAccessible(true);
                    fieldCache.put(name, field);
                    break;
                } catch (NoSuchFieldException ignored) {
                    // просто переходим к суперобъекту
                }
                cl = cl.getSuperclass();
            }
        }
        return field;
    }
}
