package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

import java.math.BigDecimal;

/**
 *
 */
public class NumericComparisonModifier implements ValueModifier {

    @FunctionalInterface
    public interface Action {
        boolean matches(BigDecimal a, BigDecimal b);
    }

    private final Action action;

    public NumericComparisonModifier(Action action) {
        this.action = action;
    }

    @Override
    public int getMinParameterCount() {
        return 2;
    }

    @Override
    public int getMaxParameterCount() {
        return 3;
    }

    @Override
    public String eval(String source, Section[] parameters, boolean strict) throws EvaluationFailedException {
        try {
            BigDecimal sourceBd = new BigDecimal(source);
            BigDecimal specimenBd = new BigDecimal(parameters[0].evaluate(strict));
            if (action.matches(sourceBd, specimenBd)) {
                return parameters[1].evaluate(strict);
            }
        } catch (NumberFormatException e) {
            // если один из компонентов не число, то сравнение даёт false
        }
        return parameters.length <= 2 ? "" : parameters[2].evaluate(strict);
    }

    public static final NumericComparisonModifier EQUALS = new NumericComparisonModifier((a, b) -> a.compareTo(b) == 0);
    public static final NumericComparisonModifier NOT_EQUAL = new NumericComparisonModifier((a, b) -> a.compareTo(b) != 0);
    public static final NumericComparisonModifier GT = new NumericComparisonModifier((a, b) -> a.compareTo(b) > 0);
    public static final NumericComparisonModifier LT = new NumericComparisonModifier((a, b) -> a.compareTo(b) < 0);
    public static final NumericComparisonModifier GE = new NumericComparisonModifier((a, b) -> a.compareTo(b) >= 0);
    public static final NumericComparisonModifier LE = new NumericComparisonModifier((a, b) -> a.compareTo(b) <= 0);

}
