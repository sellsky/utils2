package tk.bolovsrol.utils.store;

import tk.bolovsrol.utils.conf.AutoConfiguration;
import tk.bolovsrol.utils.io.LineInputStream;
import tk.bolovsrol.utils.io.LineOutputStream;
import tk.bolovsrol.utils.log.LogDome;

import java.io.IOException;
import java.util.Map;

/**
 * Стор-менеджер, который делегирует все задачи переданному в конструкторе стор-менеджеру,
 * добавляя к id заданный префикс.
 * <p>
 * Попытка вызывать {@link #forceStoreAll()} вызовет
 */
public class PrefixStoreManager implements StoreManager {
    private final StoreManager delegate;
    private final String prefix;

    public PrefixStoreManager(StoreManager delegate, String prefix) {
        this.delegate = delegate;
        this.prefix = prefix;
    }

    @Override
    public void registerAndRestore(String id, Storeable storeable) throws IllegalArgumentException, RestoreException {
        delegate.registerAndRestore(prefix + id, storeable);
    }

	/**
	 * Выкидывает {@link UnsupportedOperationException}.
	 *
	 * @return ничего не возвращает.
	 * @throws UnsupportedOperationException всегда
	 */
	@Override public Map<String, StoreException> forceStoreAll() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("PrefixStoreManager cannot save all storeables.");
	}

    @Override public boolean storeAndUnregister(String id) throws StoreException {
        return delegate.storeAndUnregister(prefix + id);
    }

    @Override public boolean storeAndUnregister(Storeable storeable) throws StoreException {
        return delegate.storeAndUnregister(storeable);
    }

	@Override public StoreStreamer<?> getStoreStreamer() {
		return new StoreStreamer<AutoConfiguration>() {
			/**
			 * Выкидывает {@link UnsupportedOperationException}.
			 *
			 * @param log не используется
			 * @param conf не используется
			 * @throws UnsupportedOperationException всегда
			 */
			@Override public void init(LogDome log, AutoConfiguration conf) throws UnsupportedOperationException {
				throw new UnsupportedOperationException("PrefixStoreManager cannot initialize StoreStreamer.");
			}

			@Override public LineOutputStream newStoreOutputStream(String id) throws IOException {
				return delegate.getStoreStreamer().newStoreOutputStream(prefix + id);
			}

			@Override public LineInputStream newStoreInputStream(String id) throws IOException {
				return delegate.getStoreStreamer().newStoreInputStream(prefix + id);
			}
		};
	}
}
