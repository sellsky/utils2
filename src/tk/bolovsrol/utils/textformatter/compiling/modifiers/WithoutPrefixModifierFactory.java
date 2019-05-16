package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

/**
 * Отрезает префикс.
 * <p/>
 * tag prefix1 [prefix2]...
 * <p/>
 * Если исходная строка начинается одним из префиксов,
 * модификатор возвращает строку без префикса.
 * <p/>
 * Префиксы проверяются в порядке перечисления
 * до первого совпадения.
 * <p/>
 * Если ни один префикс не подошёл, возвращает исходную строку.
 */
public class WithoutPrefixModifierFactory implements ValueModifierFactory {
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
                    if (source.startsWith(val)) {
                        return source.substring(val.length());
                    }
                }
                return source;
            }
        };
    }
}