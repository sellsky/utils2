package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

public class LengthModifierFactory implements ValueModifierFactory {
    @Override
    public ValueModifier newModifier() {
        return new ValueModifier() {
            @Override
            public int getMinParameterCount() {
                return 0;
            }

            @Override
            public int getMaxParameterCount() {
                return 0;
            }

            @Override
            public String eval(String source, Section[] parameters, boolean strict) {
                return String.valueOf(source.length());
            }

        };
    }
}
