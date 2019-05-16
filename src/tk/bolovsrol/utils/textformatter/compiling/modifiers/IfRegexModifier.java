package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.PatternCompileException;
import tk.bolovsrol.utils.RegexUtils;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.containers.PatternContainer;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

import java.util.regex.Pattern;

/**
 *
 */
class IfRegexModifier implements ValueModifier {
    private boolean inverse;

    private final PatternContainer specimen = new PatternContainer();

    public IfRegexModifier(boolean inverse) {
        this.inverse = inverse;
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
            Pattern specimen = this.specimen.get(parameters[0], strict);
            if (inverse ^ RegexUtils.matches(specimen, source)) {
                return parameters[1].evaluate(strict);
            } else {
                return parameters.length <= 2 ? "" : parameters[2].evaluate(strict);
            }
        } catch (PatternCompileException e) {
            if (strict) {
                throw new EvaluationFailedException("Could compile regexp from string " + Spell.get(source), e);
            }
            return null;
        }
    }

}
