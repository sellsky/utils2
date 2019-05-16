package tk.bolovsrol.utils;

import java.util.AbstractCollection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Очередь в виде связанного списка произвольного размера. Очередь отсортирована, наименьшие элементы в голове очереди.
 * <p/>
 * В отличие от {@link java.util.PriorityQueue}, итератор этой очереди отдаёт элементы в порядке возрастания.
 * Добавление элемента сделано очень просто в расчёте на то, что будут вставляться упорядоченные данные.
 * Новый элемент сравнивается с последним вставленным, затем с соседним до тех пор, пока не обнаружится подходящее место.
 * В худшем случае вставка элемента закончится после size сравнений.
 * Если в очереди уже есть элементы, равные добавляемому, новый будет добавлен после присутствующих.
 * <p/>
 * По умолчанию используется натуральная сортировка. Элементы в этом случае должны имплементить {@link java.lang.Comparable}.
 * Нулл-значения поддерживаются при использовании соответствующего сравнивателя.
 * <p/>
 * Массовые операции, а также поиск и удаления значения выполняются итерацией всей коллекции.
 *
 * @param <E>
 */
public class SortedLinkedQueue<E> extends AbstractCollection<E> implements Queue<E> {
    /**
     * Хранит полезное значение, а также указывает на соседей. У головы нет предыдущего соседа, у хвоста — следующего.
     *
     * @param <E>
     */
    private static class Item<E> {
        final E e;
        Item<E> prev;
        Item<E> next;

        public Item(E e) {
            this.e = e;
        }

        @Override public String toString() {
            return e.toString();
        }
    }

    /** Чтобы сделать его статическим, без дженериков. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Comparator NATURAL_COMPARATOR = (o1, o2) -> ((Comparable) o1).compareTo(o2);

    /** Размер храним в переменной, чтобы не считать. */
    private int size = 0;
    /** Голова очереди, наименьший элемент. */
    private Item<E> head;
    /** Указатель на последний вставленный элемент, от него будут произведены сравнения при поиске места для следующего элемента. */
    private Item<E> ptr;
    /** Хвост очереди храним для того, чтобы при добавлении элемента, большего чем вся очередь, сразу добавить его в конец. */
    private Item<E> tail;
    /** Сравниватель элементов. */
    private final Comparator<? super E> comparator;

    /**
     * Создаёт очередь, в которой элементы будут упорядочены в соответствии с {@link java.lang.Comparable#compareTo(Object)}.
     * Добавляемые элементы должны имплементить {@link java.lang.Comparable}. Нуллы, соответственно, не разрешены.
     */
    @SuppressWarnings("unchecked") public SortedLinkedQueue() {
        this(NATURAL_COMPARATOR);
    }

    /**
     * Создаёт очередь, в которой элементы будут упорядочены в соответствии с переданным сравнивателем.
     * В очередь можно класть нул-элементы, если сравниватель умеет обращаться с ними.
     *
     * @param comparator
     */
    public SortedLinkedQueue(Comparator<? super E> comparator) {
        this.comparator = comparator;
    }

    @Override public int size() {
        return size;
    }

    @Override public boolean isEmpty() {
        return head == null;
    }

    @Override public void clear() {
        size = 0;
        head = ptr = tail = null;
    }

    private class CrawlingIterator implements Iterator<E> {
        private Item<E> item;

        public CrawlingIterator(Item<E> item) {
            this.item = item;
        }

        @Override public boolean hasNext() {
            return item.next != null;
        }

        @Override public E next() {
            if (item.next == null) {
                throw new NoSuchElementException();
            }
            item = item.next;
            return item.e;
        }

        @Override public void remove() {
            SortedLinkedQueue.this.remove(item);
        }
    }

    @Override public Iterator<E> iterator() {
        Item<E> bazinga = new Item<>(null);
        bazinga.next = head;
        return new CrawlingIterator(bazinga);
    }

