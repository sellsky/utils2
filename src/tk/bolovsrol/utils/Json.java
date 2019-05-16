package tk.bolovsrol.utils;

import tk.bolovsrol.utils.box.Box;
import tk.bolovsrol.utils.function.ThrowingFunction;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.properties.sources.ReadOnlySource;
import tk.bolovsrol.utils.properties.sources.SourceUnavailableException;
import tk.bolovsrol.utils.time.TimeUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * Простенькая реализация JSON-объекта, представляющего собой рекурсивный контейнер.
 * В контейнере содержится значение одного из возможных типов:
 * <ul>
 * <li>строковое значение как String,
 * <li>числовое значение как BigDecimal,
 * <li>булево значение как Boolean,
 * <li>двоичное значение как byte[] (хоть двоичные данные в стандарте не описаны, но так удобней, учитывая де-факто-стандарт кодирования двоичных данных в Base64),
 * <li>«джсон-объект» как LinkedHashMap&lt;String, Json&gt; (имя → Json) и
 * <li>«джсон-массив» как ArrayList&lt;Json&gt;.
 * </ul>
 * <p>
 * Геттеры простых типов (строка, число, буль и массив байтов) по возможности прозрачно приводят хранимое простое значение к нужному типу.
 * Если привести значение не удалось, вываливается соответствующее исключение. Если контейнер хранит значение иного типа —
 * вываливается исключение {@link JsonValueOfOtherTypeException}.
 * <p>
 * Для сложных типов (джсон-объект и джсон-массив) предусмотрены всякие удобные шорткаты.
 * <p>
 * Методы  <code>set*</code> и <code>add*</code>, возвращают текущий объект.
 * Методы <code>new*</code> возвращают новый созданный объект.
 * <p>
 * Хитрость: точки в имени джсон-объекта трактуются как уровни вложенности.
 * Например, для объекта {@code {"foo":{"bar":"azazaza"}}}
 * вызов {@code getObjectItem("foo.bar")} вернёт объект, содержащий "azazaza".
 * <p>
 * Джсон показывает своё содержимое как {@link ReadOnlySource}. Значения простых типов (строка, число, буль и массив байтов) доступны
 * преобразованные в строку, а дерево именованных объектов отражается в пропертях составными именами, склеенными из составляющих простых имён через точку,
 * а массив виден будто именованный объект с ключами, равными индексу (десятичному).
 *
 * @see #INDENT
 */
@SuppressWarnings("unchecked") public class Json implements ReadOnlySource {

    /** Разделитель составных имён. */
    public static final char COMPLEX_DELIMITER = '.';

    /**
     * Размер отступа в пробелах для одного уровня вложенных объектов при форматировании джсона.
     * Если тут 0 (по умолчанию), объект будет вывведен встык, как можно компактнее, если больше нуля —
     * аккуратно разбит на строки с отступом (INDENT × уровень вложенности), очень человекочитаемо.
     * <p>
     * Начальное значение берётся из параметра <code>debug.json.indent</code> основного конфига.
     */
    public static int INDENT = Cfg.getInteger("debug.json.indent", 0, Log.getInstance());

    /** Тип хранимого значения. */
    private Type type = Type.NULL;

    /** Хранимое значение. */
    private Object value;

    enum Type {
        NULL {
            @Override String getStringValue(Json j) { return null;}

            @Override <E extends Enum<E>> E getEnumValue(Class<E> enumType, Json j) { return null;}

            @Override BigDecimal getBigDecimalValue(Json j) { return null;}

            @Override Integer getIntegerValue(Json j) { return null;}

            @Override Long getLongValue(Json j) { return null;}

            @Override Instant getInstantValue(Json j) { return null;}

            @Override Date getDateValue(Json j) { return null;}

            @Override Boolean getBooleanValue(Json j) { return null;}

            @Override byte[] getBinaryValue(Json j) { return null;}

            @Override Map<String, Json> getObjectValue(Json j) { return null;}

            @Override List<Json> getArrayValue(Json j) { return null;}

            @Override void appendToString(StringBuilder sb, int indent, int level, Json j) { sb.append("null"); }
        },
        STRING {
            @Override boolean isSimple() { return true;}

            @Override String getStringValue(Json j) { return (String) j.value;}

            @Override <E extends Enum<E>> E getEnumValue(Class<E> enumType, Json j) throws JsonValueOfOtherTypeException { try { return Enum.valueOf(enumType, (String) j.value);} catch (Exception e) { throw new JsonValueOfOtherTypeException(j, e); } }

            @Override BigDecimal getBigDecimalValue(Json j) throws JsonValueOfOtherTypeException { try { return NumberUtils.parseBigDecimal((String) j.value); } catch (Exception e) { throw new JsonValueOfOtherTypeException(j, e); } }

            @Override Integer getIntegerValue(Json j) throws JsonValueOfOtherTypeException { try { return NumberUtils.parseInteger((String) j.value); } catch (Exception e) { throw new JsonValueOfOtherTypeException(j, e); } }

            @Override Long getLongValue(Json j) throws JsonValueOfOtherTypeException { try { return NumberUtils.parseLong((String) j.value); } catch (Exception e) { throw new JsonValueOfOtherTypeException(j, e); } }

            @Override Instant getInstantValue(Json j) throws JsonValueOfOtherTypeException { try { return TimeUtils.parseIsoInstant((String) j.value); } catch (Exception e) { throw new JsonValueOfOtherTypeException(j, e); } }

            @Override Date getDateValue(Json j) throws JsonValueOfOtherTypeException { try { return TimeUtils.parseIsoDate((String) j.value); } catch (Exception e) { throw new JsonValueOfOtherTypeException(j, e); } }

            @Override Boolean getBooleanValue(Json j) throws JsonValueOfOtherTypeException { try { return BooleanUtils.parse((String) j.value); } catch (Exception e) { throw new JsonValueOfOtherTypeException(j, e); } }

            @Override byte[] getBinaryValue(Json j) throws JsonValueOfOtherTypeException { try { return Base64.getDecoder().decode((String) j.value);} catch (Exception e) { throw new JsonValueOfOtherTypeException(j, e); } }

            @Override void appendToString(StringBuilder sb, int indent, int level, Json j) { sb.append('"').append(escapeString((String) j.value)).append('"'); }
        },
        ENUM {
            @Override boolean isSimple() { return true;}

            @Override String getStringValue(Json j) throws JsonValueOfOtherTypeException {
                return ((Enum) j.value).name();
            }

            @Override <E extends Enum<E>> E getEnumValue(Class<E> enumType, Json j) throws JsonValueOfOtherTypeException {
                // если класс соответствует, то отлично, вернём значение, а иначе попробуем найти подходящее в запрошенном енуме, хехе. 
                try {
                    return j.value.getClass() == enumType ? (E) j.value : Enum.valueOf(enumType, ((Enum) j.value).name());
                } catch (Exception e) {
                    throw new JsonValueOfOtherTypeException(j, e);
                }
            }

            @Override void appendToString(StringBuilder sb, int indent, int level, Json j) {
                sb.append('"').append(((Enum) j.value).name()).append('"');
            }
        },
        BIG_DECIMAL {
            @Override boolean isSimple() { return true;}

            @Override boolean isDecimal() { return true;}

            @Override boolean isNumber() { return true;}

            @Override String getStringValue(Json j) { return ((BigDecimal) j.value).toPlainString();}

            @Override BigDecimal getBigDecimalValue(Json j) { return (BigDecimal) j.value;}

            @Override Integer getIntegerValue(Json j) throws JsonValueOfOtherTypeException { try { return Integer.valueOf(((BigDecimal) j.value).intValueExact()); } catch (Exception e) { throw new JsonValueOfOtherTypeException(j, e); } }

            @Override Long getLongValue(Json j) throws JsonValueOfOtherTypeException { try { return Long.valueOf(((BigDecimal) j.value).longValueExact()); } catch (Exception e) { throw new JsonValueOfOtherTypeException(j, e); } }

            @Override Instant getInstantValue(Json j) throws JsonValueOfOtherTypeException { try { return Instant.ofEpochMilli(((BigDecimal) j.value).longValueExact()); } catch (Exception e) { throw new JsonValueOfOtherTypeException(j, e); } }

            @Override Date getDateValue(Json j) throws JsonValueOfOtherTypeException { try { return new Date(((BigDecimal) j.value).longValueExact()); } catch (Exception e) { throw new JsonValueOfOtherTypeException(j, e); } }

            @Override Boolean getBooleanValue(Json j) { return ((BigDecimal) j.value).signum() == 0 ? Boolean.FALSE : Boolean.TRUE; }

            @Override void appendToString(StringBuilder sb, int indent, int level, Json j) { sb.append(((BigDecimal) j.value).toPlainString()); }
        },
        INTEGER {
            @Override boolean isSimple() { return true;}

            @Override boolean isInteger() { return true;}

            @Override boolean isNumber() { return true;}

            @Override String getStringValue(Json j) { return j.value.toString(); }

            @Override BigDecimal getBigDecimalValue(Json j) { return BigDecimal.valueOf(((Integer) j.value).longValue()); }

            @Override Integer getIntegerValue(Json j) { return (Integer) j.value; }

            @Override Long getLongValue(Json j) { return Long.valueOf(((Integer) j.value).longValue()); }

            @Override Boolean getBooleanValue(Json j) { return ((Integer) j.value).intValue() == 0 ? Boolean.FALSE : Boolean.TRUE; }

            @Override void appendToString(StringBuilder sb, int indent, int level, Json j) { sb.append(j.value.toString()); }
        },
        LONG {
            @Override boolean isSimple() { return true;}

            @Override boolean isInteger() { return true;}

            @Override boolean isNumber() { return true;}

            @Override String getStringValue(Json j) throws JsonValueOfOtherTypeException { return j.value.toString(); }

            @Override BigDecimal getBigDecimalValue(Json j) { return BigDecimal.valueOf(((Long) j.value).longValue()); }

            @Override Integer getIntegerValue(Json j) throws JsonValueOfOtherTypeException { try { return Integer.valueOf(((Long) j.value).intValue()); } catch (Exception e) { throw new JsonValueOfOtherTypeException(j, e); } }

            @Override Long getLongValue(Json j) { return (Long) j.value; }

            @Override Instant getInstantValue(Json j) { return Instant.ofEpochMilli(((Long) j.value).longValue()); }

            @Override Date getDateValue(Json j) { return new Date(((Long) j.value).longValue()); }

            @Override Boolean getBooleanValue(Json j) { return ((Long) j.value).longValue() == 0L ? Boolean.FALSE : Boolean.TRUE; }

            @Override void appendToString(StringBuilder sb, int indent, int level, Json j) { sb.append(j.value.toString()); }
        },
        INSTANT {
            @Override boolean isSimple() { return true; }

            @Override boolean isDate() { return true; }

            @Override String getStringValue(Json j) { return TimeUtils.printIso((Instant) j.value); }

            @Override BigDecimal getBigDecimalValue(Json j) { return BigDecimal.valueOf(((Instant) j.value).toEpochMilli()); }

            @Override Long getLongValue(Json j) { return Long.valueOf(((Instant) j.value).toEpochMilli()); }

            @Override Instant getInstantValue(Json j) { return (Instant) j.value; }

            @Override Date getDateValue(Json j) { return Date.from(((Instant) j.value)); }

            @Override void appendToString(StringBuilder sb, int indent, int level, Json j) { sb.append('"').append(TimeUtils.printIso(((Instant) j.value))).append('"'); }
        },
        DATE {
            @Override boolean isSimple() { return true; }

            @Override boolean isDate() { return true; }

            @Override String getStringValue(Json j) { return TimeUtils.printIso((Date) j.value); }

            @Override BigDecimal getBigDecimalValue(Json j) { return BigDecimal.valueOf(((Date) j.value).getTime()); }

            @Override Long getLongValue(Json j) { return Long.valueOf(((Date) j.value).getTime()); }

            @Override Instant getInstantValue(Json j) { return ((Date) j.value).toInstant(); }

            @Override Date getDateValue(Json j) { return (Date) j.value; }

            @Override void appendToString(StringBuilder sb, int indent, int level, Json j) { sb.append('"').append(TimeUtils.printIso(((Date) j.value))).append('"'); }
        },
        BOOLEAN {
            @Override boolean isSimple() { return true; }

            @Override String getStringValue(Json j) { return Boolean.toString(((Boolean) j.value).booleanValue()); }

            @Override BigDecimal getBigDecimalValue(Json j) { return ((Boolean) j.value).booleanValue() ? BigDecimal.ONE : BigDecimal.ZERO; }

            @Override Integer getIntegerValue(Json j) { return ((Boolean) j.value).booleanValue() ? Integer.valueOf(1) : Integer.valueOf(0); }

            @Override Long getLongValue(Json j) { return ((Boolean) j.value).booleanValue() ? Long.valueOf(1L) : Long.valueOf(0L); }

            @Override Boolean getBooleanValue(Json j) { return (Boolean) j.value; }

            @Override void appendToString(StringBuilder sb, int indent, int level, Json j) { sb.append(Boolean.toString(((Boolean) j.value).booleanValue())); }
        },
        BINARY {
            @Override boolean isSimple() { return true; }

            @Override String getStringValue(Json j) { return Base64.getEncoder().encodeToString(getBinaryValue(j)); }

            @Override byte[] getBinaryValue(Json j) { return (byte[]) j.value; }

            @Override void appendToString(StringBuilder sb, int indent, int level, Json j) {
                sb.append('"').append(Base64.getEncoder().encodeToString(getBinaryValue(j))).append('"');
            }
        },
        OBJECT {
            @Override boolean isObject() { return true; }

            @Override Map<String, Json> getObjectValue(Json j) { return (Map<String, Json>) j.value; }

            @Override Object copyValueFrom(Json j) {
                Map<String, Json> thatObjectValue = getObjectValue(j);
                Map<String, Json> thisObjectValue = new LinkedHashMap<>(thatObjectValue.size());
                for (Map.Entry<String, Json> entry : thatObjectValue.entrySet()) {
                    thisObjectValue.put(entry.getKey(), new Json(entry.getValue()));
                }
                return thisObjectValue;
            }

            @Override void appendToString(StringBuilder sb, int indent, int level, Json j) {
                sb.append('{');
                Map<String, Json> objectValue = getObjectValue(j);
                if (!objectValue.isEmpty()) {
                    level++;
                    Iterator<Map.Entry<String, Json>> iterator = objectValue.entrySet().iterator();
                    Map.Entry<String, Json> entry = iterator.next();
                    appendIndent(sb, indent, level);
                    sb.append('"').append(entry.getKey()).append('"').append(':');
                    if (entry.getValue() == null) { sb.append("null"); } else { entry.getValue().appendToString(sb, indent, level); }
                    while (iterator.hasNext()) {
                        sb.append(',');
                        entry = iterator.next();
                        appendIndent(sb, indent, level);
                        sb.append('"').append(entry.getKey()).append('"').append(':');
                        if (entry.getValue() == null) { sb.append("null"); } else { entry.getValue().appendToString(sb, indent, level); }
                    }
                    appendIndent(sb, indent, level - 1);
                }
                sb.append('}');
            }
        },
        ARRAY {
            @Override boolean isArray() { return true; }

            @Override List<Json> getArrayValue(Json j) { return (List<Json>) j.value; }

            @Override Object copyValueFrom(Json j) {
                List<Json> thatArrayValue = getArrayValue(j);
                List<Json> thisArrayValue = new ArrayList<>(thatArrayValue.size());
                for (Json that : thatArrayValue) {
                    thisArrayValue.add(new Json(that));
                }
                return thisArrayValue;
            }

            @Override void appendToString(StringBuilder sb, int indent, int level, Json j) {
                sb.append('[');
                List<Json> arrayValue = getArrayValue(j);
                if (!arrayValue.isEmpty()) {
                    level++;
                    Iterator<Json> iterator = arrayValue.iterator();
                    Json value = iterator.next();
                    appendIndent(sb, indent, level);
                    if (value == null) { sb.append("null"); } else { value.appendToString(sb, indent, level); }
                    while (iterator.hasNext()) {
                        sb.append(',');
                        value = iterator.next();
                        appendIndent(sb, indent, level);
                        if (value == null) { sb.append("null"); } else { value.appendToString(sb, indent, level); }
                    }
                    appendIndent(sb, indent, level - 1);
                }
                sb.append(']');
            }
        };

