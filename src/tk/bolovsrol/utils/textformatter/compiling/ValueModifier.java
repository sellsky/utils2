package tk.bolovsrol.utils.textformatter.compiling;

import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

/** Модификатор значения. */
public interface ValueModifier {

    /** @return минимальное количество требуемых модификатором параметров или -1, если ограничения нет. */
    default int getMinParameterCount() { return -1; }

    /** @return максимальное количество требуемых модификатором параметров или -1, если ограничения нет. */
    default int getMaxParameterCount() { return -1; }

    /**
     * Модифицирует <code>source</code> в соответствии
     * с переданными параметрами <code>parameters</code>.
     * <p/>
     * Количество переданных параметров
     * не меньше {@link #getMinParameterCount()} и не больше {@link #getMaxParameterCount()}.
     * <p/>
     * Если модификация невозможно, возвращает нул.
     *
     * @param source     строка для модификации
     * @param parameters параметры
     * @param strict
     * @return модифицированная строка или нул
     */
    String eval(String source, Section[] parameters, boolean strict) throws EvaluationFailedException;

}
