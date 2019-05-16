package tk.bolovsrol.utils.textformatter.compiling.evaluators;

import tk.bolovsrol.utils.textformatter.compiling.KeywordEvaluator;

/**
 * Всегда возвращает нул.
 * <p/>
 * Внутренних состояний нет, можно пользоваться статическим {@link #INSTANCE}.
 */
public class EmptyEvaluator implements KeywordEvaluator {
    public static final EmptyEvaluator INSTANCE = new EmptyEvaluator();

    private EmptyEvaluator() {
    }

    @Override
    public String evaluate(String keyword) {
        return null;
    }

}
