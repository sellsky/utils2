package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.PatternCompileException;
import tk.bolovsrol.utils.RegexUtils;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

/**
 * Если значение матчится первым параметром, то возвращает результат подстановки второго.
 */
class ReplaceModifier implements ValueModifier {

    @Override
    public int getMinParameterCount() {
        return 2;
    }

    @Override
    public int getMaxParameterCount() {
        return 2;
    }

    @Override
    public String eval(String source, Section[] parameters, boolean strict) throws EvaluationFailedException {
        String patternStr = parameters[0].evaluate(strict);
        try {
            return RegexUtils.getMatcher(patternStr, source).replaceAll(parameters[1].evaluate(strict));
        } catch (PatternCompileException e) {
            if (strict) {
                throw new EvaluationFailedException("Could compile pattern " + Spell.get(patternStr), e);
            }
            return null;
        }
    }

}
