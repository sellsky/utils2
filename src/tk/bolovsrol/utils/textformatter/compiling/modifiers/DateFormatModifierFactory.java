package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;

/**
 * Форматирует время (количество миллисекунд) в виде даты.
 * <p/>
 * tag pattern
 * <p/>
 * pattern - шаблон {@link java.text.SimpleDateFormat}.
 * <p/>
 * В режиме durationMode показывают длительность. Фактически, не учитывается time zone.
 */
public class DateFormatModifierFactory implements ValueModifierFactory {
    @Override
    public ValueModifier newModifier() {
        return new SimpleDateFormatModifier(false);
    }

}