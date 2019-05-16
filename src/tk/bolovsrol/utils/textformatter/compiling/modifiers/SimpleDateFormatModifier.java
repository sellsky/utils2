package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.containers.SimpleDateFormatContainer;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
class SimpleDateFormatModifier implements ValueModifier {

    private final SimpleDateFormatContainer sdf;

    public SimpleDateFormatModifier(boolean durationMode) {
        sdf = new SimpleDateFormatContainer(durationMode);
    }

    @Override
    public int getMinParameterCount() {
        return 1;
    }

    @Override
    public int getMaxParameterCount() {
        return 1;
    }

    @Override
    public String eval(String source, Section[] parameters, boolean strict) throws EvaluationFailedException {
        Date date;
        try {
            date = new Date(Long.parseLong(source));
        } catch (Exception e) {
            if (strict) {
                throw new EvaluationFailedException("Cannot parse " + Spell.get(source) + "  as number", e);
            } else {
                return null;
            }
        }
        SimpleDateFormat sdf = this.sdf.get(parameters[0], strict);
        try {
            return sdf.format(date);
        } catch (Exception e) {
            if (strict) {
                throw new EvaluationFailedException("Cannot format " + Spell.get(source) + " with pattern " + Spell.get(sdf) + " as date ", e);
            } else {
                return null;
            }
        }
    }

}
