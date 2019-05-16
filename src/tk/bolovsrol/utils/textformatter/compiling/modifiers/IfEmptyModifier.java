package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

/**
 *
 */
class IfEmptyModifier implements ValueModifier {
    private boolean inverse;

    public IfEmptyModifier(boolean inverse) {
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
        if (inverse ^ source.isEmpty()) {
            return parameters[0].evaluate(strict);
        } else {
            return parameters.length <= 1 ? source : parameters[1].evaluate(strict);
        }
    }

}
