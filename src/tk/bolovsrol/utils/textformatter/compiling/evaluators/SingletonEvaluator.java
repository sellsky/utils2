package tk.bolovsrol.utils.textformatter.compiling.evaluators;

import tk.bolovsrol.utils.textformatter.compiling.KeywordEvaluator;

/** Всегда возвращает одно и то же значение на все запросы. */
public class SingletonEvaluator implements KeywordEvaluator {

    private final String value;

    public SingletonEvaluator(String value) {
        this.value = value;
    }

    @Override
    public String evaluate(String keyword) {
        return value;
    }

}
