package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;

/**
 * <code>{key after-first &lt;specimen&gt; [&lt;no-specimen-result&gt; [&lt;search-from&gt;]]>}</code>
 * <p/>
 * Возвращает <code>key</code> от первого <code>specimen</code>, не включая его, до конца строки.
 * <p/>
 * Поиски ведёт слева направо с символа <code>search-from</code>, по умолчанию — с начала строки.
 * Если <code>search-from</code> < 0, то отсчёт символов ведётся с конца строки.
 * Если <code>specimen</code> не найден, возвращает <code>no-specimen-result</code>.
 */
public class AfterFirstModifierFactory implements ValueModifierFactory {
    @Override
    public ValueModifier newModifier() {
        return new SubstringByDelimiterModifier(false, true);
    }

}
