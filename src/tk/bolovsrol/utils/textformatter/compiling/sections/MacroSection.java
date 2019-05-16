package tk.bolovsrol.utils.textformatter.compiling.sections;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.KeywordEvaluator;

/** Макрозначение, возвращённое вычислятелем {@link KeywordEvaluator}-а. */
public class MacroSection implements Section {

    private final String keyword;
    private final KeywordEvaluator keywordEvaluator;

    private String value;

    public MacroSection(String keyword, KeywordEvaluator keywordEvaluator) {
        this.keyword = keyword;
        this.keywordEvaluator = keywordEvaluator;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override public String evaluate(boolean strict) throws EvaluationFailedException {
        if (value == null) {
            value = keywordEvaluator.evaluate(keyword);
            if (value == null) {
                if (strict) {
                    throw new EvaluationFailedException("Keyword " + Spell.get(keyword) + " is not defined or inacceptable");
                }
                value = "";
            }
        }
        return value;
    }

    @Override
    public void reset() {
        value = null;
    }

    @Override
    public int hashCode() {
        return keyword.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof MacroSection && this.keyword.equals(((MacroSection) that).keyword);
    }

    @Override public String toString() {
        return "macro[" + keyword + ']';
    }
}
