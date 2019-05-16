package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.containers.EnumContainer;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

/**
 *
 */
class TrimModifier implements ValueModifier {
    private CharsFilter charsFilter;
    private final EnumContainer<StringUtils.TrimMode> trimMode = new EnumContainer<StringUtils.TrimMode>(StringUtils.TrimMode.class);

    private static class CharsFilter implements StringUtils.TrimFilter {
        private final String chars;

        private CharsFilter(String chars) {
            this.chars = chars;
        }

        @Override
        public boolean allowTrim(char ch, String source, int pos, StringUtils.TrimMode direction) {
            return chars.indexOf((int) ch) >= 0;
        }
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
        try {
            CharsFilter charsFilter;
            if (this.charsFilter != null) {
                charsFilter = this.charsFilter;
            } else {
                charsFilter = new CharsFilter(parameters[0].evaluate(strict));
                if (parameters[0].isConstant()) {
                    this.charsFilter = charsFilter;
                }
            }

            StringUtils.TrimMode trimMode;
            if (parameters.length == 1) {
                trimMode = StringUtils.TrimMode.BOTH;
            } else {
                trimMode = this.trimMode.get(parameters[1], strict);
            }
            return StringUtils.trim(source, charsFilter, trimMode);
        } catch (Exception e) {
            if (strict) {
                throw new EvaluationFailedException("Cannot apply trim modifier", e);
            }
            return null;
        }
    }
}
