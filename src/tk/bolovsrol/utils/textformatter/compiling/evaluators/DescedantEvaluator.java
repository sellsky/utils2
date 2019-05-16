package tk.bolovsrol.utils.textformatter.compiling.evaluators;

import tk.bolovsrol.utils.textformatter.compiling.KeywordEvaluator;

/**
 * Источник-воронка. Автоматический сливатель нескольких источников.
 * <p/>
 * Если в первом источнике нет запрошенного ключа, то запрос адресуется
 * второму источнику и т.д.
 * <p/>
 * Аналогично, дамп возвращает содержимое всех источников.
 * Чем первее источник, тем выше приоритет его значения.
 */
public class DescedantEvaluator implements KeywordEvaluator {

    private final KeywordEvaluator[] sources;

    /**
     * Чем раньше соурс в списке, тем выше его приоритет.
     */
    public DescedantEvaluator(KeywordEvaluator... sources) {
        this.sources = sources;
    }

    @Override
    public String evaluate(String keyword) {
        for (KeywordEvaluator source : sources) {
            String result = source.evaluate(keyword);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

}