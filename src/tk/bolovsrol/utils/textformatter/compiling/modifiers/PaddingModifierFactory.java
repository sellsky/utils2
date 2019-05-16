package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.containers.IntContainer;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

/**
 * Дополняет строку символами справа или слева.
 * <p/>
 * Cинтаксис:
 * <p/>
 * tag направление минимальная_длина наполнитель
 * <p/>
 * Направление задаётся знаком &lt; (слева) или &gt; (справа).
 */
public class PaddingModifierFactory implements ValueModifierFactory {
    @Override
    public ValueModifier newModifier() {
        return new ValueModifier() {

            private StringUtils.Padding padding;
            private final IntContainer minLen = new IntContainer();

            @Override
            public int getMinParameterCount() {
                return 3;
            }

            @Override
            public int getMaxParameterCount() {
                return 3;
            }

            @Override
            public String eval(String source, Section[] parameters, boolean strict) throws EvaluationFailedException {
                StringUtils.Padding padding;
                if (this.padding != null) {
                    padding = this.padding;
                } else {
                    String val0 = parameters[0].evaluate(strict);
                    if (val0.equals("<") || val0.startsWith("l") || val0.startsWith("L")) {
                        padding = StringUtils.Padding.LEFT;
                    } else if (val0.equals(">") || val0.startsWith("r") || val0.startsWith("R")) {
                        padding = StringUtils.Padding.RIGHT;
                    } else {
                        return null;
                    }
                    if (parameters[0].isConstant()) {
                        this.padding = padding;
                    }
                }

                int minLen = this.minLen.get(parameters[1], strict);
                String fill = parameters[2].evaluate(strict);

                return StringUtils.pad(source, minLen, fill, padding);
            }
        };
    }
}
