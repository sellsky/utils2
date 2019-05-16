package tk.bolovsrol.utils.xml;

import java.util.Map;

/** Ограниченная функциональность для пространств имён. */
public final class NamespaceUtils {

    private NamespaceUtils() {
    }

    /**
     * Выясняет пространство имён указанного {@code namespaceUri},
     * анализируя атрибуты переданного элемента и его родителей.
     *
     * @param el
     * @param namespaceUri
     * @return префикс или null, если префикс не найден.
     * @see #getNamespacePrefix(Element, String)
     */
    public static String getNamespace(Element el, String namespaceUri) {
        while (true) {
            for (Map.Entry<String, String> attribute : el.attributesMap().entrySet()) {
                if (attribute.getKey().startsWith("xmlns") && attribute.getValue().equals(namespaceUri)) {
                    if (attribute.getKey().length() == "xmlns".length()) {
                        return "";
                    } else {
                        return attribute.getKey().substring("xmlns".length() + 1);
                    }
                }
            }
            if (el.hasParent()) {
                el = el.getParent();
            } else {
                return null;
            }
        }
    }

    /**
     * Выясняет пространство имён указанного {@code namespaceUri},
     * анализируя атрибуты переданного элемента и его родителей.
     * <p/>
     * Если префикс не пуст, то он содержит двоеточие
     * и его можно приклеивать к тэгам как есть.
     *
     * @param el
     * @param namespaceUri
     * @return префикс или null, если префикс не найден.
     * @see #getNamespacePrefix(Element, String)
     */
    public static String getNamespacePrefix(Element el, String namespaceUri) {
        return getNamespacePrefix(getNamespace(el, namespaceUri));
    }

    /**
     * Если {@code namespaceName} нул или пуст, возвращается пустая строка.
     * Если он оканчивается двоеточием, то он возвращается как есть,
     * иначе возвращается {@code namespaceName} с приклеенным к нему двоеточием.
     * Результат работы этого метода можно прицеплять префиксом к тэгам.
     *
     * @param namespaceName название пространства имён
     * @return префикс или null, если префикс не найден.
     * @see #getNamespacePrefix(Element, String)
     */
    public static String getNamespacePrefix(String namespaceName) {
        if (namespaceName == null || namespaceName.length() == 0) {
            return "";
        } else if (namespaceName.endsWith(":")) {
            return namespaceName;
        } else {
            return namespaceName + ':';
        }
    }

    /**
     * Выясняет namespace uri для текущего элемента.
     *
     * @param el исследуемый элемент.
     * @return строка-uri либо нул, если неймспейс не обнаружен.
     */
    public static String getNamespaceUri(Element el) {
        int colonPos = el.getName().indexOf(':');
        String xmlnsAttrName = colonPos < 0 ? "xmlns" : "xmlns:" + el.getName().substring(0, colonPos);

        while (true) {
            String uri = el.a().get(xmlnsAttrName);
            if (uri != null) {
                return uri;
            }
            el = el.getParent();
            if (el == null) {
                return null;
            }
        }
    }

    /**
     * Если переданный {@code namespace} нул или пуст,
     * возввращает {@link NamespaceConst#XMLNS_ATTR},
     * иначе возвращает {@link NamespaceConst#XMLNS_ATTR_PREFIX}
     * с приклеенным к нему {@code namespace}.
     * <p/>
     * Если namespace оканчивается двоеточием, то это двоеточие отрезают.
     *
     * @param namespace пространство
     * @return название атрибута с namespace uri
     */
    public static String getNamespaceAttr(String namespace) {
        if (namespace == null || namespace.length() == 0) {
            return NamespaceConst.XMLNS_ATTR;
        } else if (namespace.endsWith(":")) {
            return NamespaceConst.XMLNS_ATTR_PREFIX + namespace.substring(0, namespace.length() - 1);
        } else {
            return NamespaceConst.XMLNS_ATTR_PREFIX + namespace;
        }
    }

    /**
     * Возвращает ns-префикс названия элемента либо нул, если префикса нет.
     * <p/>
     * Без двоеточия.
     *
     * @param elementName
     * @return
     */
    public static String extractNamespace(String elementName) {
        int colonPos = elementName.indexOf(':');
        if (colonPos < 0) {
            return null;
        } else {
            return elementName.substring(0, colonPos);
        }
    }
}
