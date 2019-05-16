package tk.bolovsrol.utils.tree;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Дерево, каждый уровень в котором соответствует определённой последовательности ключей.
 * <p/>
 * Специально обрабатываются нул-ключи: в поиске значения в дереве предоставляется возможность
 * использовать узел, хранимый за нул-ключом, как узел по умолчанию в случае отсутствия
 * на данном уровне узла для ключа, равного искомому.
 * <p/>
 * Также есть возможность в поиске использовать значение, наиболее
 * близко расположенное к искомому по направлению к корню дерева.
 * <p/>
 * Рекомендуется наследовать {@link AbstractTree}
 * и создавать методы со строгим набором параметров,
 * которые в свою очередь должны вызывать {@link AbstractTree#put(Object[], Object)},
 * {@link AbstractTree#get(Object[])} и/или  {@link AbstractTree#get(Object[], boolean, boolean)}.
 * <p/>
 * Можно воспользоваться наследником {@link Tree} — в классе тупо открыты публично
 * три вышеупомянутых метода. Но это не рекомендуется.
 *
 * @param <V> класс хранимых значений
 */
public class AbstractTree<V> {

    public static final Object[] EMPTY_ARRAY = new Object[0];
    private final Node<V> root = new Node<>();

    /**
     * Узел дерева.
     *
     * @param <V>
     */
    protected static class Node<V> {
        private V value;
        private Map<Object, Node<V>> branches;
        private Node<V> nullBranch;

        private boolean cleanup() {
            boolean noBranches = true;
            if (nullBranch != null) {
                if (nullBranch.cleanup()) {
                    nullBranch = null;
                } else {
                    noBranches = false;
                }
            }
            if (branches != null) {
                Iterator<Map.Entry<Object, Node<V>>> iterator = branches.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Object, Node<V>> entry = iterator.next();
                    if (entry.getValue().cleanup()) {
                        iterator.remove();
                    }
                }
                if (branches.isEmpty()) {
                    branches = null;
                } else {
                    noBranches = false;
                }
            }
            return noBranches && value == null;
        }

        /**
         * Проверяет, пуста ли нода. Она пуста, нет значения и нет ни одной непустой ветки.
         *
         * @return
         */
        private boolean isEmpty() {
            return value == null && isChildrenEmpty();
        }

        private boolean isChildrenEmpty() {
            if (nullBranch != null && !nullBranch.isEmpty()) {
                return false;
            }
            if (branches != null) {
                for (Node<V> node : branches.values()) {
                    if (!node.isEmpty()) {
                        return false;
                    }
                }
            }
            return true;
        }

        public void dump(Map<Object[], V> target, Object[] keySeq) {
            if (value != null) {
                target.put(keySeq, value);
            }
            if (nullBranch != null) {
                nullBranch.dump(target, getLevelKey(keySeq, null));
            }
            if (branches != null) {
                for (Map.Entry<Object, Node<V>> entry : branches.entrySet()) {
                    entry.getValue().dump(target, getLevelKey(keySeq, entry.getKey()));
                }
            }
        }

        private static Object[] getLevelKey(Object[] upperKey, Object levelValue) {
            Object[] levelKey = Arrays.copyOf(upperKey, upperKey.length + 1);
            levelKey[upperKey.length] = levelValue;
            return levelKey;
        }
    }

    protected AbstractTree() {
    }

    /**
     * Сохраняет значение под указанной последовательностью ключей.
     * <p/>
     * Каждый элемент ключа может быть нулом.
     * <p/>
     * Если значение нул, то соответствующий ключ будет удалён.
     *
     * @param keySeq последовательность ключей
     * @param value
     * @return прежнее значение
     */
    protected V put(Object[] keySeq, V value) {
		return putInternal(keySeq, 0, root, value, null, false);
	}

    /**
     * Сохраняет значение под указанной последовательностью ключей,
     * если в дереве на момент сохранения не сохранено значения
     * по таким же ключам.
     * <p/>
     * Каждый элемент ключа может быть нулом.
     *
     * @param keySeq последовательность ключей
     * @param value
     * @return прежнее значение
     */
    protected V putIfAbsent(Object[] keySeq, V value) {
		return putInternal(keySeq, 0, root, value, null, true);
	}

	/**
	 * Сохраняет значение, возвращаемое supplier-ом, под указанной последовательностью ключей,
	 * если в дереве на момент сохранения не сохранено значения по таким же ключам.
	 * <p>
	 * Каждый элемент ключа может быть нулом.
	 * <p>
	 * К генератору метод обращается только в случае отсутствия ключа, разумеется.
	 *
	 * @param keySeq последовательность ключей
	 * @param valueSupplier генератор значения
	 * @return актуальное значение
	 */
	protected V putIfAbsentAndGet(Object[] keySeq, Supplier<V> valueSupplier) {
		return putInternal(keySeq, 0, root, null, valueSupplier, true);
	}

	@SuppressWarnings({"unchecked"})
	private V putInternal(Object[] keySeq, int level, Node<V> root, V value, Supplier<V> valueSupplier, boolean onlyIfAbsent) {
		Node<V> parent = null;
		while (true) {
            if (level == keySeq.length) {
                V result = root.value;
                if (result == null || !onlyIfAbsent) {
					if (valueSupplier == null) {
						root.value = value;
					} else {
						root.value = valueSupplier.get();
						result = root.value; // тонкость! Если указан valueSupplier, то мы возвращаем не прежнее значение, а актуальное.
					}
					if (root.value == null && parent != null) {
						parent.cleanup();
					}
                }
                return result;
            }
            Node<V> lon;
            Object key = keySeq[level];
            if (key == null) {
                if (root.nullBranch == null) {
                    root.nullBranch = new Node<>();
                }
                lon = root.nullBranch;
            } else if (root.branches == null) {
                root.branches = (Map<Object, Node<V>>) newMap(key.getClass(), level);
                lon = new Node<>();
                root.branches.put(key, lon);
            } else {
                lon = root.branches.get(key);
                if (lon == null) {
                    lon = new Node<>();
                    root.branches.put(key, lon);
                }
            }
            level++;
            parent = root;
            root = lon;
        }
    }

    /**
     * Удаляет ключ по указанным координатам.
     * <p/>
     * То же, что и {@link #put(Object[], Object)} с нулом в качестве второго параметра.
     *
     * @param keySeq
     * @return
     */
    protected V remove(Object[] keySeq) {
        return put(keySeq, null);
    }

    /**
     * Создаёт и возвращает новую пустую карту,
     * используемую для хранения ключей указанного класса.
     * <p/>
     * Хранение нул-ключей от карты не требуется.
     * <p/>
	 * В реализации по умолчанию возвращается {@link HashMap},
	 * созданный конструктором без параметров {@link HashMap()}.
	 *
     * @param keyClass класс ключа
     * @param level    уровень, для которого нужно создать карту
     * @return новая пустая карта для хранения
     */
    protected <K> Map<K, Node<V>> newMap(Class<K> keyClass, int level) {
        return new HashMap<>();
    }

    /**
     * Ищет по дереву значение для указанной последовательности ключей.
     * <p/>
     * То же, что {@link #get(Object[], boolean, boolean) get(keySeq, false, false)}.
     *
     * @param keySeq последовательность ключей
     * @return ассоциированное значение
     */
    protected V get(Object[] keySeq) {
        return get(keySeq, 0, root, false, false);
    }

    /**
     * Ищет по дереву значение, подходящее под указанную последовательность ключей.
     * <p/>
     * Модификаторы поиска (включаются значением true):
     * <dl>
     * <dt><code>useNullNodes</code>:<dd>при отсутствии подходящего узла для указанного значения ключа
     * будет использован нул-узел, если есть;
     * <dt><code>pickAnyLowest</code>:<dd>если точного значения нет, будет возвращено значение,
     * расположенное в узле, наиболее близком к искомому по вертикали.
     * </dl>
     * В ключах можно использовать нулы.
     *
     * @param keySeq       - набор искомых ключей
     * @param useNullNodes - использовать нул-ветки в отсутствие равных или нет
     * @param pickLowest   - возвращать значение наиболее близких ключей по вертикали
     * @return найденное значение или нул
     */
    protected V get(Object[] keySeq, boolean useNullNodes, boolean pickLowest) {
        return get(keySeq, 0, root, useNullNodes, pickLowest);
    }

    private V get(Object[] keys, int level, Node<V> root, boolean goForNulls, boolean pickLowest) {
        if (level == keys.length) {
            return root.value;
        }
        V v;
        Object key = keys[level];
        if (key == null) {
            v = pickNullBranch(keys, level, root, goForNulls, pickLowest);
        } else {
            v = pickEqualBranch(keys, level, root, goForNulls, pickLowest, key);
            if (v == null && goForNulls) {
                v = pickNullBranch(keys, level, root, true, pickLowest);
            }
        }
        return (v == null && pickLowest) ? root.value : v;
    }

    private V pickEqualBranch(Object[] keySeq, int level, Node<V> root, boolean goForNulls, boolean pickAnyLowest, Object key) {
        if (root.branches == null) {
            return null;
        } else {
            Node<V> node = root.branches.get(key);
            if (node == null) {
                return null;
            } else {
                return get(keySeq, level + 1, node, goForNulls, pickAnyLowest);
            }
        }
    }

    private V pickNullBranch(Object[] keySeq, int level, Node<V> root, boolean goForNulls, boolean pickAnyLowest) {
        if (root.nullBranch == null) {
            return null;
        } else {
            return get(keySeq, level + 1, root.nullBranch, goForNulls, pickAnyLowest);
        }
    }

    /**
     * Возвращает содержимое дерева на момент вызова.
     *
     * @return
     */
    protected LinkedHashMap<Object[], V> dump() {
        LinkedHashMap<Object[], V> target = new LinkedHashMap<>();
        root.dump(target, EMPTY_ARRAY);
        return target;
    }

}
