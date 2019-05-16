package tk.bolovsrol.utils.localcache;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.properties.Cfg;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * Простейший кэш, основанный на {@link SoftReference}.
 * <p>
 * В случае, если в кэше интересующего объекта нет,
 * кэш попытается создать его, обратившись к фабрике.
 * <p>
 * Если фабрика вернёт нул, что означает, что объекта
 * с запрошенным идентификатором не существует.
 * Кэш это не запомнит, и при следующем запросе
 * снова обратится к фабрике.
 * <p>
 * Кэш может в произвольное время грохнуть себя целиком или частично,
 * как следствие, разные треды будут пользоваться различными копиями
 * записи с одним и тем же ключом. Фабрика должна обеспечить их идентичность.
 * <p>
 * Если кэшированную запись предполагается модифицировать, необходимо
 * синхронизировать любой доступ к кэшу по ключу записи.
 *
 * @param <I> идентификатор кешируемых объектов
 * @param <O> кешируемый объект
 */
public class LocalCache<I extends Comparable<? super I>, O> {

    private static final Supplier DEFAULT_CONTAINER_SUPPLIER = pick();

    private static <I extends Comparable<? super I>, O> Supplier<LocalCacheStorage<I, O>> pick() {
        String strategy = Cfg.get("localcache.strategy", "empty");
        switch (strategy) {
        case "weak":
            return WeakReferenceLocalCacheStorage::new;
        case "watched":
            return WatchedLocalCacheStorage::new;
        case "empty":
            return EmptyLocalCacheStorage::new;
        default:
            Log.warning("Unexpected localcache.container value " + Spell.get(strategy), ", cache disabled");
            return EmptyLocalCacheStorage::new;
        }
    }

    /** Хранилище данных. */
    private final LocalCacheStorage<I, O> container;

    /** Фабрика новых объектов. */
    private final LocalCacheObjectFactory<I, O> factory;

    @SuppressWarnings("unchecked") public LocalCache(LocalCacheObjectFactory<I, O> factory) {
        this((LocalCacheStorage<I, O>) DEFAULT_CONTAINER_SUPPLIER.get(), factory);
    }

    public LocalCache(LocalCacheStorage<I, O> container, LocalCacheObjectFactory<I, O> factory) {
        this.container = container;
        this.factory = factory;
    }

    public void put(I id, O o) {
        container.put(id, o);
    }

    public void drop(I id) {
        container.remove(id);
    }

    public void dropBunch(Collection<I> ids) {
        container.removeAll(ids);
    }

    public O get(I id) throws ObjectCreationFailedException {
        O o = container.get(id);
        if (o == null) {
            o = factory.newObject(id);
            if (o != null) {
                put(id, o);
            }
        }
        return o;
    }

    /**
     * Возвращает карту объектов для переданных ид.
     * Возвращаемая карта содержит те ключи, для которых существует соответствующий объект.
     * <p>
     * Если в качестве параметра передан нул, вернётся нул. Если передана пустая коллекция, вернётся пустая карта.
     *
     * @param ids интересующие ид
     * @return карта с обнаруженными объектами из списка интересующих ид
     * @throws ObjectCreationFailedException проблема с созданием объекта для одного из интересующих ид
     */
    public Map<I, O> getBunch(Collection<I> ids) throws ObjectCreationFailedException {
        if (ids == null) {
            return null;
        }
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<I, O> result = new TreeMap<>();
        List<I> missedIds = new ArrayList<>(ids.size());
        for (I id : ids) {
            O o = container.get(id);
            if (o == null) {
                missedIds.add(id);
            } else {
                result.put(id, o);
            }
        }
        if (!missedIds.isEmpty()) {
            Map<I, O> ios = createBunch(missedIds);
            for (Map.Entry<I, O> entry : ios.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
            result.putAll(ios);
        }
        return result;
    }

    /**
     * Создаёт пачку объектов. Пытаемся вызвать фабричный создаватель пачки,
     * но если фабрика не поддерживает создание пачки, будем перебирать ид по одному.
     *
     * @param ids
     * @return
     * @throws ObjectCreationFailedException
     */
    private Map<I, O> createBunch(List<I> ids) throws ObjectCreationFailedException {
        Map<I, O> result = factory.newObjects(ids);
        if (result == null) {
            result = new LinkedHashMap<>(ids.size());
            for (I id : ids) {
                O o = factory.newObject(id);
                if (o != null) {
                    result.put(id, o);
                }
            }
        }
        return result;
    }

    /**
     * «Грязный» гет.
     * <p>
     * Пытается возвратить объект по любому из переданных идентификаторов,
     * причём сначала проверяет наличие всех идентификаторов в кэше
     * в порядке, возвращаемом итератором, а затем пытается в том же порядке загрузить их
     * из БД.
     * <p>
     * Если объект создать не удалось, даже если фабрика выкинула исключение,
     * оно будет проигнорировано, будто фабрика вернула нул.
     *
     * @param ids
     * @return найденный объект или нул
     */
    public O getDirty(Collection<I> ids) {
        O o;
        for (I id : ids) {
            o = container.get(id);
            if (o != null) {
                return o;
            }
        }
        for (I id : ids) {
            try {
                o = factory.newObject(id);
                if (o != null) {
                    put(id, o);
                    return o;
                }
            } catch (ObjectCreationFailedException e) {
                // а нам пофиг, у нас грязный гет
            }
        }
        return null;
    }

}