        /** @return джсон содержит простое нерекурсивное значение (не джсон-объект и не джсон-массив). */
        boolean isSimple() { return false; }

        /** @return джсон содержит явно установленное дробное числовое значение джсона {@link BigDecimal}. */
        boolean isDecimal() { return false; }

        /** @return джсон содержит явно установленное целое числовое значение джсона {@link Integer} или {@link Long}. */
        boolean isInteger() { return false; }

        /** @return джсон содержит явно установленное числовое значение джсона {@link BigDecimal}, {@link Integer} или {@link Long}. */
        boolean isNumber() { return false; }

        /** @return джсон содержит явно установленную дату — {@link Date} или {@link Instant}. */
        boolean isDate() { return false; }

        /** @return джсон содержит джсон-объект. */
        boolean isObject() { return false; }

        /** @return джсон содержит джсон-массив. */
        boolean isArray() { return false; }

        String getStringValue(Json j) throws JsonValueOfOtherTypeException { throw new JsonValueOfOtherTypeException(j); }

        <E extends Enum<E>> E getEnumValue(Class<E> enumType, Json j) throws JsonValueOfOtherTypeException { throw new JsonValueOfOtherTypeException(j); }

        BigDecimal getBigDecimalValue(Json j) throws JsonValueOfOtherTypeException { throw new JsonValueOfOtherTypeException(j); }

        Integer getIntegerValue(Json j) throws JsonValueOfOtherTypeException { throw new JsonValueOfOtherTypeException(j); }

        Long getLongValue(Json j) throws JsonValueOfOtherTypeException { throw new JsonValueOfOtherTypeException(j); }

        Instant getInstantValue(Json j) throws JsonValueOfOtherTypeException { throw new JsonValueOfOtherTypeException(j); }

        Date getDateValue(Json j) throws JsonValueOfOtherTypeException { throw new JsonValueOfOtherTypeException(j); }

        Boolean getBooleanValue(Json j) throws JsonValueOfOtherTypeException { throw new JsonValueOfOtherTypeException(j); }

        byte[] getBinaryValue(Json j) throws JsonValueOfOtherTypeException { throw new JsonValueOfOtherTypeException(j); }

        Map<String, Json> getObjectValue(Json j) throws JsonValueOfOtherTypeException { throw new JsonValueOfOtherTypeException(j); }

        List<Json> getArrayValue(Json j) throws JsonValueOfOtherTypeException { throw new JsonValueOfOtherTypeException(j); }

        Object copyValueFrom(Json j) { return j.value; }

        abstract void appendToString(StringBuilder sb, int indent, int level, Json j);
    }

    /** Создаёт пустой джсон. */
    public Json() {
    }

    /**
     * Создаёт джсон с копией значения переданного образца.
     *
     * @param that образец
     */
    public Json(Json that) {
        this.type = that.type;
        this.value = this.type.copyValueFrom(that);
    }

    // --------------- type checks ---------

    /** @return джсон не содержит значения. */
    public boolean isNull() {
        return value == null;
    }

    /** @return у джсона и его субъобъектов (джсон-объектов и джсон-массивов) кроме субъобъектов нет никаких значений. */
    public boolean isEffectiveNull() {
        return isNull() || (type == Type.OBJECT && areEffectiveNulls(type.getObjectValue(this).values())) || (type == Type.ARRAY && areEffectiveNulls(type.getArrayValue(this)));
    }

    private static boolean areEffectiveNulls(Collection<Json> jsons) {
        for (Json json : jsons) {
            if (!json.isEffectiveNull()) { return false; }
        }
        return true;
    }

    /** @return джсон содержит простое нерекурсивное значение (не джсон-объект и не джсон-массив). */
    public boolean isSimple() {return type.isSimple();}

    /** @return джсон содержит джсон-объект. */
    public boolean isObject() {return type.isObject();}

    /** @return джсон содержит джсон-массив. */
    public boolean isArray() {return type.isArray();}

