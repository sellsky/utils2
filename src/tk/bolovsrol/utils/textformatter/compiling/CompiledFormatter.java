package tk.bolovsrol.utils.textformatter.compiling;

public interface CompiledFormatter {
    String format(KeywordEvaluator keywordEvaluator);

    String format(KeywordEvaluator keywordEvaluator, boolean strict) throws EvaluationFailedException;
}
