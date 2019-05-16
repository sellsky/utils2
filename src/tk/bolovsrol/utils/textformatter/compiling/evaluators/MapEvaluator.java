package tk.bolovsrol.utils.textformatter.compiling.evaluators;

import tk.bolovsrol.utils.textformatter.compiling.KeywordEvaluator;

import java.util.HashMap;
import java.util.Map;

/**
 * Вычислятор значений по карте ключ→значение.
 * <p/>
 * Использует переданную карту.
 */
public class MapEvaluator implements KeywordEvaluator {
    private Map<String, String> mappings;

    public MapEvaluator() {
        this(new HashMap<>());
    }

    public MapEvaluator(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    @Override public String evaluate(String keyword) {
        return mappings.get(keyword);
    }

	public Map<String, String> mappings() {
		return mappings;
	}

    public MapEvaluator add(String keyword, String value) {
        this.mappings.put(keyword, value);
        return this;
    }

    public MapEvaluator add(String[] keywords, String value) {
        for (String keyword : keywords) {
            this.mappings.put(keyword, value);
        }
        return this;
    }

    public MapEvaluator remove(String... keywords) {
        for (String keyword : keywords) {
            this.mappings.remove(keyword);
        }
        return this;
    }

    public MapEvaluator add(Map<String, String> mappings) {
        this.mappings.putAll(mappings);
        return this;
    }

    public MapEvaluator set(Map<String, String> mappings) {
        this.mappings = mappings;
        return this;
	}

    public MapEvaluator clear() {
        this.mappings.clear();
        return this;
    }
}
