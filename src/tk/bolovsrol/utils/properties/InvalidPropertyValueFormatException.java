package tk.bolovsrol.utils.properties;

import tk.bolovsrol.utils.PatternCompileException;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.time.TimeUtils;
import tk.bolovsrol.utils.xml.ElementParsingException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Ключ в пропертях есть, но значение содержит какую-то ерунду.
 * <p/>
 * Для публичного использования есть статический метод {@link #getInvalid(String, String, String, String, Throwable)}.
 * <p/>
 * Как сhecked-вариант {@link NumberFormatException} это исключение выбрасывают геттеры {@link ReadOnlyProperties},
 * которым не удаётся преобразовать значение проперти в число.
 */
public class InvalidPropertyValueFormatException extends PropertyException {
    public InvalidPropertyValueFormatException(String propertyName, String message, Throwable cause) {
        super(propertyName, message, cause);
    }

    public static InvalidPropertyValueFormatException getNotANumber(String propertyName, String propertyValue, String propertyIdentity, NumberFormatException cause) {
        return new InvalidPropertyValueFormatException(
                propertyName,
                getPrefix(propertyName, propertyValue, propertyIdentity) + " which is not a number.",
                cause
        );
    }

    public static InvalidPropertyValueFormatException getNotADate(String propertyName, String propertyValue, String propertyIdentity, DateFormat dateFormat, ParseException cause) {
        return new InvalidPropertyValueFormatException(
                propertyName,
                getPrefix(propertyName, propertyValue, propertyIdentity) + " which isn't valid date for "
                        + (dateFormat instanceof SimpleDateFormat ? Spell.get("pattern " + ((SimpleDateFormat) dateFormat).toPattern()) : "date format " + Spell.get(dateFormat)),
                cause
        );
    }

    public static InvalidPropertyValueFormatException getNotADuration(String propertyName, String propertyValue, String propertyIdentity, TimeUtils.DurationParsingException cause) {
        return new InvalidPropertyValueFormatException(
                propertyName,
                getPrefix(propertyName, propertyValue, propertyIdentity) + " which isn't valid Duration.",
                cause
        );
    }

    public static InvalidPropertyValueFormatException getNotAnElement(String propertyName, String propertyValue, String propertyIdentity, ElementParsingException cause) {
        return new InvalidPropertyValueFormatException(
                propertyName,
                getPrefix(propertyName, propertyValue, propertyIdentity) + " which is not valid XML element.",
                cause
        );
    }

    public static <E extends Enum<E>> InvalidPropertyValueFormatException getNotAnEnumValue(String propertyName, String propertyValue, String propertyIdentity, Class<E> cl, IllegalArgumentException cause) {
        return new InvalidPropertyValueFormatException(
                propertyName,
                getPrefix(propertyName, propertyValue, propertyIdentity) + " which is not a valid Еnum for " + Spell.get(cl.getName()) + '.',
                cause
        );
    }

    public static InvalidPropertyValueFormatException getInvalidPattern(String propertyName, String propertyValue, String propertyIdentity, PatternCompileException cause) {
        return new InvalidPropertyValueFormatException(
                propertyName,
                getPrefix(propertyName, propertyValue, propertyIdentity) + " which is not valid regular expression.",
                cause
        );
    }

    /**
     * Генерирует исключение с сообщением «Property propertyName (propertyIdentity) reads value propertyValue which is not whatItShouldBe».
     *
     * @param propertyName           ключ проперти
     * @param propertyValue          считанное значение проперти
     * @param propertyIdentityOrNull сущность проперти (не обязательно)
     * @param whatItShouldBe         допустимые значения
     * @param causeOrNull            причина
     * @return исключение с типичным сообщением
     */
    public static InvalidPropertyValueFormatException getInvalid(String propertyName, String propertyValue, String propertyIdentityOrNull, String whatItShouldBe, Throwable causeOrNull) {
        return new InvalidPropertyValueFormatException(
                propertyName,
                getPrefix(propertyName, propertyValue, propertyIdentityOrNull) + " which is not " + whatItShouldBe + '.',
                causeOrNull
        );
    }

    /**
     * Генерирует универсальный префикс «Property propertyName (propertyIdentity) reads value propertyValue»
     *
     * @param propertyName           ключ проперти
     * @param propertyValue          считанное значение проперти
     * @param propertyIdentityOrNull сущность проперти (не обязательно)
     * @return префикс для диагностического сообщения
     */
    private static String getPrefix(String propertyName, String propertyValue, String propertyIdentityOrNull) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Property ")
                .append(Spell.get(propertyName));
        if (propertyIdentityOrNull != null) {
            sb.append(" (")
                    .append(propertyIdentityOrNull)
                    .append(')');
        }
        sb.append(" reads value ")
                .append(Spell.get(propertyValue));
        return sb.toString();
    }
}