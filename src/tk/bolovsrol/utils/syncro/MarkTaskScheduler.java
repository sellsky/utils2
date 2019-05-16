package tk.bolovsrol.utils.syncro;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * В шедулер можно добавлять и (затем) изымать произвольные идентификаторы.
 * <p>
 * Для текущего набора идентификаторов можно зарегистрировать задание {@link Runnable}. Оно будет
 * запущено, когда из шедулера будет изъят последний идентификатор из содержавшихся в нём на момент регистрации.
 * <p>
 * Если на момент регистрации ни одного идентификатора не зарегистрировано, попытка регистрации возвратит false.
 * Иначе таск будет зарегистрирован и затем выполнен в треде, который разрегистрирует последний идентификатор
 * из присутствующих на момент регистрации.
 */
public class MarkTaskScheduler<I> {

	private static final class MarkedTask<I> {
		private final Map<I, Object> taskIds;
		private final AtomicReference<Runnable> taskRef;

		public MarkedTask(Map<I, Object> ids, Runnable task) {
			this.taskIds = new ConcurrentHashMap<>(ids.size());
			this.taskIds.putAll(ids);
			this.taskRef = new AtomicReference<>(task);
		}
	}

	/** Подходящего сета нет, поэтому мап, у которого значения используются в качестве флажка нул/не нул. */
	private final Map<I, Object> ids = new ConcurrentHashMap<>();

	/** Очередь с заданиями. Тут всё просто. */
	private final Queue<MarkedTask<I>> tasks = new ConcurrentLinkedQueue<>();

	/**
	 * Добавляет идентификатор в шедулер.
	 *
	 * @param id
	 */
	public void add(I id) {
		ids.put(id, this);
	}

	/**
	 * Удаляет идентификатор из шедулера и разрегистрирует и выполняет задания, для которых этот
	 * идентификатор оказался удалён последним из всех идентификаторов, присутствовавших на момент
	 * регистрации задания.
	 *
	 * @param id
	 */
	public void remove(I id) {
		if (ids.remove(id) != null && !tasks.isEmpty()) {
			Iterator<MarkedTask<I>> iterator = tasks.iterator();
			while (iterator.hasNext()) {
				MarkedTask<I> markedTask = iterator.next();
				if (markedTask.taskIds.remove(id) != null && markedTask.taskIds.isEmpty()) {
					Runnable task = markedTask.taskRef.getAndSet(null);
					if (task != null) {
						iterator.remove();
						task.run();
					}
				}
			}
		}
	}

	/**
	 * Регистрирует задание, которое будет выполнено после того, как из шедулера будут удалены
	 * все идентификаторы, находящиеся в нём в данный момент (т. е. добавленные ранее и ещё
	 * не удалённые на момент регистрации задания).
	 * <p>
	 * Если в шедулере на момент регистрации идентификаторов нет, просто возвратит false.
	 * Если задание добавлено и (будет) выполнено, возвратит true.
	 *
	 * @param task задание
	 * @return true, если задание добавлено и (будет) выполнено, иначе false.
	 */
	public boolean enroll(Runnable task) {
		if (ids.isEmpty()) {
			return false;
		} else {
			tasks.add(new MarkedTask<>(ids, task));
			return true;
		}
	}

}
