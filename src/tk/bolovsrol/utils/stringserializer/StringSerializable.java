package tk.bolovsrol.utils.stringserializer;

/**
 * Маркер: объект этого класса можно преобразовать в строку
 * вызовом {@link StringSerializer#serialize(StringSerializable)}, а из этой строки
 * воссоздать исходный объект вызовом {@link StringDeserializer#deserialize(String)}.
 * <p>
 * Механизм ориентирован на классы-контейнеры.
 * <p>
 * Класс, имплементящий этот интерфейс, должен содержать конструктор без параметров.
 * <p>
 * Механизм сериализует и восстанавливает поля без модификаторов static, final и transient.
 * <p>
 * Формат сериализованной строки:
 * <br/><code>
 * &lt;название класса&gt;{@link Const#CLASS_DELIMITER}&lt;поле1&gt;{@link Const#EQ_CHAR}&lt;строковое представление значения поля1&gt;[{@link Const#CONNECTOR_CHAR}&lt;поле2&gt;{@link Const#EQ_CHAR}&lt;строковое представление значения поля1&gt;...]
 * </code><br/>
 * Механизм поддерживает ограниченный перечень типов полей: примитивы, некоторые объекты
 * и массивы этих объектов (не примитивов). Правила преобразования заданы в классе {@link Codec}.
 */
public interface StringSerializable {
}
