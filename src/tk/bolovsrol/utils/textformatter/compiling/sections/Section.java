package tk.bolovsrol.utils.textformatter.compiling.sections;

import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;

/** Секция шаблона. */
public interface Section {

    /**
     * @return true тогда и только тогда, когда результат {@link #evaluate(boolean)}
     *         не зависит от внешних факторов.
     */
    boolean isConstant();

    /**
     * Вычисляет значение секции в соответствии с актуальным вычислятелем.
     *
     * @return значение секции
     */
    String evaluate(boolean strict) throws EvaluationFailedException;

    /** Сбрасывает возможные внутренние кэши секции. */
    void reset();

}
