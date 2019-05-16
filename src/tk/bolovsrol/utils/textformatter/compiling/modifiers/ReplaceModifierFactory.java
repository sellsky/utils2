package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;

/**
 * Заменяет в исходном значении всё, что попадает под регулярное выражение, указанное первым парамтером, на значение второго параметра.
 */
public class ReplaceModifierFactory implements ValueModifierFactory {
    @Override
    public ReplaceModifier newModifier() {
        return new ReplaceModifier();
    }
}
