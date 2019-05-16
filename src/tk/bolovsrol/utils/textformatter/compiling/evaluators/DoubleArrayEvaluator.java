package tk.bolovsrol.utils.textformatter.compiling.evaluators;

import tk.bolovsrol.utils.textformatter.compiling.KeywordEvaluator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Вычислятор значений по массивам ключей и значений.
 * <p/>
 * Конструктору надо передать список ключевых слов.
 * <p/>
 * Перед форматированием нужно установить массив значений.
 * <p/>
 * Для каждого ключевого слова будет возвращено значение, соответствующее индексу.
 * Если значений установлено меньше, чем ключевых слов, то вместо недостающих
 * значений возвратится null, так же как и для не найденных ключевых слов.
 */
public class DoubleArrayEvaluator implements KeywordEvaluator {
    private final Map<String, Integer> keyIndices = new HashMap<>();
    private Object[] values;

    public DoubleArrayEvaluator(String... keywords) {
        for (int i = 0; i < keywords.length; i++) {
            keyIndices.put(keywords[i], i);
        }
    }

    public DoubleArrayEvaluator(Collection<String> keywords) {
        int i = 0;
        for (String keyword : keywords) {
            keyIndices.put(keyword, i);
            i++;
        }
    }

    public DoubleArrayEvaluator(String[] keywords, Object[] values) {
        this(keywords);
        this.values = values;
    }

    public void setValues(Object... values) {
        this.values = values;
    }

    public Object[] getValues() {
        return values;
    }

    @Override
    public String evaluate(String keyword) {
        Integer index = keyIndices.get(keyword);
        if (index == null || index.intValue() >= values.length) {
            return null;
        } else {
            return values[index.intValue()].toString();
        }
    }

}