package tk.bolovsrol.utils.textformatter.compiling.evaluators;

import tk.bolovsrol.utils.textformatter.compiling.KeywordEvaluator;

/**
 * Пробрасывает все запросы делегату.
 * <p/>
 * Изначально делегатом выступает {@link EmptyEvaluator}.
 */
public class ProxyKeywordEvaluator implements KeywordEvaluator {
    private KeywordEvaluator evaluator = EmptyEvaluator.INSTANCE;

    public void setEvaluator(KeywordEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public String evaluate(String keyword) {
        return evaluator.evaluate(keyword);
    }

    @Override public String toString() {
        return evaluator.toString();
    }
}
