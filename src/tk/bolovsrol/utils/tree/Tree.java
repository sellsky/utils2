package tk.bolovsrol.utils.tree;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * Дерево без спецификации конфигурации ключей.
 * <p/>
 * Рекомендуется наследовать {@link AbstractTree}
 * и создавать методы со строгим набором параметров,
 * которые в свою очередь должны вызывать {@link AbstractTree#put(Object[], Object)},
 * {@link AbstractTree#get(Object[])} и/или  {@link AbstractTree#get(Object[], boolean, boolean)}.
 *
 * @param <V> класс хранимых значений
 */
public class Tree<V> extends AbstractTree<V> {

    public Tree() {
    }

    @Override public V put(Object[] keySeq, V value) {
        return super.put(keySeq, value);
    }

    @Override public V putIfAbsent(Object[] keySeq, V value) {
        return super.putIfAbsent(keySeq, value);
    }

	@Override public V putIfAbsentAndGet(Object[] keySeq, Supplier<V> valueSupplier) {
		return super.putIfAbsentAndGet(keySeq, valueSupplier);
	}

    @Override public V get(Object[] keySeq) {
        return super.get(keySeq);
    }

    @Override public V get(Object[] keySeq, boolean useNullNodes, boolean pickLowest) {
        return super.get(keySeq, useNullNodes, pickLowest);
    }

    @Override public V remove(Object[] keySeq) {
        return super.remove(keySeq);
    }

    @Override public LinkedHashMap<Object[], V> dump() {
        return super.dump();
    }
}

