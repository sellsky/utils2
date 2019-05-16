package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;

/**
 * Решает числовой падеж, используется для согласования слов при числительных.
 * Возвращает образец, подходящий под числительное.
 * <p/>
 * Синтаксис:
 * <p/>
 * tag [значение-1] значение-2 значение-5
 * <p/>
 * вместо значений нужно подставить подходящие под указанные числительные слова.
 */
public class NumericCaseModifierFactory implements ValueModifierFactory {
    @Override
    public ValueModifier newModifier() {
        return new NumericCaseModifier();
    }

}
