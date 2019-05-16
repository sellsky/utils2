package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.containers.IntContainer;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

/**
 * Вырезание подстроки.
 * <p/>
 * tag from [to]
 * <p/>
 * Если указано положительное число, то позиция считается с начала строки.
 * Если отрицательное, то позиция считается с конца строки.
 * Если to не указано, то оно принимается за конец строки.
 * <p/>
 * Если from или to задан некорректно, вернётся необкусанная строка целиком.
 */
public class SubstringModifierFactory implements ValueModifierFactory {
    @Override
    public ValueModifier newModifier() {
        return new ValueModifier() {
            private final IntContainer from = new IntContainer();
            private final IntContainer to = new IntContainer();

            @Override
            public int getMinParameterCount() {
                return 1;
            }

            @Override
            public int getMaxParameterCount() {
                return 2;
            }

            @Override
            public String eval(String source, Section[] parameters, boolean strict) {
                int from, to;
                try {
                    from = this.from.get(parameters[0], strict);
                    if (from < 0) {
                        from = source.length() + from;
                        if (from < 0) {
                            from = 0;
                        }
                    }

                    if (parameters.length > 1) {
                        to = this.to.get(parameters[1], strict);
                        if (to < 0) {
                            to = source.length() + to;
                        }
                        if (to > source.length()) {
                            to = source.length();
                        }
                    } else {
                        to = source.length();
                    }
                    if (from >= to) {
                        return "";
                    }
                    return source.substring(from, to);
                } catch (Exception ignored) {
                    return null;
                }
            }

        };
    }
}