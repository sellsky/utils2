package tk.bolovsrol.utils.textformatter.compiling.evaluators;

import tk.bolovsrol.utils.textformatter.compiling.KeywordEvaluator;

/**
 * Префикс-решатель: если ключевое слово начинается с префикса,
 * то этот префикс отргрызается, и получившееся ключевое слово
 * решается переданным решателем.
 */
public class PrefixedEvaluator implements KeywordEvaluator {
    private final String prefix;
    private final int prefixLen;
    private final KeywordEvaluator delegate;

    public PrefixedEvaluator(String prefix, KeywordEvaluator delegate) {
        this.prefix = prefix;
        this.prefixLen = prefix.length();
        this.delegate = delegate;
    }

    /**
     * Вычисляет значение ключевого слова.
     * <p/>
     * Если слово неизвестно или вычислить его невозможно,
     * возвращает нул.
     *
     * @param keyword ключевое слово
     * @return значение ключевого слова или нул
     */
    @Override public String evaluate(String keyword) {
        if (keyword.startsWith(prefix)) {
            return delegate.evaluate(keyword.substring(prefixLen));
        } else {
            return null;
        }
    }
}
