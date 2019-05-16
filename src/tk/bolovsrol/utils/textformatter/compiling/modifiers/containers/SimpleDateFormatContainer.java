package tk.bolovsrol.utils.textformatter.compiling.modifiers.containers;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 *
 */
public class SimpleDateFormatContainer {
    private SimpleDateFormat value;
    private final boolean durationMode;

    public SimpleDateFormatContainer(boolean durationMode) {
        this.durationMode = durationMode;
    }

    public SimpleDateFormat get(Section section, boolean strict) throws EvaluationFailedException {
        if (value != null) {
            return value;
        }
        String pattern = section.evaluate(strict);
        SimpleDateFormat value;
        try {
            value = new SimpleDateFormat(pattern);
        } catch (Exception e) {
            if (strict) {
                throw new EvaluationFailedException("Error compiling SDF with pattern " + Spell.get(pattern), e);
            } else {
                return null;
            }
        }
        if (durationMode) {
            value.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        if (section.isConstant()) {
            this.value = value;
        }
        return value;
    }
}