    /**
     * Проверяет, что объект содержит явно установленное число.
     * Если объект содержит строковую запись числа, то этот метод вернёт false.
     * Парсер, например, считает строку с цифрами как строку и на этом успокоится.
     * Таким образом, этот метод следует использовать только для внутренне сгенерированных джсонов.
     *
     * @return джсон содержит явно установленное числовое значение — {@link BigDecimal}, {@link Integer} или {@link Long}
     */
    public boolean hasNumber() { return type.isNumber(); }

    /**
     * Проверяет, что объект содержит явно установленное число типа BigDecimal.
     * Если объект содержит строковую запись числа или целое число, то этот метод вернёт false.
     * Парсер, например, считает строку с цифрами без дробной части как Long и на этом успокоится.
     * Таким образом, этот метод следует использовать только для внутренне сгенерированных джсонов.
     *
     * @return джсон содержит явно установленное значение {@link BigDecimal}
     */
    public boolean hasDecimal() { return type.isDecimal(); }

    /**
     * Проверяет, что объект содержит явно установленное целое число.
     * Если объект содержит строковую запись числа, то этот метод вернёт false.
     * Парсер, например, считает строку с цифрами как строку и на этом успокоится.
     * Таким образом, этот метод следует использовать только для внутренне сгенерированных джсонов.
     *
     * @return джсон содержит явно установленное целое числовое значение — {@link Integer} или {@link Long}
     */
    public boolean hasInteger() { return type.isInteger(); }

    /**
     * Проверяет, что объект содержит явно установленную дату.
     * Если объект содержит строковую запись даты, то этот метод вернёт false.
     * Парсер, например, считает дату как строку и на этом успокоится.
     * Таким образом, этот метод следует использовать только для внутренне сгенерированных джсонов.
     *
     * @return джсон содержит явно установленную дату — {@link Date} или {@link Instant}.
     */
    public boolean hasDate() { return type.isDate(); }

    /** @return джсон содержит явно установленное булево значение. */
    public boolean hasBoolean() { return type == Type.BOOLEAN; }

    /** @return джсон содержит явно установленный бинарный массив. */
    public boolean hasBinary() { return type == Type.BINARY; }


    // --------------- setters ---------------

    /**
     * Сбрасывает значение узла в нул. Только что созданный объект содержит именно это значение.
     *
     * @return this
     */
    public Json drop() {
        this.type = Type.NULL;
        this.value = null;
        return this;
    }

    /**
     * Устанавливает узлу копию значения переданного исходного узла.
     *
     * @param that исходный узел
     * @return this
     */
    public Json copyFrom(Json that) {
        this.type = that.type;
        this.value = this.type.copyValueFrom(that);
        return this;
    }

    /**
     * Устанавливает узлу строковое значение.
     *
     * @param stringValue значение
     * @return this
     */
    public Json set(String stringValue) {
        this.type = stringValue == null ? Type.NULL : Type.STRING;
        this.value = stringValue;
        return this;
    }

    /**
     * Устанавливает узлу строковое значение.
     *
     * @param enumValue значение
     * @return this
     */
    public <E extends Enum<E>> Json set(E enumValue) {
        this.type = enumValue == null ? Type.NULL : Type.ENUM;
        this.value = enumValue;
        return this;
    }

    /**
     * Устанавливает узлу указанное числовое значение.
     *
     * @param bigDecimalValue значение
     * @return this
     */
    public Json set(BigDecimal bigDecimalValue) {
        this.type = bigDecimalValue == null ? Type.NULL : Type.BIG_DECIMAL;
        this.value = bigDecimalValue;
        return this;
    }

    /**
     * Устанавливает узлу числовое значение.
     *
     * @param integerValue значение
     * @return this
     */
    public Json set(Integer integerValue) {
        this.type = integerValue == null ? Type.NULL : Type.INTEGER;
        this.value = integerValue;
        return this;
    }

    /**
     * Устанавливает узлу числовое значение, хранящееся в контейнере.
     *
     * @param atomicInteger контейнер
     * @return this
     */
    public Json set(AtomicInteger atomicInteger) {
        this.type = Type.INTEGER;
        this.value = Integer.valueOf(atomicInteger.get());
        return this;
    }

    /**
     * Устанавливает узлу числовое значение.
     *
     * @param longValue значение
     * @return this
     */
    public Json set(Long longValue) {
        this.type = longValue == null ? Type.NULL : Type.LONG;
        this.value = longValue;
        return this;
    }

    /**
     * Устанавливает узлу числовое значение, хранящееся в контейнере.
     *
     * @param atomicLong контейнер
     * @return this
     */
    public Json set(AtomicLong atomicLong) {
        this.type = Type.LONG;
        this.value = Long.valueOf(atomicLong.get());
        return this;
    }

    /**
     * Устанавливает узлу инстант-значение.
     *
     * @param instantValue
     * @return this
     */
    public Json set(Instant instantValue) {
        this.type = instantValue == null ? Type.NULL : Type.INSTANT;
        this.value = instantValue;
        return this;
    }

    /**
     * Устанавливает узлу дата-значение.
     *
     * @param dateValue
     * @return this
     */
    public Json set(Date dateValue) {
        this.type = dateValue == null ? Type.NULL : Type.DATE;
        this.value = dateValue;
        return this;
    }

    /**
     * Устанавливает узлу указанное булево значение.
     *
     * @param booleanValue значение
     * @return this
     */
    public Json set(Boolean booleanValue) {
        this.type = booleanValue == null ? Type.NULL : Type.BOOLEAN;
        this.value = booleanValue;
        return this;
    }

    /**
     * Устанавливает узлу указанное булево значение.
     *
     * @param flagValue значение
     * @return this
     */
    public Json set(Flag flagValue) {
        this.type = flagValue == null ? Type.NULL : Type.BOOLEAN;
        this.value = flagValue == null ? null : flagValue.booleanValue() ? Boolean.TRUE : Boolean.FALSE;
        return this;
    }

    /**
     * Устанавливает узлу указанное двоичное значение.
     *
     * @param binaryValue значение
     * @return this
     */
    public Json set(byte[] binaryValue) {
        this.type = binaryValue == null ? Type.NULL : Type.BINARY;
        this.value = binaryValue;
        return this;
    }


    /**
     * Устанавливает узлу указанное значение типа «джсон-объект».
     *
     * @param objectValue значение
     * @return this
     */
    public Json set(Map<String, Json> objectValue) {
        this.type = objectValue == null ? Type.NULL : Type.OBJECT;
        this.value = objectValue;
        return this;
    }

    /**
     * Устанавливает узлу указанное значение типа «джсон-массив».
     *
     * @param arrayValue значение
     * @return this
     */
    public Json set(List<Json> arrayValue) {
        this.type = arrayValue == null ? Type.NULL : Type.ARRAY;
        this.value = arrayValue;
        return this;
    }

    // ------------- getters and toString()

    /**
     * Рекурсивно удаляет все нул-значения объектных и массивных подветвей.
     * <p>
     * Например,
     * <pre>{"root":{"stem":null,"anotherStem":"foo","tree":{"leaves":[null,null]}}}</pre>
     * станет
     * <pre>{"root":{"anotherStem":"foo"}}</pre>
     * — в таком аксепте.
     *
     * @return this
     * @see #isEffectiveNull()
     */
    public Json collapse() {
        if (type == Type.OBJECT) {
            for (Iterator<Json> iterator = type.getObjectValue(this).values().iterator(); iterator.hasNext(); ) {
                Json json = iterator.next();
                json.collapse();
                if (json.isNull()) {
                    iterator.remove();
                }
            }
        } else if (type == Type.ARRAY) {
            for (Iterator<Json> iterator = type.getArrayValue(this).iterator(); iterator.hasNext(); ) {
                Json json = iterator.next();
                json.collapse();
                if (json.isNull()) {
                    iterator.remove();
                }
            }
        }
        if (isEffectiveNull()) {
            drop();
        }
        return this;
    }

    /**
     * Возвращает содержащееся значение в виде строки.
     * Если у узла числовое или булево значение, прозрачно преобразует его в строку.
     * Если у узла двоичное значение, преобразует его в Base64-кодированную строку.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     *
     * @return строковое значение или нул
     * @throws JsonValueOfOtherTypeException
     * @see #getEnum(Class)
     */
    public String getString() throws JsonValueOfOtherTypeException {
        return type.getStringValue(this);
    }

    /**
     * Возвращает содержащееся значение в виде строки.
     * Если у узла числовое или булево значение, прозрачно преобразует его в строку.
     * Если у узла двоичное значение, преобразует его в Base64-кодированную строку.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если значение нул, выкидывает предоставленное сапплаером исключение.
     *
     * @param exceptionSupplier
     * @param <E>
     * @return строковое значение
     * @throws E
     * @see #getEnumOrDie(Class, Supplier)
     */
    public <E extends Throwable> String getStringOrDie(Supplier<E> exceptionSupplier) throws JsonValueOfOtherTypeException, E {
        return Box.with(getString()).getOrDie(exceptionSupplier);
    }


    /**
     * Возвращает содержащееся в указанном подузле значение в виде строки.
     * <p>
     * Если у узла числовое или булево значение, прозрачно преобразует его в строку.
     * Если у узла двоичное значение, преобразует его в Base64-кодированную строку.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     *
     * @return строковое значение или нул
     * @throws JsonValueOfOtherTypeException
     * @see #getEnum(Class, String)
     */
    public String getString(String complexName) throws JsonValueOfOtherTypeException {
        return Box.with(getObjectItem(complexName)).mapAndGet(Json::getString);
    }


    /**
     * Возвращает содержащееся в указанном подузле значение в виде строки.
     * <p>
     * Если у узла числовое или булево значение, прозрачно преобразует его в строку.
     * Если у узла двоичное значение, преобразует его в Base64-кодированную строку.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если значение нул, выкидывает предоставленное исключение, передавая <code>complexName</code> в качестве единственного аргумента.
     *
     * @param <E>
     * @param exceptionSupplier
     * @return строковое значение
     * @throws E
     * @see #getEnumOrDie(Class, String, Function)
     */
    public <E extends Throwable> String getStringOrDie(String complexName, Function<String, E> exceptionSupplier) throws JsonValueOfOtherTypeException, E {
        return Box.with(getString(complexName)).getOrDie(() -> exceptionSupplier.apply(complexName));
    }

