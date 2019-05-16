package tk.bolovsrol.utils.store;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.io.LineInputStream;
import tk.bolovsrol.utils.io.LineOutputStream;
import tk.bolovsrol.utils.syncro.LockQueuedSynchronizer;
import tk.bolovsrol.utils.syncro.QueuedKey;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Менеджер реализует централизованное сохранение состояния объектов программы.
 * <p/>
 * Объекты, которые должны сохранять своё состояние,
 * нужно зарегистрировать методом {@link #registerAndRestore(String, Storeable)}. В процессе регистрации,
 * если обнаружены сохранённые данные, менеджер передаст управление регистрируемому
 * объекту методом{@link Storeable#restore(LineInputStream)}.
 * <p/>
 * В процессе работы или после завершения работы можно вызвать {@link #forceStoreAll()}, который
 * принудительно сохранит актуальное состояние всех объектов.
 * <p/>
 * Объект, который больше не надо обслуживать, нужно разрегистрировать методом {@link #storeAndUnregister(Storeable)}.
 * В процессе разрегистрации менеджер обратится к разрегистрируемому объекту методом
 * {@link Storeable#store(LineOutputStream)}.
 * <p/>
 * Менеджер делегирует задачи создания потоков отдельному сохранятелю {@link StoreStreamer}.
 */
public class StandardStoreManager implements StoreManager {

    /** Генератор читателей и писателей. */
    private final StoreStreamer storeStreamer;

    /** Хранилище. */
	private final ConcurrentMap<String, Storeable> storeables = new ConcurrentHashMap<>();

    /** Синхронизатор обращений к потокам. */
	private final LockQueuedSynchronizer<String> synchronizer = new LockQueuedSynchronizer<>();

    public StandardStoreManager(StoreStreamer storeStreamer) {
        this.storeStreamer = storeStreamer;
    }

    /**
     * Регистрирует объект, желающий сохранять и восстанавливать
     * своё состояние.
     * <p/>
     * В процессе регистрации, если для объекта существуют сохранённые данные,
     * вызывает {@link Storeable#restore(LineInputStream)}.
     * <p/>
     * Если такой объект под указанным идентификтаором уже зарегистрирован,
	 * возвращается, когда объект восстановлен (если он восстанавливается в момент вызова, ждёт завершения восстановления).
	 * <p/>
	 * В случае возникновения ошибок метод выкидывает {@link RestoreException}.
	 * Переданный <code>storeable</code> в случае ошибки остаётся незарегистрированным.
	 *
	 * @param storeable объект, желающий сохранять своё состояние
     * @throws RestoreException ошибка восстановления состояния
     *                          или указанный идентификатор уже занят другим объектом
     */
    @Override public void registerAndRestore(String id, Storeable storeable) throws RestoreException {
//        validateStoreableId(id);
        Storeable alreadyRegistered = storeables.putIfAbsent(id, storeable);
        if (alreadyRegistered == null) {
            try {
                restore(id, storeable);
            } catch (RestoreException e) {
                // restore failed, pretend we haven't registered storeable
                storeables.remove(id, storeable);
                throw e;
            }
        } else if (alreadyRegistered == storeable) {
			// мы уже зарегистрировали этого клиента и восстановили его либо восстанавливаем сейчас. Дождёмся завершения восстановления и вернёмся как ни в чём не бывало.
			try {
				synchronizer.enter(id).release();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RestoreException("Interrupted while waiting for restore of Storeable id=" + Spell.get(id), e);
			}
			// а если при восстановлении произошла ошибка? проверим ещё раз.
			if (storeables.get(id) != storeable) {
				throw new RestoreException("Failed restoring Storeable id=" + Spell.get(id) + " in another thread");
			}
		} else {
			throw new StoreableAlreadyRegisteredException("Storeable id=" + Spell.get(id) + " already registered with another object.");
		}
	}

    private void restore(String id, Storeable storeable) throws RestoreException {
        try {
            QueuedKey key = synchronizer.enter(id);
            try {
                LineInputStream lis = storeStreamer.newStoreInputStream(id);
                if (lis != null) {
                    try {
                        storeable.restore(lis);
                    } finally {
                        lis.close();
                    }
                }
            } finally {
                key.release();
            }
        } catch (RestoreException e) {
            throw e;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RestoreException("Interrupted while restoring Storeable id=" + Spell.get(id), e);
		} catch (Exception e) {
			throw new RestoreException("Failed restoring Storeable id=" + Spell.get(id), e);
		}
	}

    /** Принудительно записывает информацию всех зарегистрированных {@link Storeable Storeables}. */
    @Override public Map<String, StoreException> forceStoreAll() {
        Map<String, StoreException> fails = null;
        for (Map.Entry<String, Storeable> entry : storeables.entrySet()) {
            try {
                forceStore(entry.getKey(), entry.getValue());
            } catch (StoreException e) {
                if (fails == null) {
					fails = new LinkedHashMap<>();
				}
				//noinspection ThrowableResultOfMethodCallIgnored
				fails.put(entry.getKey(), e);
			}
		}
		return fails;
	}

    private void forceStore(String id, Storeable storeable) throws StoreException {
        try {
            QueuedKey key = synchronizer.enter(id);
            try {
                LineOutputStream los = storeStreamer.newStoreOutputStream(id);
                try {
                    storeable.store(los);
                } finally {
                    los.close();
                }
            } finally {
                key.release();
            }
        } catch (StoreException e) {
            throw e;
        } catch (Exception e) {
            throw new StoreException(e);
        }
    }

    /**
     * Разрегистрирует {@link Storeable}.
     * <p/>
     * В процессе регистрации вызывает {@link Storeable#store(LineOutputStream)}.
     *
     * @param id
     */
    @Override public boolean storeAndUnregister(String id) throws StoreException {
        Storeable storeable = storeables.remove(id);
        if (storeable == null) {
            return false;
        } else {
            forceStore(id, storeable);
            return true;
        }
    }

    /**
     * Разрегистрирует {@link Storeable}.
     * <p/>
     * В процессе регистрации вызывает {@link Storeable#store(LineOutputStream)}.
     *
     * @param storeable
     * @throws StoreException
     */
    @Override public boolean storeAndUnregister(Storeable storeable) throws StoreException {
        Iterator<Map.Entry<String, Storeable>> it = storeables.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Storeable> entry = it.next();
            if (entry.getValue() == storeable) {
                it.remove();
                forceStore(entry.getKey(), storeable);
                return true;
            }
        }
        return false;
    }

	@Override public StoreStreamer<?> getStoreStreamer() {
		return storeStreamer;
	}

}