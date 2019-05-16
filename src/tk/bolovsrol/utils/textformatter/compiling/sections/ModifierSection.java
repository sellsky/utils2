package tk.bolovsrol.utils.textformatter.compiling.sections;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.InvalidValueModifierException;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;

import java.util.Arrays;

/** Модификатор значения делегированной секции модификатором {@link ValueModifier}. */
public class ModifierSection implements Section {

    private final Section sourceSection;
    private final ValueModifier valueModifier;
    private final Section[] modifierParameters;

    private String value;

    public ModifierSection(Section sourceSection, ValueModifier valueModifier, Section[] modifierParameters) throws InvalidValueModifierException {
        this.sourceSection = sourceSection;
        this.valueModifier = valueModifier;
        this.modifierParameters = modifierParameters;
        check(valueModifier, modifierParameters);
    }

    private static void check(ValueModifier valueModifier, Section[] modifierParameters) throws InvalidValueModifierException {
        int maxParameterCount = valueModifier.getMaxParameterCount();
        int minParameterCount = valueModifier.getMinParameterCount();
        if (maxParameterCount == minParameterCount) {
            if (maxParameterCount >= 0 && modifierParameters.length != minParameterCount) {
                throw new InvalidValueModifierException("Provided " + modifierParameters.length + " parameter(s), expected " + minParameterCount);
            }
        } else {
            if (minParameterCount >= 0 && modifierParameters.length < minParameterCount) {
                throw new InvalidValueModifierException("Provided " + modifierParameters.length + " parameter(s), expected at least " + minParameterCount);
            }
            if (maxParameterCount >= 0 && modifierParameters.length > maxParameterCount) {
                throw new InvalidValueModifierException("Provided " + modifierParameters.length + " parameter(s), expected no more " + minParameterCount);
            }
        }
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public String evaluate(boolean strict) throws EvaluationFailedException {
        if (value == null) {
            String unmodifiedValue = sourceSection.evaluate(strict);
            value = valueModifier.eval(unmodifiedValue, modifierParameters, strict);
            if (value == null) {
                if (strict) {
                    throw new EvaluationFailedException("Cannot modify value " + Spell.get(unmodifiedValue) + " with modifier " + valueModifier);
                }
                return "";
            }
        }
        return value;
    }

    @Override
    public void reset() {
        value = null;
        sourceSection.reset();
        for (Section modifierParameter : modifierParameters) {
            modifierParameter.reset();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModifierSection)) {
            return false;
        }

        ModifierSection that = (ModifierSection) o;

        if (!sourceSection.equals(that.sourceSection)) {
            return false;
        }
        if (!Arrays.equals(modifierParameters, that.modifierParameters)) {
            return false;
        }
        if (!valueModifier.equals(that.valueModifier)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = sourceSection.hashCode();
        result = 31 * result + valueModifier.hashCode();
        result = 31 * result + Arrays.hashCode(modifierParameters);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("modifier[").append(sourceSection.toString()).append(',');
        sb.append(valueModifier.getClass().getSimpleName());
        if (modifierParameters.length > 0) {
            sb.append('{');
            boolean first = true;
            for (Section modifierParameter : modifierParameters) {
                if (!first) {
                    sb.append(',');
                }
                sb.append(modifierParameter.toString());
                first = false;
            }
            sb.append('}');
        }
        sb.append(']');
        return sb.toString();
    }
}
