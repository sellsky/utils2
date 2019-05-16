package tk.bolovsrol.utils.textformatter.compiling.modifiers.containers;

import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

import java.math.BigDecimal;

/**
 *
 */
public class BigDecimalContainer {
    private BigDecimal value;

    public BigDecimal get(Section section, boolean strict) throws NumberFormatException, EvaluationFailedException {
        if (value != null) {
            return value;
        }
        BigDecimal value = new BigDecimal(section.evaluate(strict));
        if (section.isConstant()) {
            this.value = value;
        }
        return value;
    }
}
