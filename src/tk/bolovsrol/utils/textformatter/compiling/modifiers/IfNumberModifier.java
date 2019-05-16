package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

import java.math.BigDecimal;

/**
 *
 */
class IfNumberModifier implements ValueModifier {
    private boolean inverse;

    public IfNumberModifier(boolean inverse) {
        this.inverse = inverse;
    }

    @Override
    public int getMinParameterCount() {
        return 1;
    }

    @Override
    public int getMaxParameterCount() {
        return 2;
    }

    @Override
    public String eval(String source, Section[] parameters, boolean strict) throws EvaluationFailedException {
        boolean number;
        try {
            //noinspection ResultOfObjectAllocationIgnored
            new BigDecimal(source);
            number = true;
        } catch (NumberFormatException ignored) {
            number = false;
        }

        if (inverse ^ number) {
            return parameters[0].evaluate(strict);
        } else {
            return parameters.length <= 1 ? "" : parameters[1].evaluate(strict);
        }
    }

}
