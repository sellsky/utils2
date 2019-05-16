package tk.bolovsrol.utils.objectpool;

import java.lang.ref.SoftReference;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Пул объектов в софт-рефренсах.
 * <p/>
 * Если объекты какого-нибудь класса и его наследников часто используются, но дороги в конструировании,
 * и их нельзя выполнять несколькими тредами одновременно, можно не создавать каждый раз новый объект,
 * а пользоваться уже существующим. Для хранения свободных объектов и нужен этот пул.
 * <p/>
 * При запросе объекта пул либо вернёт бездельничающий объект, либо, если таких нет, создаст новый.
 * <p/>
 * Если объект от использования испортился, можно его выкинуть. Но если он ещё хорош, после использования
 * его надо вернуть в пул, и следующий нуждающийся в объекте такого класса получит его быстрее.
 * <p/>
 * Использовать предполагается как-то так:
 * <pre>
 * ObjectPool&lt;Foobar&gt; foobarPool = new ObjectPool(...);
 * ...
 * Class&lt;? extends Foobar&gt; foobarSubclass = ...;
 * Foobar foobar = foobarPool.get(foobarSubclass);
 * foobar.init(...);
 * try {
 *     foobar.work(...);
 * } finally {
 *     foobarPool.put(foobar);
 * }
 * </pre>
 */
public class ObjectPool<O> {

    /** Племенной производитель объектов. */
    private final ObjectProducer<O> producer;

    /**
     * Хранит связанные списки кешируемых объектов.
     * <p/>
     * Ключ -- класс.
     */
    private final ConcurrentMap<Class<? extends O>, Queue<SoftReference<? extends O>>> classToInstance = new ConcurrentHashMap<>();

    /**
     * Создаёт инстанцию пула с переданным производителем объектов.
     *
     * @param producer производитель
     */
    public ObjectPool(ObjectProducer<O> producer) {
        this.producer = producer;
    }

    /**
     * Создаёт инстанцию пула с производителем объектов по умолчанию,
     * который создаёт объекты конструктором без параметров.
     */
    public ObjectPool() {
        this(new DefaultObjectProducer<O>());
    }

    /**
     * Отдаёт объект по имени класса.
     * <p/>
     * Если закешированного объекта не нашлось, то вызывает {@link ObjectProducer#produce(Class)}
     * для создания новой инстанции объекта. Если параметр <code>producer</code> равен null,
     * то создаёт новый объект своими силами -- конструктором без параметров.
     * <p/>
     * Объект после использования следует положить (вернуть) в пул методом put().
     *
     * @param className Название интересующего класса
     * @return инстанция класса
     * @throws ObjectProducingException Ошибка при загрузке класса
     */
    @SuppressWarnings({"unchecked"})
    public O get(String className) throws ObjectProducingException {
        try {
            return get((Class<? extends O>) Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new ObjectProducingException(e);
        }
    }

    /**
     * Отдаёт объект класса.
     * <p/>
     * Если закешированного объекта не нашлось, то вызывает {@link ObjectProducer#produce(Class)}
     * для создания новой инстанции объекта. Если параметр <code>producer</code> равен null,
     * то создаёт новый объект своими силами -- конструктором без параметров.
     * <p/>
     * Объект после использования следует положить (вернуть) в пул  методом put().
     *
     * @param objectClass интересующий класс
     * @return инстанция класса
     * @throws ObjectProducingException Ошибка при загрузке класса
     */
    public O get(Class<? extends O> objectClass) throws ObjectProducingException {
        try {
            Queue<SoftReference<? extends O>> instances = retrieveClassInstances(objectClass);
            try {
                while (true) {
                    O result = instances.remove().get();
                    if (result != null) {
//                        if (instances.isEmpty()) {
//                            // тут есть некоторая вероятность, что с тех пор, когда мы проверяли, сюда вернули объект — ну да и чёрт с ним.
//                            classToInstance.remove(objectClass, instances);
//                        }
                        return result;
                    }
                }
            } catch (NoSuchElementException ignored) {
                return producer.produce(objectClass);
            }
        } catch (Exception e) {
            throw new ObjectProducingException(e);
        }
    }

    private Queue<SoftReference<? extends O>> retrieveClassInstances(Class<? extends O> objectClass) {
        Queue<SoftReference<? extends O>> instances = classToInstance.get(objectClass);
        if (instances == null) {
            instances = new ConcurrentLinkedQueue<>();
            Queue<SoftReference<? extends O>> existing = classToInstance.putIfAbsent(objectClass, instances);
            if (existing != null) {
                instances = existing;
            }
        }
        return instances;
    }

    /**
     * Возвращает объект в пул.
     * <p/>
     * Следует возвращать объекты после использования.
     * <p/>
     * Можно передать нул, тогда метод ничего не будет делать.
     *
     * @param object объект или нул
     */
    @SuppressWarnings({"unchecked"})
    public void put(O object) {
        if (object == null) {
            return;
        }
        Class<? extends O> objectClass = (Class<? extends O>) object.getClass();
        retrieveClassInstances(objectClass).add(new SoftReference<>(object));
    }

}