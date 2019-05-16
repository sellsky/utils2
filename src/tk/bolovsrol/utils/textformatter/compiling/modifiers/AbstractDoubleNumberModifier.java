package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.NumberUtils;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.containers.BigDecimalContainer;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

import java.math.BigDecimal;

/**
 * Четыре арифметических целочисленных действия.
 * <p/>
 * {val1 <действие> val2}
 */
public abstract class AbstractDoubleNumberModifier implements ValueModifier {

    private final BigDecimalContainer val2 = new BigDecimalContainer();

    protected abstract BigDecimal action(BigDecimal val1, BigDecimal val2);

    @Override
    public int getMinParameterCount() {
        return 1;
    }

    @Override
    public int getMaxParameterCount() {
        return 1;
    }

    @Override
    public String eval(String source, Section[] parameters, boolean strict) {
        try {
            return NumberUtils.getString(action(new BigDecimal(source), val2.get(parameters[0], strict)));
        } catch (Exception ignored) {
            return null;
        }
    }
}
