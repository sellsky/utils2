package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.box.Box;
import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

import java.math.BigDecimal;

/**
 *
 */
public class NumericUniModifier implements ValueModifier {

    public static final NumericUniModifier ABSOLUTE = new NumericUniModifier(BigDecimal::abs);
    public static final NumericUniModifier NEGATE = new NumericUniModifier(BigDecimal::negate);

    @FunctionalInterface
    public interface Action {
        BigDecimal transform(BigDecimal src);
    }

    private final Action action;

    public NumericUniModifier(Action action) {
        this.action = action;
    }

    @Override
    public int getMinParameterCount() {
        return 0;
    }

    @Override
    public int getMaxParameterCount() {
        return 0;
    }

    @Override
    public String eval(String source, Section[] parameters, boolean strict) throws EvaluationFailedException {
        try {
            BigDecimal sourceBd = new BigDecimal(source);
            BigDecimal resultBd = Box.with(action.transform(sourceBd)).getOr(sourceBd);
            return resultBd.toPlainString();
        } catch (NumberFormatException e) {
            // если один из компонентов не число, то возвращаем исходное
        }
        return source;
    }

}
