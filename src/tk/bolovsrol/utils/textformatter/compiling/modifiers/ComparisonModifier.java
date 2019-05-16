package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

/**
 *
 */
public class ComparisonModifier implements ValueModifier {

    @FunctionalInterface
    public interface Action {
        boolean matches(String a, String b);
    }

    private final Action action;

    public ComparisonModifier(Action action) {
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
        String specimen = parameters[0].evaluate(strict);
        if (action.matches(source, specimen)) {
            return parameters[1].evaluate(strict);
        } else {
            return parameters.length <= 2 ? "" : parameters[2].evaluate(strict);
        }
    }

    public static final ComparisonModifier EQUALS = new ComparisonModifier(String::equals);
    public static final ComparisonModifier NOT_EQUAL = new ComparisonModifier((a, b) -> !a.equals(b));


}
