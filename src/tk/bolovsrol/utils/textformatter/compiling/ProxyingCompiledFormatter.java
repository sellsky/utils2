package tk.bolovsrol.utils.textformatter.compiling;

import tk.bolovsrol.utils.textformatter.compiling.evaluators.ProxyKeywordEvaluator;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

/**
 * Скомпилированный форматирователь.
 * <p/>
 * Следует создавать компилятором {@link TextFormatCompiler} либо статическим методом {@link #create(String)},
 * который сам вызовет компилятор.
 * <p/>
 * Форматирователь можно использовать параллельно, но только с одним и тем же вычислителем
 * ключевых слов. Иначе результат будет неопределённым.
 */
public class ProxyingCompiledFormatter implements CompiledFormatter {

    private final String template;
    private final Section section;
    private final ProxyKeywordEvaluator proxyKeywordEvaluator;

    /**
     * Создаёт форматирователь секции.
     *
     * @param section корневая секция
     * @param proxyKeywordEvaluator прокси-вычислитель
     */
    ProxyingCompiledFormatter(String template, Section section, ProxyKeywordEvaluator proxyKeywordEvaluator) {
        this.template = template;
        this.section = section;
        this.proxyKeywordEvaluator = proxyKeywordEvaluator;
    }

    /**
     * Устанавливает вычислитель, который будет использоваться при форматировании.
     *
     * @param keywordEvaluator актуальный вычислитель
     * @return this
     */
    public ProxyingCompiledFormatter setKeywordEvaluator(KeywordEvaluator keywordEvaluator) {
        this.proxyKeywordEvaluator.setEvaluator(keywordEvaluator);
        return this;
    }

    /**
     * Устанавливает вычислитель, который будет использоваться при форматировании,
     * и форматирует шаблон с его использованием.
     *
     * @param keywordEvaluator актуальный вычислитель
     * @return отформатированный шаблон
     */
    @Override public String format(KeywordEvaluator keywordEvaluator) {
        setKeywordEvaluator(keywordEvaluator);
        return format();
    }

    /**
     * Устанавливает вычислитель, который будет использоваться при форматировании,
     * и форматирует шаблон с его использованием.
     * <p/>
     * В строгом режиме при несоответствии шаблона ключевым словам (отсутствие либо неверный формат)
     * выкинет {@link EvaluationFailedException}.
     *
     * @param keywordEvaluator вычислитель ключевых слов для использования
     * @param strict строгий режим
     * @return отформатированный шаблон
     * @throws EvaluationFailedException только в строгом режиме: шаблон не соответствует ключевым словам
     */
    @Override public String format(KeywordEvaluator keywordEvaluator, boolean strict) throws EvaluationFailedException {
        setKeywordEvaluator(keywordEvaluator);
        return format(strict);
    }

    /**
     * Форматирует шаблон при помощи установленного вычислителя.
     * <p/>
     * Изначально используется пустой вычислитель.
     *
     * @return форматированный шаблон
     */
    public String format() {
        try {
            return format(false);
        } catch (EvaluationFailedException e) {
            throw new RuntimeException("Evaluation exception thrown in non-strict mode, this shouldn't had happen.", e);
        }
    }

    /**
     * Форматирует шаблон при помощи установленного вычислителя.
     * <p/>
     * Изначально используется пустой вычислитель.
     * <p/>
     * В строгом режиме при несоответствии шаблона ключевым словам (отсутствие либо неверный формат)
     * выкинет {@link EvaluationFailedException}.
     *
     * @param strict строгий режим
     * @return отформатированный шаблон
     * @throws EvaluationFailedException только в строгом режиме: шаблон не соответствует ключевым словам
     */
    public String format(boolean strict) throws EvaluationFailedException {
        section.reset();
        return section.evaluate(strict);
    }

    @Override public String toString() {
        return template;
    }

    /**
     * Простая версия, создание форматирователя
     * {@link TextFormatCompiler компилятором} со стандартными настройками.
     *
     * @param template шаблон
     * @return форматирователь
     * @throws InvalidTemplateException плохой шаблон
     * @see TextFormatCompiler#compile(String)
     */
    public static ProxyingCompiledFormatter create(String template) throws InvalidTemplateException {
        return new TextFormatCompiler().compile(template);
    }

}
