package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.containers.IntContainer;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.containers.StringContainer;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

/**
 * Возвращение позиции ближайшего needle в строке.
 * <p/>
 * tag substring [from]
 * <p/>
 * Поиск происходит справа налево, начиная с позиции from.
 * Если подстрока не найдена, возвращает пустую строку,
 * иначе наибольшую позицию x, с которой начинается искомая подстрока,
 * Если from не указан, то ищет с конца строки.
 * <p/>
 * Если from указывает вне строки, вернётся пустая строка.
 */
public class LastIndexOfModifierFactory implements ValueModifierFactory {
    @Override
    public ValueModifier newModifier() {
        return new ValueModifier() {
            private final StringContainer needle = new StringContainer();
            private final IntContainer from = new IntContainer();

            @Override
            public int getMinParameterCount() {
                return 1;
            }

            @Override
            public int getMaxParameterCount() {
                return 2;
            }

            @Override
            public String eval(String source, Section[] parameters, boolean strict) throws EvaluationFailedException {
                int pos;
                if (parameters.length > 1) {
                    int from = this.from.get(parameters[1], strict);
                    if (from < 0 || from >= source.length()) {
                        return "";
                    }
                    pos = source.lastIndexOf(needle.get(parameters[0], strict), from);
                } else {
                    pos = source.lastIndexOf(needle.get(parameters[0], strict));
                }

                if (pos < 0) {
                    return "";
                } else {
                    return Integer.toString(pos);
                }
            }

        };
    }
}