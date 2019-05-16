package tk.bolovsrol.utils.textformatter.compiling.modifiers.containers;

import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

/**
 *
 */
public class IntContainer {
    private Integer value;

    public int get(Section section, boolean strict) throws NumberFormatException, EvaluationFailedException {
        if (value != null) {
            return value.intValue();
        }
        int value = Integer.parseInt(section.evaluate(strict));
        if (section.isConstant()) {
            this.value = value;
        }
        return value;
    }
}
