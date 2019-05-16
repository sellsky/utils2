package tk.bolovsrol.utils.store;

import java.util.Map;

/** Диспетчер постоянного хранилища. */
public interface StoreManager {
    /**
     * Регистрирует клиента и загружает его информацию из хранилища.
     * Атомарная операция.
     *
     * @param id идентификатор клиента
     * @param storeable клиент
     * @throws RestoreException регистрация и восстановление не удались
     */
    void registerAndRestore(String id, Storeable storeable) throws RestoreException;

    /**
     * Принудительно сохраняет состояние всех зарегистрированных клиентов.
     *
     * @return карта клиентов, которые не смогли сохранить состояние, или null, если все клиенты сохранили состояние успешно
     */
    Map<String, StoreException> forceStoreAll();

    /**
     * Сохраняет и разрегистрирует клиента, если такой зарегисрирован.
     * Атомарная операция.
     *
     * @param id идентификатор клиента
     * @return true, если клиент был разрегистрирован, false, если клиент не был зарегистрирован
     * @throws StoreException сохранение состояния не удалось
     */
    boolean storeAndUnregister(String id) throws StoreException;

    /**
     * /**
     * Сохраняет и разрегистрирует клиента, если такой зарегисрирован.
     * Атомарная операция.
     *
     * @param storeable клиент
     * @return true, если клиент был разрегистрирован и его состояние сохранено; false, если клиент не был зарегистрирован
     * @throws StoreException клиент был разрегистрирован, но сохранение состояния привело к ошибке
     */
    boolean storeAndUnregister(Storeable storeable) throws StoreException;

	/** @return стример, используемый менеджером в данный момент. */
	StoreStreamer<?> getStoreStreamer();
}
