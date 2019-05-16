package tk.bolovsrol.utils.log.providers;

import tk.bolovsrol.utils.Spell;

/** Содержит и отдаёт stdout и stderr. */
public class StandardWriterProviderPool implements LogWriterProviderPool {

	private static final class StdOutContainer {
		static final LogWriterProvider PROVIDER = new SimpleWriterProvider("stdout", System.out);

        private StdOutContainer() {
        }
    }

	private static final class StdErrContainer {
		static final LogWriterProvider PROVIDER = new SimpleWriterProvider("stderr", System.err);

        private StdErrContainer() {
        }
    }

    @Override public LogWriterProvider retrieve(String streamName) throws StreamProviderException {
        if ("stdout".equals(streamName)) {
            return StdOutContainer.PROVIDER;
        }
        if ("stderr".equals(streamName)) {
            return StdErrContainer.PROVIDER;
        }
        throw new StreamProviderException("Unexpected stream name " + Spell.get(streamName));
    }
}
