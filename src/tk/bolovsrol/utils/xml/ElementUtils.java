package tk.bolovsrol.utils.xml;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Утилиты для {@link Element}, не связанные с неймспейсом.
 */
public final class ElementUtils {

    private ElementUtils() {
    }

    /**
     * Складывает {@link Element} в карту {@link Map}.
     * <p>
     * Ключ составляет из имён вложенных тэгов, разделённых точкой. Читает атрибуты и текстовые данные,
     * сваливает всё в кучу. Дублирующиеся ключи тупо перезаписывает, но на практике это не страшно. Неймспейсы выкидывает.
     * <p>
     * т.е. типа тело:
     * <pre>
     * &lt;ns1:Owner trans_id="wfe234r43"&gt;
     *   &lt;ns1:id&gt;GHJ45jhjg45hJHGJ&lt;/ns1:id&gt;
     * &lt;/ns1:Owner&gt;
     * &lt;ns1:Result&gt;
     *   &lt;ns1:code&gt;0&lt;/ns1:code&gt;
     *   &lt;ns1:comment&gt;Запрос принят в обработку&lt;/ns1:comment&gt;
     * &lt;/ns1:Result&gt;
     * </pre>
     * <p>
     * превращает в набор пропертей, к которым можно по ключам обращаться:
     * <pre>
     * Owner.trans_id → wfe234r43
     * Owner.id → GHJ45jhjg45hJHGJ
     * Result.code → 0
     * Result.comment → "Запрос принят в обработку"
     * </pre>
     *
     * @param element
     * @return
     */
    public static Map<String, String> toMap(Element element) {
        Map<String, String> data = new LinkedHashMap<>();
        toMapInternal(element, "", data);
        return data;
    }

    private static void toMapInternal(Element element, String prefix, Map<String, String> target) {
        String name = element.getName();
        {
            int colonPos = name.indexOf(':');
            if (colonPos >= 0) {
                name = name.substring(colonPos + 1); // избавляемя от неймспейса
            }
        }
        name = prefix + name;
        String namedot = name + '.';

        // атрибуты
        for (Map.Entry<String, String> entry : element.a().dump().entrySet()) {
            target.put(namedot + entry.getKey(), entry.getValue());
        }

        // текстдаты
        String data = element.getFirstTextData();
        if (data != null) {
            target.put(name, data);
        }

        // наследники
        for (Element el : element.getChildren()) {
            if (el.getName() != null) {
                toMapInternal(el, namedot, target);
            }
        }
    }

    public static void fromMap(Element root, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();

            Element el;
            int dotPos = name.indexOf('.');
            if (dotPos < 0) {
                el = root.getOrSpawnFirstChild(name);
            } else {
                Element target = root;
                int nameStart = 0;
                do {
                    target = target.getOrSpawnFirstChild(name.substring(nameStart, dotPos));
                    nameStart = dotPos + 1;
                    dotPos = name.indexOf('.', nameStart);
                } while (dotPos > 0);
                el = target.getOrSpawnFirstChild(name.substring(nameStart));
            }

            el.addTextData(value);
        }
    }

//
//    public static void main(String[] args) {
//        String body = "<subscribe trans_id='8619497004'>\n" +
//              "    <zuka><tailgunner>100500</tailgunner></zuka>\n" +
//              "    <partner_key>15012229</partner_key>\n" +
//              "    <phone>79251552033</phone>\n" +
//              "    <status>prolongOk</status>\n" +
//              "    <n_days>1</n_days>\n" +
//              "    <original_currency>RUB</original_currency>\n" +
//              "    <original_sum>7.0</original_sum>\n" +
//              "    <original_operator_sum>20.0</original_operator_sum>\n" +
//              "    <sum>7.0</sum>\n" +
//              "    <operator_sum>20.0</operator_sum>\n" +
//              "    <hash>04dcb610aa4b52a361381b968fec45e9</hash>\n" +
//              "    <service_code>24video</service_code>\n" +
//              "</subscribe>";
//        try {
//            Map<String, String> map = ElementUtils.toMap(new XmlParser().setTrimWhitespaces(true).parse(body));
//            System.out.println(Spell.get(map));
//            Element tmpEl = new Element("Foo");
//            ElementUtils.fromMap(tmpEl, map);
//            System.out.println(new IndentXmlPrinter().toXmlString(tmpEl.getFirstChild()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
