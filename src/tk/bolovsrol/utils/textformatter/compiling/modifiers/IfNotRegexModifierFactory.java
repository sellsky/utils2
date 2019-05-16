package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;

/**
 * Решает совпадение с регулярным выражением.
 * <p/>
 * Сравнение -- ключ сопоставляется с образцом, и при совпадении возвращается значение-равенство,
 * при несовпадении возвращается значение-неравенство.
 * <p/>
 * Синтаксис:
 * <p/>
 * tag паттерн значение-равенство [значение-неравенство]
 * <p/>
 * либо, если установлен флаг inverse (по умолчанию нет),
 * <p/>
 * tag паттерн значение-неравенство [значение-равенство]
 */
public class IfNotRegexModifierFactory implements ValueModifierFactory {
    @Override
    public ValueModifier newModifier() {
        return new IfRegexModifier(true);
    }

}