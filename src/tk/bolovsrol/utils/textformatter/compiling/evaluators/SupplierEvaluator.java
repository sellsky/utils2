package tk.bolovsrol.utils.textformatter.compiling.evaluators;

import tk.bolovsrol.utils.textformatter.compiling.KeywordEvaluator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Передаёт запрос решения решателям, зарегистрированным по  определённым ключевым словам. */
public class SupplierEvaluator implements KeywordEvaluator {

    private final Map<String, Supplier<String>> suppliers = new HashMap<>();

    public SupplierEvaluator() {
    }

    public SupplierEvaluator(String keyword, Supplier<String> supplier) {
        suppliers.put(keyword, supplier);
    }

    /**
     * Добавляет решатель ключевого слова.
     *
     * @param keyword слово
     * @param supplier решатель
     * @return this
     */
    public SupplierEvaluator register(String keyword, Supplier<String> supplier) {
        suppliers.put(keyword, supplier);
        return this;
    }

    /**
     * Добавляет решатель ключевых слов.
     *
     * @param keywords слово
     * @param supplier решатель
     * @return this
     */
    public SupplierEvaluator register(String[] keywords, Supplier<String> supplier) {
        for (String keyword : keywords) {
            suppliers.put(keyword, supplier);
        }
        return this;
    }

    /**
     * Добавляет решатель ключевых слов.
     *
     * @param keywords слово
     * @param supplier решатель
     * @return this
     */
    public SupplierEvaluator register(Collection<String> keywords, Supplier<String> supplier) {
        for (String keyword : keywords) {
            suppliers.put(keyword, supplier);
        }
        return this;
    }

    /**
     * Вычисляет значение ключевого слова.
     * <p>
     * Если слово неизвестно или вычислить его невозможно,
     * возвращает нул.
     *
     * @param keyword ключевое слово
     * @return значение ключевого слова или нул
     */
    @Override public String evaluate(String keyword) {
        Supplier<String> ke = suppliers.get(keyword);
        return ke == null ? null : ke.get();
    }

}
