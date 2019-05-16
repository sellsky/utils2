package tk.bolovsrol.utils;

import tk.bolovsrol.utils.syncro.VersionParking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Простая реализация блокирующего таскстека.
 * <p/>
 * Стек блокирует выполнение потока, запрашивающего данные до тех пор,
 * пока данные в стеке не появятся.
 */
public class BlockingStack<E> {

	/** Собственно очередь элементов. */
	private final BlockingQueue<E> stack = new LinkedBlockingQueue<>();

	/** Максимальный размер очереди, который ждут ожидатели сдутия. */
	private final AtomicInteger thresholdSize = new AtomicInteger(-1);
	/** Парковка для ожидателей сдутия. */
	private final VersionParking trimWaiters = new VersionParking();
	/** Парковка для ожидателей пополнения стека: так как Queue не умеет уведомлять о своих изменениях, при пополнениях мы будем обновлять тут версию. */
	private final VersionParking entityWaiters = new VersionParking();

    public BlockingStack() {
    }

	private void checkSize() {
		if (stack.size() < thresholdSize.get()) {
			thresholdSize.set(-1);
			trimWaiters.nextVersion();
		}
	}

	/**
	 * Возвращает управление, когда в стеке оказывается меньше указанного количества элементов.
	 *
	 * @param lessThan
	 * @throws InterruptedException
	 */
	public void waitForTrim(int lessThan) throws InterruptedException {
		int version = trimWaiters.getVersion();
		while (stack.size() >= lessThan) {
			while (true) {
				int current = thresholdSize.get();
				int desired = Math.max(current, lessThan);
				if (desired == current || thresholdSize.compareAndSet(current, desired)) {
					break;
				}
			}
			version = trimWaiters.park(version);
		}
	}

	/**
	 * Добавляет объект в стек на последнее место.
	 * <p>
	 * Метод делает то же, что и {@code pushLast()}.
	 *
	 * @param object добавляемый объект
	 */
	public void add(E object) {
		stack.add(object);
		entityWaiters.nextVersion();
	}

    /**
     * Добавляет объекты в конец стека в прямом порядке.
	 *
	 * @param objects
	 */
	public void addAll(E[] objects) {
		addAll(Arrays.asList(objects));
	}

    /**
     * Добавляет объекты в конец стека в прямом порядке.
	 *
	 * @param objects
	 */
	public void addAll(Collection<? extends E> objects) {
		stack.addAll(objects);
		entityWaiters.nextVersion();
	}

    /**
     * Удаляет первый объект со стека и возвращает его.
	 * <p/>
	 * Если стек пуст, то возвращает нул.
	 *
	 * @return верхний объект или нул.
	 * @throws InterruptedException
	 */
	public E poll() throws InterruptedException {
		E result = stack.poll();
		if (result != null) { checkSize(); }
		return result;
	}

	/**
	 * Удаляет первый объект со стека и возвращает его.
	 * <p>
	 * Если стек пуст, то блокирует выполнение до тех пор,
	 * когда в стеке что-нибудь появится.
	 *
	 * @return верхний объект.
	 * @throws InterruptedException
	 */
	public E take() throws InterruptedException {
		E result = stack.take();
		checkSize();
		return result;
	}

	/**
	 * Удаляет первые limit объектов со стека и возвращает их.
	 * <p>
	 * Если стек пуст, то блокирует выполнение до тех пор,
	 * когда в стек что-нибудь засунут.
	 * <p>
	 * Если limit == 0, то возвращается всё содержимое стека.
	 *
	 * @return объекты
	 * @throws InterruptedException
	 */
	public List<E> take(int limit) throws InterruptedException {
		ArrayList<E> result = limit == 0 ? new ArrayList<>() : new ArrayList<>(limit);
		take(limit, result);
		return result;
	}

	/**
	 * Удаляет первые limit объектов со стека в переданную коллекцию.
	 * <p>
	 * Если стек пуст, то блокирует выполнение до тех пор,
	 * когда в стек что-нибудь засунут.
	 * <p>
	 * Если limit == 0, то возвращается всё содержимое стека.
	 *
	 * @throws InterruptedException
	 */
	public void take(int limit, Collection<E> result) throws InterruptedException {
		int version = entityWaiters.getVersion();
		while (true) {
			int count = limit == 0 ? stack.drainTo(result) : stack.drainTo(result, limit);
			if (count != 0) {
				return;
			}
			version = entityWaiters.park(version);
		}
	}

	/**
	 * Удаляет первые limit объектов со стека и возвращает их.
	 * <p>
	 * Если стек пуст, то возвращает пустой список;
	 * <p>
	 * Если limit == 0, то возвращается всё содержимое стека.
	 *
	 * @return объекты
	 * @throws InterruptedException
	 */
	public List<E> poll(int limit) throws InterruptedException {
		ArrayList<E> result = limit == 0 ? new ArrayList<>() : new ArrayList<>(limit);
		poll(limit, result);
		return result;
	}

	/**
	 * Удаляет первые limit объектов со стека в переданную коллекцию и возвращает true.
	 * <p>
	 * Если стек пуст, то возвращает false;
	 * <p>
	 * Если limit == 0, то возвращается всё содержимое стека.
	 *
	 * @return true, если что-то было снято со стека, иначе false
	 * @throws InterruptedException
	 */
	public boolean poll(int limit, Collection<E> result) throws InterruptedException {
		return (limit == 0 ? stack.drainTo(result) : stack.drainTo(result, limit)) != 0;
	}

	/**
	 * Возвращает количество объектов в стеке в данный момент.
	 *
	 * @return размер стека.
	 */
	public int size() {
		return stack.size();
	}

    /** @return true, если стек на момент проверки пуст, иначе false. */
    public boolean isEmpty() {
        return stack.isEmpty();
    }

}
