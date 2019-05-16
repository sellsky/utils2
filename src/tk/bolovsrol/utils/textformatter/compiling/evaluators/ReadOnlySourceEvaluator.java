package tk.bolovsrol.utils.textformatter.compiling.evaluators;

import tk.bolovsrol.utils.properties.sources.ReadOnlySource;
import tk.bolovsrol.utils.textformatter.compiling.KeywordEvaluator;

/** Вычислятор значений по пропертям. Должно передаваться имя проперти. */
public class ReadOnlySourceEvaluator implements KeywordEvaluator {
    private final ReadOnlySource properties;

    public ReadOnlySourceEvaluator(ReadOnlySource properties) {
        this.properties = properties;
    }

    @Override public String evaluate(String keyword) {
        return properties.get(keyword);
    }
}
