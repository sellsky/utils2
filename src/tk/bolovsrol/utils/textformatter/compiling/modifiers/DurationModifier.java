package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;
import tk.bolovsrol.utils.time.TimeUtils;

/**
 *
 */
class DurationModifier implements ValueModifier {

    @Override
    public int getMinParameterCount() {
        return 0;
    }

    @Override
    public int getMaxParameterCount() {
        return 1;
    }

    @Override
    public String eval(String source, Section[] parameters, boolean strict) throws EvaluationFailedException {
        try {
            long duration = Long.parseLong(source);
            TimeUtils.ForceFields forceFields = parameters.length > 0 ?
                    TimeUtils.ForceFields.pickByShortcut(parameters[0].evaluate(strict)) :
                    TimeUtils.ForceFields.NOTHING;
            return TimeUtils.formatDuration(duration, forceFields);
        } catch (Exception e) {
            if (strict) {
                throw new EvaluationFailedException("String " + Spell.get(source) + " is not a duration", e);
            }
            return null;
        }
    }

}