    /**
     * Добавляет элемент в подходящее место очереди.
     * То же, что и {@link #offer(Object)}.
     *
     * @param e
     * @return true
     */
    @Override public boolean add(E e) {
        return offer(e);
    }

    /**
     * Добавляет элемент в подходящее место очереди.
     *
     * @param e
     * @return true
     */
    @Override public boolean offer(E e) {
        Item<E> item = new Item<>(e);
        if (size == 0) {
            head = tail = item;
        } else {
            int eToPtrCmp = comparator.compare(e, ptr.e);
            if (eToPtrCmp >= 0) {
                // е не меньше указателя
                // проверим, может, е надо вообще положить после хвоста?
                if (ptr == tail || comparator.compare(e, tail.e) >= 0) {
                    // да, наш элемент не меньше хвоста: делаем новый хвост
                    item.prev = tail;
                    tail.next = item;
                    tail = item;
                } else {
                    // наш элемент где-то между указателем и хвостом
                    // шагаем к хвосту, пока е станет больше
                    while (true) {
                        ptr = ptr.next;
                        if (comparator.compare(e, ptr.e) < 0) {
                            // нашли элемент, который больше нашего, перед ним и положим
                            item.next = ptr;
                            item.prev = ptr.prev;
                            item.prev.next = item;
                            item.next.prev = item;
                            break;
                        }
                    }
                }
            } else {
                // e меньше указателя,
                // проверим, может, е новая голова?
                if (ptr == head || comparator.compare(e, head.e) < 0) {
                    // у нас новая голова
                    item.next = head;
                    head.prev = item;
                    head = item;
                } else {
                    // шагаем к началу, пока не кончится либо пока е станет не больше
                    while (true) {
                        ptr = ptr.prev;
                        if (comparator.compare(e, ptr.e) >= 0) {
                            // нашли элемент, который не меньше нашего, после него и положим
                            item.prev = ptr;
                            item.next = ptr.next;
                            item.prev.next = item;
                            item.next.prev = item;
                            break;
                        }
                    }
                }
            }
        }
        ptr = item;
        size++;
        return true;
    }

    @Override public E peek() {
        return head == null ? null : head.e;
    }


    @Override public E element() throws NoSuchElementException {
        if (head == null) {
            throw new NoSuchElementException("Queue is empty");
        }
        return head.e;
    }

    @Override public E poll() {
        if (head == null) {
            return null;
        }
        E result = head.e;
        removeHead();
        return result;
    }


    @Override public E remove() throws NoSuchElementException {
        if (head == null) {
            throw new NoSuchElementException("Queue is empty");
        }
        E result = head.e;
        removeHead();
        return result;
    }

    private void removeHead() {
        size--;
        if (size == 0) {
            head = ptr = tail = null;
        } else {
            if (ptr == head) {
                ptr = head.next;
            }
            head = head.next;
            // новая голова
            head.prev = null;
        }
    }

    /**
     * Удаляем элемент коллекции. Переданный итем должен находиться в коллекции.
     *
     * @param item
     */
    private void remove(Item<E> item) {
        if (head == item) {
            removeHead();
        } else if (tail == item) {
            size--;
            if (ptr == item) {
                ptr = tail.prev;
            }
            tail = tail.prev;
            // новый хвост
            tail.next = null;
        } else {
            // изымаем элемент из середины
            size--;
            item.next.prev = item.prev;
            item.prev.next = item.next;
            if (ptr == item) {
                ptr = item.next;
            }
        }
    }

    @Override public void forEach(Consumer<? super E> action) {
        Item<E> item = head;
        while (item != null) {
            action.accept(item.e);
            item = item.next;
        }
    }

    @Override public String toString() {
        Item<E> item = head;
        if (head == null) {
            return "";
        }
        StringDumpBuilder sdb = new StringDumpBuilder();
        do {
            sdb.append(Spell.get(item.e));
            item = item.next;
        } while (item != null);
        return sdb.toString();
    }

}
