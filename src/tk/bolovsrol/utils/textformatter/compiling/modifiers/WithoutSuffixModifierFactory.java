package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

/**
 * Отрезает суффикс.
 * <p/>
 * tag suffix1 [suffix2]...
 * <p/>
 * Если исходная строка заканчивается одним из суффиксов,
 * модификатор возвращает строку без суффикса.
 * <p/>
 * Суффиксы проверяются в порядке перечисления
 * до первого совпадения.
 * <p/>
 * Если ни один суффикс не подошёл, возвращает исходную строку.
 */
public class WithoutSuffixModifierFactory implements ValueModifierFactory {
    @Override
    public ValueModifier newModifier() {
        return new ValueModifier() {
            @Override
            public int getMinParameterCount() {
                return 1;
            }

            @Override
            public int getMaxParameterCount() {
                return -1;
            }

            @Override
            public String eval(String source, Section[] parameters, boolean strict) throws EvaluationFailedException {
                for (Section parameter : parameters) {
                    String val = parameter.evaluate(strict);
                    if (source.endsWith(val)) {
                        return source.substring(0, source.length() - val.length());
                    }
                }
                return source;
            }
        };
    }
}