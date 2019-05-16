package tk.bolovsrol.utils;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * Класс-костыль для создания TreeSet из изменяющегося конкьюрент-сета за линейное время, подсмотренный
 * <a href="https://stackoverflow.com/questions/35683724/exception-creating-treeset-from-concurrently-modified-concurrentskiplistset#answer-35685713">на StackOverflow</a>.
 *
 * @param <E>
 */
public class IterateOnlySortedSet<E>
    extends AbstractSet<E> implements SortedSet<E> {
    private final ArrayList<E> elements;
    private final Comparator<? super E> comparator;

    public IterateOnlySortedSet(SortedSet<E> source) {
        elements = new ArrayList<>(source);
        comparator = source.comparator();
    }

    @Override
    public Iterator<E> iterator() {
        return elements.iterator();
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    // remaining methods simply throw UnsupportedOperationException

    @NotNull @Override public SortedSet<E> subSet(E fromElement, E toElement) {
        throw new UnsupportedOperationException();
    }

    @NotNull @Override public SortedSet<E> headSet(E toElement) {
        throw new UnsupportedOperationException();
    }

    @NotNull @Override public SortedSet<E> tailSet(E fromElement) {
        throw new UnsupportedOperationException();
    }

    @Override public E first() {
        throw new UnsupportedOperationException();
    }

    @Override public E last() {
        throw new UnsupportedOperationException();
    }
}