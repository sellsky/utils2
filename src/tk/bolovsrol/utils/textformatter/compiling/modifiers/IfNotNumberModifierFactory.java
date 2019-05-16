package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;

/**
 * Возвращает значение, если source число.
 * <p/>
 * Синтаксис:
 * <p/>
 * tag значение-если-да [значение-если-нет]
 */
public class IfNotNumberModifierFactory implements ValueModifierFactory {
    @Override
    public ValueModifier newModifier() {
        return new IfNumberModifier(true);
    }

}