    /**
     * Возвращает енум-значение узла.
     * Если у узла строковое значение, то пытается получить из него енум при помощи {@link Enum#valueOf(Class, String)}. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла нет значения, возвращает нул.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     *
     * @param <E>
     * @param enumType
     * @return
     * @see #getString()
     */
    public <E extends Enum<E>> E getEnum(Class<E> enumType) {
        return type.getEnumValue(enumType, this);
    }

    /**
     * Возвращает енум-значение узла.
     * Если у узла строковое значение, то пытается получить из него енум при помощи {@link Enum#valueOf(Class, String)}. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла нет значения, выкидывает предоставленное сапплаером исключение.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     *
     * @param <T>
     * @param enumType
     * @param exceptionSupplier @return строковое значение
     * @throws T
     * @see #getStringOrDie(Supplier)
     */
    public <E extends Enum<E>, T extends Throwable> E getEnumOrDie(Class<E> enumType, Supplier<T> exceptionSupplier) throws JsonValueOfOtherTypeException, T {
        return Box.with(getEnum(enumType)).getOrDie(exceptionSupplier);
    }

    /**
     * Возвращает енум-значение указанного узла.
     * Если у узла строковое значение, то пытается получить из него енум при помощи {@link Enum#valueOf(Class, String)}. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла нет значения, возвращает нул.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     *
     * @return строковое значение или нул
     * @throws JsonValueOfOtherTypeException
     * @see #getString(String)
     */
    public <E extends Enum<E>> E getEnum(Class<E> enumType, String complexName) throws JsonValueOfOtherTypeException {
        return Box.with(getObjectItem(complexName)).mapAndGet(j -> j.getEnum(enumType));
    }

    /**
     * Возвращает енум-значение указанного узла.
     * Если у узла строковое значение, то пытается получить из него енум при помощи {@link Enum#valueOf(Class, String)}. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла нет значения, выкидывает предоставленное сапплаером исключение, передавая <code>complexName</code> в качестве единственного аргумента.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     *
     * @param <E>
     * @param enumType
     * @param exceptionSupplier @return строковое значение
     * @throws E
     * @see #getStringOrDie(String, Function)
     */
    public <X extends Enum<X>, E extends Throwable> X getEnumOrDie(Class<X> enumType, String complexName, Function<String, E> exceptionSupplier) throws JsonValueOfOtherTypeException, E {
        return Box.with(getEnum(enumType, complexName)).getOrDie(() -> exceptionSupplier.apply(complexName));
    }

    /**
     * Возвращает числовое значение в виде {@link BigDecimal}.
     * <p>
     * Если у узла строковое значение, пытается преобразовать его в число. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла булево значение, возвращает {@link BigDecimal#ZERO} для false или {@link BigDecimal#ONE} для true.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     *
     * @return числовое значение объекта, если возможно, или нул
     */
    public BigDecimal getBigDecimal() throws JsonValueOfOtherTypeException, NumberFormatException {
        return type.getBigDecimalValue(this);
    }

    /**
     * Возвращает числовое значение в виде {@link BigDecimal}.
     * <p>
     * Если у узла строковое значение, пытается преобразовать его в число. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла булево значение, возвращает {@link BigDecimal#ZERO} для false или {@link BigDecimal#ONE} для true.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если значение нул, выкидывает предоставленное сапплаером исключение.
     *
     * @return числовое значение объекта, если возможно
     */
    public <E extends Throwable> BigDecimal getBigDecimalOrDie(Supplier<E> exceptionSupplier) throws JsonValueOfOtherTypeException, E {
        return Box.with(getBigDecimal()).getOrDie(exceptionSupplier);
    }

    /**
     * Возвращает числовое значение указанного подузла в виде {@link BigDecimal}.
     * <p>
     * Если подузла не существует, возвращает нул.
     * Если у узла строковое значение, пытается преобразовать его в число. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла булево значение, возвращает {@link BigDecimal#ZERO} для false или {@link BigDecimal#ONE} для true.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     *
     * @return числовое значение объекта, если возможно, или нул
     */
    public BigDecimal getBigDecimal(String complexName) throws JsonValueOfOtherTypeException {
        return Box.with(getObjectItem(complexName)).mapAndGet(Json::getBigDecimal);
    }

    /**
     * Возвращает числовое значение указанного подузла в виде {@link BigDecimal}.
     * <p>
     * Если подузла не существует, возвращает нул.
     * Если у узла строковое значение, пытается преобразовать его в число. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла булево значение, возвращает {@link BigDecimal#ZERO} для false или {@link BigDecimal#ONE} для true.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если значение нул, выкидывает предоставленное сапплаером исключение, передавая <code>complexName</code> в качестве единственного аргумента.
     *
     * @return числовое значение объекта, если возможно
     */
    public <E extends Throwable> BigDecimal getBigDecimalOrDie(String complexName, Function<String, E> exceptionSupplier) throws E {
        return Box.with(getBigDecimal(complexName)).getOrDie(() -> exceptionSupplier.apply(complexName));
    }

    /**
     * Возвращает числовое значение в виде {@link Integer}.
     * <p>
     * Если у узла строковое значение, пытается преобразовать его в число. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла булево значение, возвращает 0 для false или 1 для true.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     *
     * @return числовое значение объекта, если возможно, или нул
     */
    public Integer getInteger() throws JsonValueOfOtherTypeException {
        return type.getIntegerValue(this);
    }

    /**
     * Возвращает числовое значение в виде {@link Integer}.
     * <p>
     * Если у узла строковое значение, пытается преобразовать его в число. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла булево значение, возвращает 0 для false или 1 для true.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если значение нул, выкидывает предоставленное сапплаером исключение.
     *
     * @return числовое значение объекта, если возможно, или нул
     */
    public <E extends Throwable> Integer getIntegerOrDie(Supplier<E> exceptionSupplier) throws E {
        return Box.with(getInteger()).getOrDie(exceptionSupplier);
    }


    /**
     * Возвращает числовое значение указанного подузла в виде {@link Integer}.
     * <p>
     * Если у узла строковое значение, пытается преобразовать его в число. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла булево значение, возвращает 0 для false или 1 для true.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     *
     * @return числовое значение объекта, если возможно, или нул
     */
    public Integer getInteger(String complexName) throws JsonValueOfOtherTypeException {
        return Box.with(getObjectItem(complexName)).mapAndGet(Json::getInteger);
    }

    /**
     * Возвращает числовое значение указанного подузла в виде {@link Integer}.
     * <p>
     * Если у узла строковое значение, пытается преобразовать его в число. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла булево значение, возвращает 0 для false или 1 для true.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если значение нул, выкидывает предоставленное сапплаером исключение, передавая <code>complexName</code> в качестве единственного аргумента.
     *
     * @return числовое значение объекта, если возможно, или нул
     */
    public <E extends Throwable> Integer getIntegerOrDie(String complexName, Function<String, E> exceptionSupplier) throws E {
        return Box.with(getInteger(complexName)).getOrDie(() -> exceptionSupplier.apply(complexName));
    }

    /**
     * Возвращает числовое значение в виде {@link Long}.
     * <p>
     * Если у узла строковое значение, пытается преобразовать его в число. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла булево значение, возвращает 0L для false или 1L для true.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     *
     * @return числовое значение объекта, если возможно, или нул
     */
    public Long getLong() throws JsonValueOfOtherTypeException {
        return type.getLongValue(this);
    }

    /**
     * Возвращает числовое значение в виде {@link Long}.
     * <p>
     * Если у узла строковое значение, пытается преобразовать его в число. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла булево значение, возвращает 0L для false или 1L для true.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     * Иначе выкидывает предоставленное сапплаером исключение.
     *
     * @return числовое значение объекта, если возможно
     */
    public <E extends Throwable> Long getLongOrDie(Supplier<E> exceptionSupplier) throws E {
        return Box.with(getLong()).getOrDie(exceptionSupplier);
    }

    /**
     * Возвращает числовое значение указанного подузла в виде {@link Long}.
     * <p>
     * Если у узла строковое значение, пытается преобразовать его в число. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла булево значение, возвращает 0L для false или 1L для true.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     *
     * @return числовое значение объекта, если возможно, или нул
     */
    public Long getLong(String complexName) throws JsonValueOfOtherTypeException {
        return Box.with(getObjectItem(complexName)).mapAndGet(Json::getLong);
    }

    /**
     * Возвращает числовое значение указанного подузла в виде {@link Long}.
     * <p>
     * Если у узла строковое значение, пытается преобразовать его в число. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла булево значение, возвращает 0L для false или 1L для true.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     * Иначе выкидывает предоставленное сапплаером исключение, передавая <code>complexName</code> в качестве единственного аргумента.
     *
     * @return числовое значение объекта, если возможно
     */
    public <E extends Throwable> Long getLongOrDie(String complexName, Function<String, E> exceptionSupplier) throws E {
        return Box.with(getLong(complexName)).getOrDie(() -> exceptionSupplier.apply(complexName));
    }

    /**
     * Возвращает значение в виде {@link Instant}.
     * <p>
     * Если у узла строковое значение, пытается распознать его как дату-время в ISO-8601. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла BigDecimal или Long-значения, трактует их как миллисекунды с 1 января 1970.
     * Если у узла значение типа Date, то понятно.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если значения нет, возвращает нул.
     *
     * @return числовое значение объекта, если возможно, или нул
     */
    public Instant getInstant() throws JsonValueOfOtherTypeException {
        return type.getInstantValue(this);
    }

    /**
     * Возвращает числовое значение в виде {@link Instant}.
     * <p>
     * Если у узла строковое значение, пытается распознать его как дату-время в ISO-8601. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла BigDecimal или Long-значения, трактует их как миллисекунды с 1 января 1970.
     * Если у узла значение типа Date, то понятно.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если значения нет, выкидывает предоставленное сапплаером исключение.
     *
     * @return числовое значение объекта, если возможно
     */
    public <E extends Throwable> Instant getInstantOrDie(Supplier<E> exceptionSupplier) throws E {
        return Box.with(getInstant()).getOrDie(exceptionSupplier);
    }

