package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

import java.math.BigInteger;

/** Преобразует шестнадцатеричное число в десятичное, без параметров. */
public class Hex2DecModifierFactory implements ValueModifierFactory {
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
                try {
                    return new BigInteger(source, 16).toString();
                } catch (Exception ignored) {
                    return null;
                }
            }
        };
    }
}