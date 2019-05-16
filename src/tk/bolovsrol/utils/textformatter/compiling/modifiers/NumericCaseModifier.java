package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

import java.math.BigDecimal;

/**
 *
 */
class NumericCaseModifier implements ValueModifier {

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
            int index = getNumericCaseIndex(new BigDecimal(source), strict);
            if (parameters.length == 2) {
                return index == 0 ? "" : parameters[index - 1].evaluate(strict);
            } else {
                return parameters[index].evaluate(strict);
            }
        } catch (NumberFormatException e) {
            if (strict) {
                throw new EvaluationFailedException("String " + Spell.get(source) + " is not a number", e);
            }
            return null;
        }
    }

    private static int getNumericCaseIndex(BigDecimal number, boolean strict) throws EvaluationFailedException {
        try {
            return getNumericCaseIndex(number.intValueExact());
        } catch (ArithmeticException e) {
            if (strict) {
                throw new EvaluationFailedException("Number " + Spell.get(number) + " is not an integer", e);
            }
            return 1;
        }
    }

    private static int getNumericCaseIndex(int number) {
        if (number < 0) {
            number = 0 - number;
        }
        number %= 100;
        if (number >= 5 && number <= 20) { // отдельно обрабатываем "-надцать"
            return 2;
        }
        number %= 10;
        if (number == 1) {
            return 0;
        }
        if (number >= 2 && number <= 4) {
            return 1;
        }
        return 2;
    }

}