    /**
     * Возвращает значение в виде {@link Instant}.
     * <p>
     * Если у узла строковое значение, пытается распознать его как дату-время в ISO-8601. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла BigDecimal или Long-значения, трактует их как миллисекунды с 1 января 1970.
     * Если у узла значение типа Date, то понятно.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если значения нет, возвращает нул.
     *
     * @return числовое значение объекта, если возможно, или нул
     */
    public Instant getInstant(String complexName) throws JsonValueOfOtherTypeException {
        return Box.with(getObjectItem(complexName)).mapAndGet(Json::getInstant);
    }

    /**
     * Возвращает значение в виде {@link Instant}.
     * <p>
     * Если у узла строковое значение, пытается распознать его как дату-время в ISO-8601. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла BigDecimal или Long-значения, трактует их как миллисекунды с 1 января 1970.
     * Если у узла значение типа Date, то понятно.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если значения нет, выкидывает предоставленное сапплаером исключение.
     *
     * @return числовое значение объекта, если возможно
     */
    public <E extends Throwable> Instant getInstantOrDie(String complexName, Function<String, E> exceptionSupplier) throws E {
        return Box.with(getInstant(complexName)).getOrDie(() -> exceptionSupplier.apply(complexName));
    }

    /**
     * Возвращает значение в виде {@link Date}.
     * <p>
     * Если у узла строковое значение, пытается распознать его как дату-время в ISO-8601. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла BigDecimal или Long-значения, трактует их как миллисекунды с 1 января 1970.
     * Если у узла значение типа Instant, то понятно.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если значения нет, возвращает нул.
     *
     * @return числовое значение объекта, если возможно, или нул
     */
    public Date getDate() throws JsonValueOfOtherTypeException {
        return type.getDateValue(this);
    }

    /**
     * Возвращает числовое значение в виде {@link Date}.
     * <p>
     * Если у узла строковое значение, пытается распознать его как дату-время в ISO-8601. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла BigDecimal или Long-значения, трактует их как миллисекунды с 1 января 1970.
     * Если у узла значение типа Instant, то понятно.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если значения нет, выкидывает предоставленное сапплаером исключение.
     *
     * @return числовое значение объекта, если возможно
     */
    public <E extends Throwable> Date getDateOrDie(Supplier<E> exceptionSupplier) throws E {
        return Box.with(getDate()).getOrDie(exceptionSupplier);
    }

    /**
     * Возвращает значение в виде {@link Date}.
     * <p>
     * Если у узла строковое значение, пытается распознать его как дату-время в ISO-8601. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла BigDecimal или Long-значения, трактует их как миллисекунды с 1 января 1970.
     * Если у узла значение типа Instant, то понятно.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если значения нет, возвращает нул.
     *
     * @return числовое значение объекта, если возможно, или нул
     */
    public Date getDate(String complexName) throws JsonValueOfOtherTypeException {
        return Box.with(getObjectItem(complexName)).mapAndGet(Json::getDate);
    }

    /**
     * Возвращает значение в виде {@link Date}.
     * <p>
     * Если у узла строковое значение, пытается распознать его как дату-время в ISO-8601. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если у узла BigDecimal или Long-значения, трактует их как миллисекунды с 1 января 1970.
     * Если у узла значение типа Instant, то понятно.
     * Если значение иного типа, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если значения нет, выкидывает предоставленное сапплаером исключение.
     *
     * @return числовое значение объекта, если возможно
     */
    public <E extends Throwable> Date getDateOrDie(String complexName, Function<String, E> exceptionSupplier) throws E {
        return Box.with(getDate(complexName)).getOrDie(() -> exceptionSupplier.apply(complexName));
    }

    /**
     * Если определено булево значение, возвращает его.
     * Если определено строковое значение, пытается распознать булево значение при помощи {@link BooleanUtils#parse(String)}. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если определено цифровое значение, возвращает false, если оно 0, иначе true.
     * Иначе выкидывает {@link JsonValueOfOtherTypeException}.
     *
     * @return булево значение или нул
     * @throws JsonValueOfOtherTypeException
     */
    public Boolean getBoolean() throws JsonValueOfOtherTypeException {
        return type.getBooleanValue(this);
    }

    /**
     * Если определено булево значение, возвращает его.
     * Если определено строковое значение, пытается распознать булево значение при помощи {@link BooleanUtils#parse(String)}. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если определено цифровое значение, возвращает false, если оно 0, иначе true.
     * Иначе выкидывает {@link JsonValueOfOtherTypeException}.
     * Иначе выкидывает предоставленное сапплаером исключение.
     *
     * @return булево значение
     * @throws JsonValueOfOtherTypeException
     */
    public <E extends Throwable> Boolean getBooleanOrDie(Supplier<E> exceptionSupplier) throws E {
        return Box.with(getBoolean()).getOrDie(exceptionSupplier);
    }

    /**
     * Если указанный узел не существует, возвращает нул.
     * Если определено булево значение, возвращает его.
     * Если определено строковое значение, пытается распознать булево значение при помощи {@link BooleanUtils#parse(String)}. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если определено цифровое значение, возвращает false, если оно 0, иначе true.
     * Иначе выкидывает {@link JsonValueOfOtherTypeException}.
     *
     * @return булево значение или нул
     */
    public Boolean getBoolean(String complexName) {
        return Box.with(getObjectItem(complexName)).mapAndGet(Json::getBoolean);
    }

    /**
     * Если указанный узел не существует, выкидывает предоставленное сапплаером исключение, передавая <code>complexName</code> в качестве единственного аргумента.
     * Если определено булево значение, возвращает его.
     * Если определено строковое значение, пытается распознать булево значение при помощи {@link BooleanUtils#parse(String)}. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Если определено цифровое значение, возвращает false, если оно 0, иначе true.
     * Иначе выкидывает {@link JsonValueOfOtherTypeException}.
     *
     * @return булево значение
     */
    public <E extends Throwable> Boolean getBooleanOrDie(String complexName, Function<String, E> exceptionSupplier) throws E {
        return Box.with(getBoolean(complexName)).getOrDie(() -> exceptionSupplier.apply(complexName));
    }

    /**
     * Если определено двоичное значение, возвращает его.
     * Если определено строковое значение, пытается его декодировать из Base64. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * В остальных случаях возвращает нул.
     *
     * @return булево значение или нул
     */
    public byte[] getBinary() {
        return type.getBinaryValue(this);
    }

    /**
     * Если определено двоичное значение, возвращает его.
     * Если определено строковое значение, пытается его декодировать из Base64. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Иначе выкидывает предоставленное сапплаером исключение.
     *
     * @return булево значение
     */
    public <E extends Throwable> byte[] getBinaryOrDie(Supplier<E> exceptionSupplier) throws E {
        return Box.with(getBinary()).getOrDie(exceptionSupplier);
    }

    /**
     * Если указанный узел не существует, возвращает нул.
     * Если определено двоичное значение, возвращает его.
     * Если определено строковое значение, пытается его декодировать из Base64. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Иначе выкидывает {@link JsonValueOfOtherTypeException}.
     *
     * @return булево значение или нул
     */
    public byte[] getBinary(String complexName) {
        return Box.with(getObjectItem(complexName)).mapAndGet(Json::getBinary);
    }

    /**
     * Если указанный узел не существует, выкидывает предоставленное сапплаером исключение, передавая <code>complexName</code> в качестве единственного аргумента.
     * Если определено двоичное значение, возвращает его.
     * Если определено строковое значение, пытается его декодировать из Base64. Если не удалось, выкидывает {@link JsonValueOfOtherTypeException}.
     * Иначе выкидывает {@link JsonValueOfOtherTypeException}.
     *
     * @return булево значение
     */
    public <E extends Throwable> byte[] getBinaryOrDie(String complexName, Function<String, E> exceptionSupplier) throws E {
        return Box.with(getBinary(complexName)).getOrDie(() -> exceptionSupplier.apply(complexName));
    }

    /**
     * Если определено булево значение, возвращает его в виде {@link Flag}.
     * Если определено строковое значение, возвращает {@link BooleanUtils#parse(String)}.
     * Если определено цифровое значение, возвращает NO, если оно 0, иначе YES.
     *
     * @return булево значение или нул
     */
    public Flag getFlag() {
        return Flag.pickBoolean(getBoolean());
    }

    /**
     * Если определено булево значение, возвращает его в виде {@link Flag}.
     * Если определено строковое значение, возвращает {@link BooleanUtils#parse(String)}.
     * Если определено цифровое значение, возвращает NO, если оно 0, иначе YES.
     * Иначе выкидывает предоставленное сапплаером исключение.
     *
     * @return булево значение
     */
    public <E extends Throwable> Flag getFlagOrDie(Supplier<E> exceptionSupplier) throws E {
        return Box.with(getFlag()).getOrDie(exceptionSupplier);
    }

    /**
     * Если определено булево значение, возвращает его в виде {@link Flag}.
     * Если определено строковое значение, возвращает {@link BooleanUtils#parse(String)}.
     * Если определено цифровое значение, возвращает NO, если оно 0, иначе YES.
     *
     * @return булево значение или нул
     */
    public Flag getFlag(String complexName) {
        return Box.with(getObjectItem(complexName)).mapAndGet(Json::getFlag);
    }

    /**
     * Если определено булево значение, возвращает его в виде {@link Flag}.
     * Если определено строковое значение, возвращает {@link BooleanUtils#parse(String)}.
     * Если определено цифровое значение, возвращает NO, если оно 0, иначе YES.
     * Иначе выкидывает предоставленное сапплаером исключение, передавая <code>complexName</code> в качестве единственного аргумента.
     *
     * @return булево значение
     */
    public <E extends Throwable> Flag getFlagOrDie(String complexName, Function<String, E> exceptionSupplier) throws E {
        return Box.with(getFlag(complexName)).getOrDie(() -> exceptionSupplier.apply(complexName));
    }

    /**
     * Если у узла определено значение типа «джсон-объект», возвращает его, иначе возвращает нул.
     *
     * @return значение типа «джсон-объект» или нул
     */
    public Map<String, Json> getObjectValue() {
        return type.getObjectValue(this);
    }

