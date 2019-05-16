package tk.bolovsrol.utils.textformatter.compiling.modifiers.containers;

import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

/**
 *
 */
public class StringContainer {
    private String value;

    public String get(Section section, boolean strict) throws NumberFormatException, EvaluationFailedException {
        if (value != null) {
            return value;
        }
        String value = section.evaluate(strict);
        if (section.isConstant()) {
            this.value = value;
        }
        return value;
    }
}
