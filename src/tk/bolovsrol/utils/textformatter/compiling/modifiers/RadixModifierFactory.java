package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.containers.IntContainer;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

import java.math.BigInteger;

/**
 * Преобразует систему счисления переданного числа.
 * {val radix &lt;from&gt; &lt;to&gt;} - из системы from в to.
 */
public class RadixModifierFactory implements ValueModifierFactory {
    @Override
    public ValueModifier newModifier() {
        return new ValueModifier() {

            private final IntContainer from = new IntContainer();
            private final IntContainer to = new IntContainer();

            @Override
            public int getMinParameterCount() {
                return 2;
            }

            @Override
            public int getMaxParameterCount() {
                return 2;
            }

            @Override
            public String eval(String source, Section[] parameters, boolean strict) {
                try {
                    int from = this.from.get(parameters[0], strict);
                    int to = this.to.get(parameters[1], strict);
                    return new BigInteger(source, from).toString(to);
                } catch (Exception ignored) {
                    return null;
                }
            }
        };
    }
}
