package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;

/**
 * Отдирает указанные символы от строки.
 * <p/>
 * {trim &lt;chars&gt; [&lt;direction&gt;]},
 * <p/>
 * где chars — набор символов, которые надо откусить,
 * direction — направление откусывания, BOTH (по умолчанию), LEFT или RIGHT.
 *
 * @see tk.bolovsrol.utils.StringUtils#trim(String, tk.bolovsrol.utils.StringUtils.TrimFilter, tk.bolovsrol.utils.StringUtils.TrimMode)
 */
public class TrimModifierFactory implements ValueModifierFactory {
    @Override
    public ValueModifier newModifier() {
        return new TrimModifier();
    }

}