    /** @return true, если объект содержит значение типа «джсон-объект», иначе false */
    public boolean hasObjectValue() { return type == Type.OBJECT; }

    /**
     * Если у узла определено значение типа «джсон-объект», возвращает его, иначе возвращает нул.
     * Иначе выкидывает предоставленное сапплаером исключение.
     *
     * @return значение типа «джсон-объект»
     */
    public <E extends Throwable> Map<String, Json> getObjectValueOrDie(Supplier<E> exceptionSupplier) throws E {
        return Box.with(getObjectValue()).getOrDie(exceptionSupplier);
    }

    /**
     * Если у узла определено значение типа «джсон-объект», возвращает его, иначе возвращает нул.
     *
     * @return значение типа «джсон-объект» или нул
     */
    public Map<String, Json> getObjectValue(String complexName) {
        return Box.with(getObjectItem(complexName)).mapAndGet(Json::getObjectValue);
    }

    /**
     * Если у узла определено значение типа «джсон-объект», возвращает его, иначе возвращает нул.
     * Иначе выкидывает предоставленное сапплаером исключение, передавая <code>complexName</code> в качестве единственного аргумента.
     *
     * @return значение типа «джсон-объект»
     */
    public <E extends Throwable> Map<String, Json> getObjectValueOrDie(String complexName, Function<String, E> exceptionSupplier) throws E {
        return Box.with(getObjectValue(complexName)).getOrDie(() -> exceptionSupplier.apply(complexName));
    }

    /**
     * Вытаскивает объектное значение текущего джсона и его наследников в текущий объект,
     * склеивая ключи символом-разделителем сложных имён {@link #COMPLEX_DELIMITER}:
     * <pre>{"foo":{"bar":{"zed":"bananana","kuka":"zuka"}}}</pre>
     * ↓
     * <pre>{"foo.bar.zed":"bananana","foo.bar.kuka":"zuka"}</pre>
     * Если у джсона значение иного типа, ничего не делает.
     *
     * @return this
     */
    public Json flattenObjectValue() {
        if (hasObjectValue() && isObjectValueFlattable()) {
            LinkedHashMap<String, Json> nv = new LinkedHashMap<>();
            for (Map.Entry<String, Json> entry : type.getObjectValue(this).entrySet()) {
                Json childJ = entry.getValue();
                if (childJ.hasObjectValue()) {
                    childJ.flattenObjectValue();
                    String prefix = entry.getKey() + COMPLEX_DELIMITER;
                    for (Map.Entry<String, Json> childEntry : childJ.type.getObjectValue(childJ).entrySet()) {
                        nv.put(prefix + childEntry.getKey(), childEntry.getValue());
                    }
                } else {
                    nv.put(entry.getKey(), entry.getValue());
                }
            }
            this.value = nv;
        }
        return this;
    }

