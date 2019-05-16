package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;

/**
 * Форматирует время (количество миллисекунд) в виде продолжительности.
 * <p/>
 * tag [forceFields]
 * <p/>
 * Общий формат вывода «[[[[д.]чч:]мм:]сс][.µµµ]», квадратные скобки обозначают
 * необязательные поля. Так, если значения всех полей в квадратных скобках 00,
 * такие поля не будут отображаться.
 * <p/>
 * Параметром forceFields можно указать, какие поля нужно отображать
 * даже если их значение ноль:
 * <dl>
 * <dt><code>dhmsz</code></dt><dd>«д.чч:мм:сс.µµµ»;</dd>
 * <dt><code>dhms</code></dt><dd>«д.чч:мм:сс»;</dd>
 * <dt><code>hmsz</code></dt><dd>«чч:мм:сс.µµµ»;</dd>
 * <dt><code>hms</code></dt><dd>«чч:мм:сс»;</dd>
 * <dt><code>msz</code></dt><dd>«мм:сс.µµµ»;</dd>
 * <dt><code>ms</code></dt><dd>«мм:сс»;</dd>
 * <dt><code>sz</code></dt><dd>«сс.µµµ»;</dd>
 * <dt><code>s</code></dt><dd>«сс»;</dd>
 * <dt><code>z</code></dt><dd>«.µµµ».</dd>
 * </dl>
 * <p/>
 * Если указана любая иная последовательность, обязательных полей нет.
 */
public class DurationFormatExModifierFactory implements ValueModifierFactory {
    @Override
    public ValueModifier newModifier() {
        return new DurationModifier();
    }

}