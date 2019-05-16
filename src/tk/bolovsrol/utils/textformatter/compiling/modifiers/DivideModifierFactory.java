package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;

import java.math.BigDecimal;

public class DivideModifierFactory implements ValueModifierFactory {
    @Override
    public ValueModifier newModifier() {
        return new AbstractDoubleNumberModifier() {
            @Override
            protected BigDecimal action(BigDecimal val1, BigDecimal val2) {
                return val1.divide(val2);
            }
        };
    }
}