    private boolean isObjectValueFlattable() {
        for (Json childJ : type.getObjectValue(this).values()) {
            if (childJ.hasObjectValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Если у узла определено значение типа «джсон-массив», возвращает его, иначе возвращает нул.
     *
     * @return значение типа «джсон-массив» или нул
     */
    public List<Json> getArrayValue() {
        return type.getArrayValue(this);
    }

    /** @return true, если объект содержит значение типа «джсон-массив», иначе false */
    public boolean hasArrayValue() { return this.type == Type.ARRAY; }

    /**
     * Сортирует массивное значение, если оно есть, указанным сравнивателем. Если массивного значения нет, ничего не делает.
     *
     * @param comparator сравниватель элементов
     * @return this
     */
    public Json sortArrayValue(Comparator<Json> comparator) {
        if (hasArrayValue()) {
            type.getArrayValue(this).sort(comparator);
        }
        return this;
    }

    /**
     * Если у узла определено значение типа «джсон-массив», возвращает его, иначе возвращает нул.
     * Если значение нул, выкидывает предоставленное сапплаером исключение.
     *
     * @return значение типа «джсон-массив»
     */
    public <E extends Throwable> List<Json> getArrayValueOrDie(Supplier<E> exceptionSupplier) throws E {
        return Box.with(getArrayValue()).getOrDie(exceptionSupplier);
    }

    /**
     * Если у подузла по указанному сложному имени определено значение типа «джсон-массив», возвращает его, иначе возвращает нул.
     *
     * @return значение типа «джсон-массив» или нул
     */
    public List<Json> getArrayValue(String complexName) {
        return Box.with(getObjectItem(complexName)).mapAndGet(Json::getArrayValue);
    }

    /**
     * Если у подузла по указанному сложному имени определено значение типа «джсон-массив», возвращает его.
     * Иначе выкидывает предоставленное сапплаером исключение, передавая <code>complexName</code> в качестве единственного аргумента.
     *
     * @return значение типа «джсон-массив»
     */
    public <E extends Throwable> List<Json> getArrayValueOrDie(String complexName, Function<String, E> exceptionSupplier) throws E {
        return Box.with(getArrayValue(complexName)).getOrDie(() -> exceptionSupplier.apply(complexName));
    }

    // ------------- shortcuts and syntax sugar for complex types

    /**
     * Устанавливает узлу значение типа «джсон-массив», если узел не содержит такого значения.
     * Возвращает значение узла.
     *
     * @return значение типа «джсон-массив»
     */
    public Map<String, Json> getObjectValueOrSpawn() {
        if (this.type != Type.OBJECT) {
            this.type = Type.OBJECT;
            this.value = new LinkedHashMap<>();
        }
        return (Map<String, Json>) this.value;
    }

    /**
     * Устанавливает узлу значение типа «джсон-объект», если узел не содержит такого значения.
     * Создаёт в нём новый узел  и возвращает его. Существующий объект с таким именем будет заменён.
     * <p>
     * Если запрошен объект с именем нул, ничего делать не будет и просто вернёт нул.
     *
     * @param name имя создаваемого узла
     * @return созданый узел или нул
     */
    public Json newObjectItemFlat(String name) {
        if (name == null) { return null; }
        return retrieveObjectItemOrSpawn(name, new Json(), false);
    }

    /**
     * Устанавливает узлу значение типа «джсон-объект», если узел не содержит такого значения.
     * Создаёт в нём новый узел  и возвращает его. Существующий объект с таким именем будет заменён.
     * <p>
     * Если запрошен объект с именем нул, ничего делать не будет и просто вернёт нул.
     *
     * @param complexName имя создаваемого узла
     * @return созданый узел или нул
     */
    public Json newObjectItem(String complexName) {
        if (complexName == null) { return null; }
        return retrieveObjectItemOrSpawn(complexName, new Json(), true);
    }

    /**
     * Устанавливает узлу значение типа «джсон-объект», если узел не содержит такого значения.
     * Добавляет к этому значению указанный узел.
     * <p>
     * Если запрошен узел с именем нул, ничего делать не будет и просто вернёт нул.
     *
     * @param name
     * @param item
     * @return this
     */
    public Json addObjectItemFlat(String name, Json item) {
        if (name == null) { return null; }
        retrieveObjectItemOrSpawn(name, item, false);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-объект», если узел не содержит такого значения.
     * Добавляет к этому значению указанный узел.
     * <p>
     * Если запрошен узел с именем нул, ничего делать не будет и просто вернёт нул.
     *
     * @param complexName
     * @param item
     * @return this
     */
    public Json addObjectItem(String complexName, Json item) {
        if (complexName == null) { return null; }
        retrieveObjectItemOrSpawn(complexName, item, true);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-объект», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param complexName
     * @param stringValue
     * @return this
     */
    public Json addNewObjectItem(String complexName, String stringValue) {
        newObjectItem(complexName).set(stringValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-объект», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param complexName
     * @param enumValue
     * @return this
     */
    public <E extends Enum<E>> Json addNewObjectItem(String complexName, E enumValue) {
        newObjectItem(complexName).set(enumValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-объект», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param complexName
     * @param bigDecimalValue
     * @return this
     */
    public Json addNewObjectItem(String complexName, BigDecimal bigDecimalValue) {
        newObjectItem(complexName).set(bigDecimalValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-объект», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param complexName
     * @param integerValue
     * @return this
     */
    public Json addNewObjectItem(String complexName, Integer integerValue) {
        newObjectItem(complexName).set(integerValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-объект», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему значение, хранящееся в контейнере.
     *
     * @param complexName
     * @param atomicInteger
     * @return this
     */
    public Json addNewObjectItem(String complexName, AtomicInteger atomicInteger) {
        newObjectItem(complexName).set(atomicInteger);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-объект», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param complexName
     * @param longValue
     * @return this
     */
    public Json addNewObjectItem(String complexName, Long longValue) {
        newObjectItem(complexName).set(longValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-объект», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему значение, хранящееся в контейнере.
     *
     * @param complexName
     * @param atomicLong
     * @return this
     */
    public Json addNewObjectItem(String complexName, AtomicLong atomicLong) {
        newObjectItem(complexName).set(atomicLong);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-объект», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param complexName
     * @param longValue
     * @return this
     */
    public Json addNewObjectItem(String complexName, Instant longValue) {
        newObjectItem(complexName).set(longValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-объект», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param complexName
     * @param dateValue
     * @return this
     */
    public Json addNewObjectItem(String complexName, Date dateValue) {
        newObjectItem(complexName).set(dateValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-объект», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param complexName
     * @param booleanValue
     * @return this
     */
    public Json addNewObjectItem(String complexName, Boolean booleanValue) {
        newObjectItem(complexName).set(booleanValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-объект», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param complexName
     * @param binaryValue
     * @return this
     */
    public Json addNewObjectItem(String complexName, byte[] binaryValue) {
        newObjectItem(complexName).set(binaryValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-объект», если узел не содержит такого значения.
     * Добавляет к этому значению указанный узел.
     *
     * @param complexName
     * @param objectValue
     * @return this
     */
    public Json addNewObjectItem(String complexName, Map<String, Json> objectValue) {
        newObjectItem(complexName).set(objectValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-объект», если узел не содержит такого значения.
     * Добавляет к этому значению указанный узел.
     *
     * @param complexName
     * @param arrayValue
     * @return this
     */
    public Json addNewObjectItem(String complexName, List<Json> arrayValue) {
        newObjectItem(complexName).set(arrayValue);
        return this;
    }

    /**
     * Возвращает элемент «объекта» в джсонной номенклатуре по его имени.
     * <p>
     * Хитрость: точки в имени трактуются как уровни вложенности.
     * Например, для объекта {@code {"foo":{"bar":"azazaza"}}}
     * вызов {@code getObjectItem("foo.bar")} вернёт объект, содержащий "azazaza".
     * <p>
     * Если объект не найден или запрошен объект с именем нул, вернёт нул.
     *
     * @param complexName (сложное) имя джсон-объекта
     * @return объект или нул.
     */
    public Json getObjectItem(String complexName) {
        if (complexName == null) { return null; }
        return retrieveObjectItem(complexName);
    }

    /**
     * Возвращает элемент «объекта» в джсонной номенклатуре по его имени.
     * <p>
     * Хитрость: точки в имени трактуются как уровни вложенности.
     * Например, для объекта {@code {"foo":{"bar":"azazaza"}}}
     * вызов {@code getObjectItem("foo.bar")} вернёт объект, содержащий "azazaza".
     * <p>
     * Если объект не найден или его значение нул, выкидывает предоставленное сапплаером исключение, передавая <code>complexName</code> в качестве единственного аргумента.
     *
     * @param complexName (сложное) имя джсон-объекта
     * @return объект
     */
    public <E extends Throwable> Json getObjectItemOrDie(String complexName, Function<String, E> exceptionSupplier) throws E {
        return Box.with(getObjectItem(complexName)).getOrDie(() -> exceptionSupplier.apply(complexName));
    }

    /**
     * Возвращает элемент «объекта» в джсонной номенклатуре по его имени.
     * <p>
     * Если объект не найден, создаст его и вернёт созданный объект.
     * Если запрошен объект с именем нул, ничего создавать не будет и просто вернёт нул.
     *
     * @param name (сложное) имя джсон-объекта
     * @return объект или нул.
     */
    public Json getOrSpawnObjectItemFlat(String name) {
        if (name == null) { return null; }
        return retrieveObjectItemOrSpawn(name, null, false);
    }

    /**
     * Возвращает элемент «объекта» в джсонной номенклатуре по его имени.
     * <p>
     * Хитрость: точки в имени трактуются как уровни вложенности.
     * Например, для объекта {@code {"foo":{"bar":"azazaza"}}}
     * вызов {@code getObjectItem("foo.bar")} вернёт объект, содержащий "azazaza".
     * <p>
     * Если объект не найден, создаст его и вернёт созданный объект.
     * Если запрошен объект с именем нул, ничего создавать не будет и просто вернёт нул.
     *
     * @param complexName (сложное) имя джсон-объекта
     * @return объект или нул.
     */
    public Json getOrSpawnObjectItem(String complexName) {
        if (complexName == null) { return null; }
        return retrieveObjectItemOrSpawn(complexName, null, true);
    }

    /**
     * @param name интересующее имя узла
     * @param forceChild объект, который нужно положить по указанному имени или нул, если новый объект класть не надо
     * @param complex если просят создать узел, то считать имя составным и сделать узел в иерархии, иначе плоско
     * @return forceChild или созданный объект в конце пути или нул
     */
    private Json retrieveObjectItemOrSpawn(String name, Json forceChild, boolean complex) {
        Json root = this;
        int pos = 0;
        while (true) {
            if (root.type != Type.OBJECT) {
                root.getObjectValueOrSpawn();
            }
            int dotPos;
            if (complex && (dotPos = name.indexOf(COMPLEX_DELIMITER, pos)) >= 0) {
                root = ((Map<String, Json>) root.value).computeIfAbsent(name.substring(pos, dotPos), x -> new Json());
                pos = dotPos + 1;
            } else if (forceChild == null) {
                return ((Map<String, Json>) root.value).computeIfAbsent(name.substring(pos), x -> new Json());
            } else {
                ((Map<String, Json>) root.value).put(name.substring(pos), forceChild);
                return forceChild;
            }
        }
    }

    /**
     * Ищет узел по имени.
     * <p>
     * Сначала смотрит плоское имя, если такого нет, разрезает имя по {@link #COMPLEX_DELIMITER} и ищет по уровням,
     *
     * @param name простое или сложное имя
     * @return найденный узел
     */
    private Json retrieveObjectItem(String name) {
        Json root = this;
        int pos = 0;
        while (true) {
            if (root.type != Type.OBJECT) {
                return null;
            }
            Json child = ((Map<String, Json>) root.value).get(pos == 0 ? name : name.substring(pos));
            if (child != null) {
                return child;
            }

            int dotPos = name.indexOf(COMPLEX_DELIMITER, pos);
            if (dotPos < 0) {
                return null;
            }

            root = ((Map<String, Json>) root.value).get(name.substring(pos, dotPos));
            if (root == null) {
                return null;
            }
            pos = dotPos + 1;
        }
    }

    /**
     * Устанавливает узлу значение типа «джсон-массив», если узел не содержит такого значения.
     * Возвращает значение узла.
     *
     * @return значение типа «джсон-массив»
     */
    public List<Json> getArrayValueOrSpawn() {
        if (this.type != Type.ARRAY) {
            this.type = Type.ARRAY;
            this.value = new ArrayList<>();
        }
        return (List<Json>) this.value;
    }

    /**
     * Устанавливает узлу значение типа «джсон-массив», если узел не содержит такого значения.
     * Создаёт в нём новый узел и возвращает его.
     *
     * @return созданный узел
     */
    public Json newArrayItem() {
        Json child = new Json();
        getArrayValueOrSpawn().add(child);
        return child;
    }

    /**
     * Устанавливает узлу значение типа «джсон-массив», если узел не содержит такого значения.
     * Добавляет к этому значению указанный узел.
     *
     * @param item
     * @return this
     */
    public Json addArrayItem(Json item) {
        getArrayValueOrSpawn().add(item);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-массив», если узел не содержит такого значения.
     * Добавляет к этому значению указанные узлы.
     *
     * @param items
     * @return this
     */
    public Json addArrayItems(Collection<Json> items) {
        getArrayValueOrSpawn().addAll(items);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-массив», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param stringValue
     * @return this
     */
    public Json addNewArrayItem(String stringValue) {
        newArrayItem().set(stringValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-массив», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param enumValue
     * @return this
     */
    public <E extends Enum<E>> Json addNewArrayItem(E enumValue) {
        newArrayItem().set(enumValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-массив», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param bigDecimalValue
     * @return this
     */
    public Json addNewArrayItem(BigDecimal bigDecimalValue) {
        newArrayItem().set(bigDecimalValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-массив», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param integerValue
     * @return this
     */
    public Json addNewArrayItem(Integer integerValue) {
        newArrayItem().set(integerValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-массив», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему значение, хранящееся в контейнере.
     *
     * @param atomicInteger
     * @return this
     */
    public Json addNewArrayItem(AtomicInteger atomicInteger) {
        newArrayItem().set(atomicInteger);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-массив», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param longValue
     * @return this
     */
    public Json addNewArrayItem(Long longValue) {
        newArrayItem().set(longValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-массив», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему значение, хранящееся в контейнере.
     *
     * @param atomicLong
     * @return this
     */
    public Json addNewArrayItem(AtomicLong atomicLong) {
        newArrayItem().set(atomicLong);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-массив», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param instantValue
     * @return this
     */
    public Json addNewArrayItem(Instant instantValue) {
        newArrayItem().set(instantValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-массив», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param dateValue
     * @return this
     */
    public Json addNewArrayItem(Date dateValue) {
        newArrayItem().set(dateValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-массив», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param booleanValue
     * @return this
     */
    public Json addNewArrayItem(Boolean booleanValue) {
        newArrayItem().set(booleanValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-массив», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param objectValue
     * @return this
     */
    public Json addNewArrayItem(Map<String, Json> objectValue) {
        newArrayItem().set(objectValue);
        return this;
    }

    /**
     * Устанавливает узлу значение типа «джсон-массив», если узел не содержит такого значения.
     * Создаёт в нём новый элемент и устанавливает ему указанное значение.
     *
     * @param arrayValue
     * @return this
     */
    public Json addNewArrayItem(List<Json> arrayValue) {
        newArrayItem().set(arrayValue);
        return this;
    }

    // ---------- Escaping tools

    /**
     * Возвращает строку, в котором двойные кавычки, прямой и обратный слэши и спецсимволы предварены обратным слэшом, как это описано в джсон-спеках.
     * Если передан нул, вернёт нул.
     *
     * @return маскированная строка значение или нул
     * @see #deescapeString(String)
     */
    public static String escapeString(String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }
        int length = string.length();
        for (int i = 0; i < length; i++) {
            char ch = string.charAt(i);
            switch (ch) {
            case '\b':
            case '\f':
            case '\n':
            case '\r':
            case '\t':
            case '\"':
            case '\\':
                return escapeHeavy(string, i, length);
            }
        }
        return string;
    }

    /**
     * Сюда попадают строки, в которых точно есть символы для замены.
     * Причём нам уже показывают на символ с наименьшим индексом.
     *
     * @param str
     * @return
     */
    private static String escapeHeavy(String str, int i, int length) {
        StringBuilder sb = new StringBuilder(str.length() * 2).append(str.substring(0, i));
        while (i < length) {
            char ch = str.charAt(i);
            switch (ch) {
            case '\"':
            case '\\':
            case '/':
                sb.append('\\').append(ch);
                break;
            case '\b':
                sb.append('\\').append('b');
                break;
            case '\f':
                sb.append('\\').append('f');
                break;
            case '\n':
                sb.append('\\').append('n');
                break;
            case '\r':
                sb.append('\\').append('r');
                break;
            case '\t':
                sb.append('\\').append('t');
                break;
            default:
                sb.append(ch);
            }
            i++;
        }
        return sb.toString();
    }

    /**
     * Возвращает строку, из которой удалены обратные слэши за исключением тех мест, перед которыми стояли обратные слэши.
     * Если передан нул, вернёт нул.
     *
     * @param str
     * @return размаскированная строка значение или нул
     * @see #escapeString(String)
     */
    public static String deescapeString(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        int pos = str.indexOf((int) '\\');
        if (pos < 0) {
            return str;
        }

        // do the heavy business
        StringBuilder sb = new StringBuilder(str.length());
        int lengthLessOne = str.length() - 1;
        int lastPos = 0;
        while (true) {
            if (pos < 0 || pos >= lengthLessOne) {
                sb.append(str.substring(lastPos));
                return sb.toString();
            } else {
                sb.append(str.substring(lastPos, pos));
            }

            pos++;
            char ch = str.charAt(pos);
            switch (ch) {
            case '\"':
            case '\\':
            case '/':
                sb.append(ch);
                break;

            case 'n':
                sb.append('\n');
                break;
            case 'r':
                sb.append('\r');
                break;
            case 't':
                sb.append('\t');
                break;
            case 'b':
                sb.append('\b');
                break;
            case 'f':
                sb.append('\f');
                break;
            case 'u':
                if (pos + 4 <= lengthLessOne) {
                    sb.append(String.valueOf((char) Integer.parseInt(str.substring(pos + 1, pos + 5), 16)));
                    pos += 4;
                    break;
                }
                // else fallthrough

            default:
                sb.append(ch);
            }
            lastPos = pos + 1;
            pos = str.indexOf((int) '\\', lastPos);
        }
    }


    // ---------- Parser

    /**
     * Разбирает каноническое представление джсон-строки и возвращает корневой джсон-объект, соответствующий переданной строке.
     *
     * @param source строковое представление
     * @return корневой объект
     * @throws JsonParsingException
     * @see #toString()
     */
    public static Json parse(String source) throws JsonParsingException {
        if (source == null) { return null; }
        Json root = new Json();
        char[] ca = source.toCharArray();
        try {
            fillNode(ca, root, 0);
        } catch (IndexOutOfBoundsException e) {
            throw new JsonParsingException("Unexpected end of data after position " + (source.length() - 1), ca, source.length() - 1);
        }
        return root;
    }

    /**
     * Записывает в переданный узел данные, лежащие в переданной строке с переданной позиции.
     * Возвращает позицию в переданной строке, после которой данные для этого узла закончились.
     *
     * @param source разбираемая строка
     * @param node узел, который надо наполнить
     * @param pos позиция, где начинаются данные узла
     * @return позиция, где данные узла закончились
     * @throws JsonParsingException
     */
    private static int fillNode(char[] source, Json node, int pos) throws JsonParsingException {
        pos = skipWhitespace(source, pos);
        char c = source[pos];
        if (c == '{') {
            // узел типа объект
            pos = skipWhitespace(source, pos + 1);
            c = source[pos];
            if (c != '}') {
                while (true) {
                    Json child = new Json();

                    // имя
                    if (c != '\"') {
                        throw new JsonParsingException("Quote expected before node name at position " + pos, source, pos);
                    } else {
                        int nameStart = pos + 1;
                        pos = findClosingQuoteOrDie(source, nameStart);
                        // читаем имя
                        String childName = new String(source, nameStart, pos - nameStart);
                        pos++;
                        if (source[pos] != ':') {
                            throw new JsonParsingException("Semicolon expected after node name " + Spell.get(childName) + " at position " + pos, source, pos);
                        }
                        node.getObjectValueOrSpawn().put(childName, child);
                        pos++;
                    }

                    // значение
                    pos = fillNode(source, child, pos);
                    pos = skipWhitespace(source, pos);
                    c = source[pos];
                    if (c == '}') {
                        break;
                    }
                    if (c != ',') {
                        throw new JsonParsingException("Unexpected character " + Spell.get(c) + " after Json object element (expected ‘}’ or ‘,’) at position " + pos, source, pos);
                    }
                    pos = skipWhitespace(source, pos + 1);
                    c = source[pos];
                }
            }
            pos++;
        } else if (c == '[') {
            pos = skipWhitespace(source, pos + 1);
            if (source[pos] != ']') {
                while (true) {
                    // значение
                    Json child = node.newArrayItem();
                    pos = fillNode(source, child, pos);
                    pos = skipWhitespace(source, pos);
                    c = source[pos];
                    if (c == ']') {
                        break;
                    }
                    if (c != ',') {
                        throw new JsonParsingException("Unexpected character " + Spell.get(c) + " after Json array element (expected  ‘]’ or ‘,’) at position " + pos, source, pos);
                    }
                    pos = skipWhitespace(source, pos + 1);
                }
            }
            pos++;
        } else if (c == '\"') {
            int valueStart = pos + 1;
            pos = findClosingQuoteOrDie(source, valueStart);
            node.set(deescapeString(new String(source, valueStart, pos - valueStart)));
            pos++;
        } else if (isNumericChar(c)) {
            int valueStart = pos;
            boolean fraction = c == '.';
            try {
                do {
                    pos++;
                    c = source[pos];
                    fraction |= c == '.';
                } while (isNumericChar(c));
            } catch (ArrayIndexOutOfBoundsException e) {
                // это вывалится в случае, если джсон просто число. Довольно редкий случай.
                // ничего не делаем, всё штатно
            }
            try {
                if (fraction) { node.set(new BigDecimal(new String(source, valueStart, pos - valueStart))); } else { node.set(Long.valueOf(new String(source, valueStart, pos - valueStart))); }
            } catch (Exception e) {
                throw new JsonParsingException("Invalid number " + Spell.get(new String(source, valueStart, pos - valueStart)) + " at position " + valueStart, source, valueStart);
            }
        } else if (c == 't' && pos + 3 < source.length && source[pos + 1] == 'r' && source[pos + 2] == 'u' && source[pos + 3] == 'e') {
            node.set(Boolean.TRUE);
            pos += 4;
        } else if (c == 'f' && pos + 4 < source.length && source[pos + 1] == 'a' && source[pos + 2] == 'l' && source[pos + 3] == 's' && source[pos + 4] == 'e') {
            node.set(Boolean.FALSE);
            pos += 5;
        } else if (c == 'n' && pos + 3 < source.length && source[pos + 1] == 'u' && source[pos + 2] == 'l' && source[pos + 3] == 'l') {
            node.drop();
            pos += 4;
        } else {
            throw new JsonParsingException("Unexpected character " + Spell.get(c) + " for a Json value at position " + pos, source, pos);
        }
        return pos;
    }

    private static int skipWhitespace(char[] source, int pos) throws JsonParsingException {
        while (Character.isWhitespace(source[pos])) {
            pos++;
        }
        return pos;
    }

    /**
     * Грязненько и простенько определяем, может ли символ содержаться в числе.
     *
     * @param c исследуемый символ
     * @return true, если может, false, если не может
     */
    private static boolean isNumericChar(char c) {
        return (c >= '0' && c <= '9') || c == '+' || c == '-' || c == '.' || c == 'E' || c == 'e';
    }

    private static int findClosingQuoteOrDie(char[] source, int stringStart) throws JsonParsingException {
        int pos = StringUtils.getClosingPosition(source, '\"', '\"', stringStart, '\\', null);
        if (pos < 0) {
            throw new JsonParsingException("Closing quote missing after position " + stringStart, source, stringStart);
        }
        return pos;
    }

    // --------- toString

    /**
     * @return джсон-объект в каноническом представлении
     * @see #parse(String)
     * @see #INDENT
     */
    @Override public String toString() {
        return toStringIndented(INDENT);
    }

    /**
     * @param indent отступ в пробелах от начала строки для одного уровня вложенности
     * @return джсон-объект в человекочитаемом представлении.
     * @see #parse(String)
     */
    public String toStringIndented(int indent) {
        StringBuilder sb = new StringBuilder(256);
        appendToString(sb, indent, 0);
        return sb.toString();
    }

    private static void appendIndent(StringBuilder sb, int indent, int level) {
        if (indent <= 0) { return;}
        sb.append('\n');
        int size = indent * level;
        while (size > 0) {
            sb.append(' ');
            size--;
        }
    }

    /**
     * Добавляет значение текущего джсон-узла к стрингбилдеру в каноническом представлении.
     *
     * @param sb
     */
    protected void appendToString(StringBuilder sb, int indent, int level) {
        type.appendToString(sb, indent, level, this);
    }


    // --------- ReadOnlySource suite

    /**
     * То же, что и {@link #getString(String)} — для {@link ReadOnlySource}.
     *
     * @param complexName составное имя интересующего поля
     * @return значение ключа
     * @throws SourceUnavailableException
     * @see #getString()
     */
    @Override public String get(String complexName) throws SourceUnavailableException {
        return getString(complexName);
    }

    @Override public Map<String, String> dump() throws SourceUnavailableException {
        Map<String, String> result = new LinkedHashMap<>();
        dumpChildrenTo(result, null);
        return result;
    }

    private void dumpChildrenTo(Map<String, String> map, String prefix) {
        if (type == Type.OBJECT) {
            Function<String, String> prefixF = prefix == null ? key -> key : key -> prefix + COMPLEX_DELIMITER + key;
            ((Map<String, Json>) value).forEach((key, valueJ) -> valueJ.dumpChildrenTo(map, prefixF.apply(key)));
        } else if (type == Type.ARRAY) {
            Function<String, String> prefixF = prefix == null ? key -> key : key -> prefix + COMPLEX_DELIMITER + key;
            IntContainer ci = new IntContainer();
            ((List<Json>) value).forEach(valueJ -> valueJ.dumpChildrenTo(map, prefixF.apply(String.valueOf(ci.getAndInc()))));
        } else {
            ThrowingFunction<String, String, RuntimeException> ifNotNull = value -> map.put(prefix, value);
            Box.with(getString()).mapAndGet(ifNotNull);
        }
    }

    @Override public boolean equals(Object o) {
        return this == o || (o instanceof Json && Objects.equals(value, ((Json) o).value));
    }

    @Override public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }

    /**
     * @return джсон-объект в каноническом представлении
     * @see #parse(String)
     * @see #INDENT
     */
    public String toDebugString() {
        return type.name() + ':' + Spell.get(value);
    }
}
