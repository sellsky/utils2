package tk.bolovsrol.utils.textformatter.compiling;

import tk.bolovsrol.utils.syncro.Locked;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Обёртка, синхронизирующая обращения к форматирователю-делегату.
 */
public class SynchronizedCompiledFormatter implements CompiledFormatter {
    private final CompiledFormatter delegate;
    private final ReentrantLock lock = new ReentrantLock();

    public SynchronizedCompiledFormatter(CompiledFormatter delegate) {
        this.delegate = delegate;
    }

    @Override public String format(KeywordEvaluator keywordEvaluator) {
        return Locked.call(lock, () -> delegate.format(keywordEvaluator));
    }

    @Override public synchronized String format(KeywordEvaluator keywordEvaluator, boolean strict) throws EvaluationFailedException {
        return Locked.call(lock, () -> delegate.format(keywordEvaluator, strict));
    }
}
