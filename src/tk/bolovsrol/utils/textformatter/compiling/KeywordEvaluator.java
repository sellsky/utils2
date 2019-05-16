package tk.bolovsrol.utils.textformatter.compiling;

/** Вычисляет значения переданных ему ключевых слов. */
@FunctionalInterface public interface KeywordEvaluator {

    /**
     * Вычисляет значение ключевого слова.
     * <p/>
     * Если слово неизвестно или вычислить его невозможно,
     * возвращает нул.
     *
     * @param keyword ключевое слово
     * @return значение ключевого слова или нул
     */
    String evaluate(String keyword);

}
