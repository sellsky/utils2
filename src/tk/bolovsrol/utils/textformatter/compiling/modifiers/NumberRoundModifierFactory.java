package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.containers.IntContainer;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Округляет переданное число до указанного в первом параметре
 * количества десятичных знаков.
 */
public class NumberRoundModifierFactory implements ValueModifierFactory {
    @Override
    public ValueModifier newModifier() {
        return new ValueModifier() {

            private final IntContainer decimalDigitsCount = new IntContainer();

            @Override
            public int getMinParameterCount() {
                return 0;
            }

            @Override
            public int getMaxParameterCount() {
                return 1;
            }

            @Override
            public String eval(String source, Section[] parameters, boolean strict) {
                try {
                    BigDecimal number = new BigDecimal(source);
                    BigDecimal roundedNumber = number.setScale(parameters.length > 0 ? decimalDigitsCount.get(parameters[0], strict) : 0, RoundingMode.HALF_UP);
                    return roundedNumber.toPlainString();
                } catch (Exception ignored) {
                    return null;
                }
            }
        };
    }
}