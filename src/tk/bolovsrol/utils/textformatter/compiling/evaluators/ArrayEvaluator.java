package tk.bolovsrol.utils.textformatter.compiling.evaluators;

import tk.bolovsrol.utils.textformatter.compiling.KeywordEvaluator;

/** Вычислятор значений по массиву строк. Должен передаваться индекс строки. */
public class ArrayEvaluator implements KeywordEvaluator {
    private final String[] values;

    public ArrayEvaluator(String value) {
        this(new String[]{value});
    }

    public ArrayEvaluator(String[] values) {
        this.values = values;
    }

    @Override public String evaluate(String keyword) {
        try {
            return values[Integer.parseInt(keyword)];
        } catch (Exception ignored) {
            return null;
        }
    }

}


