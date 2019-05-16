package tk.bolovsrol.utils.textformatter.compiling.modifiers.containers;

import tk.bolovsrol.utils.PatternCompileException;
import tk.bolovsrol.utils.RegexUtils;
import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

import java.util.regex.Pattern;

/**
 *
 */
public class PatternContainer {
    private Pattern value;

    public Pattern get(Section section, boolean strict) throws PatternCompileException, EvaluationFailedException {
        if (value != null) {
            return value;
        }
        Pattern value = RegexUtils.compilePattern(section.evaluate(strict));
        if (section.isConstant()) {
            this.value = value;
        }
        return value;
    }
}
