package tk.bolovsrol.utils.stringserializer;

import tk.bolovsrol.utils.time.TwofacedTime;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Правила преобразования поля в строку и обратно.
 * <p>
 * Примитивы и объекты кодируются по-разному, но всё вроде бы очевидно.
 * Каждому сериализатору нужен соответствующий десериализатор.
 */
final class Codec {

	static final Map<Class<?>, PrimitiveSerializer> PRIMITIVE_SERIALIZERS = new LinkedHashMap<>();
	static final Map<Class<?>, ObjectSerializer> OBJECT_SERIALIZERS = new LinkedHashMap<>();
	static final Map<Class<?>, PrimitiveDeserealizer> PRIMITIVE_DESEREALIZERS = new LinkedHashMap<>();
	static final Map<Class<?>, ObjectDeserealizer> OBJECT_DESEREALIZERS = new LinkedHashMap<>();

	static {
		PRIMITIVE_SERIALIZERS.put(int.class, (source, field) -> Integer.toString(field.getInt(source)));
		PRIMITIVE_SERIALIZERS.put(long.class, (source, field) -> Long.toString(field.getLong(source)));
		PRIMITIVE_SERIALIZERS.put(char.class, (source, field) -> Character.toString(field.getChar(source)));
		PRIMITIVE_SERIALIZERS.put(byte.class, (source, field) -> Byte.toString(field.getByte(source)));
		PRIMITIVE_SERIALIZERS.put(boolean.class, (source, field) -> Boolean.toString(field.getBoolean(source)));
		PRIMITIVE_SERIALIZERS.put(float.class, (source, field) -> Float.toString(field.getFloat(source)));
		PRIMITIVE_SERIALIZERS.put(double.class, (source, field) -> Double.toString(field.getDouble(source)));
		PRIMITIVE_SERIALIZERS.put(short.class, (source, field) -> Short.toString(field.getShort(source)));

		OBJECT_SERIALIZERS.put(String.class, value -> { try { return URLEncoder.encode((String) value, "UTF-8"); } catch (UnsupportedEncodingException wontHappen) { throw new RuntimeException(wontHappen); } });
		OBJECT_SERIALIZERS.put(Integer.class, Object::toString);
		OBJECT_SERIALIZERS.put(Long.class, Object::toString);
		OBJECT_SERIALIZERS.put(Enum.class, value -> ((Enum<?>) value).name());
		OBJECT_SERIALIZERS.put(BigDecimal.class, value -> ((BigDecimal) value).toPlainString());
		OBJECT_SERIALIZERS.put(Date.class, value -> Const.DATE_FORMAT_TL.get().format((Date) value));
		OBJECT_SERIALIZERS.put(TwofacedTime.class, value -> ((TwofacedTime) value).getHumanReadable());
		OBJECT_SERIALIZERS.put(StringSerializable.class, value -> StringSerializer.serialize((StringSerializable) value));

		PRIMITIVE_DESEREALIZERS.put(int.class, (target, field, value) -> field.setInt(target, Integer.parseInt(value)));
		PRIMITIVE_DESEREALIZERS.put(long.class, (target, field, value) -> field.setLong(target, Long.parseLong(value)));
		PRIMITIVE_DESEREALIZERS.put(char.class, (target, field, value) -> field.setChar(target, value.charAt(0)));
		PRIMITIVE_DESEREALIZERS.put(byte.class, (target, field, value) -> field.setByte(target, Byte.parseByte(value)));
		PRIMITIVE_DESEREALIZERS.put(boolean.class, (target, field, value) -> field.setBoolean(target, Boolean.parseBoolean(value)));
		PRIMITIVE_DESEREALIZERS.put(float.class, (target, field, value) -> field.setFloat(target, Float.parseFloat(value)));
		PRIMITIVE_DESEREALIZERS.put(double.class, (target, field, value) -> field.setDouble(target, Double.parseDouble(value)));
		PRIMITIVE_DESEREALIZERS.put(short.class, (target, field, value) -> field.setShort(target, Short.parseShort(value)));

		OBJECT_DESEREALIZERS.put(String.class, (fieldClass, value) -> URLDecoder.decode(value, "UTF-8"));
		OBJECT_DESEREALIZERS.put(Long.class, (fieldClass, value) -> Long.valueOf(value));
		OBJECT_DESEREALIZERS.put(Integer.class, (fieldClass, value) -> Integer.valueOf(value));
		OBJECT_DESEREALIZERS.put(Enum.class, (fieldClass, value) -> Enum.valueOf((Class) fieldClass, value));
		OBJECT_DESEREALIZERS.put(BigDecimal.class, (fieldClass, value) -> new BigDecimal(value));
		OBJECT_DESEREALIZERS.put(Date.class, (fieldClass, value) -> Const.DATE_FORMAT_TL.get().parse(value));
		OBJECT_DESEREALIZERS.put(TwofacedTime.class, (fieldClass, value) -> TwofacedTime.parseHumanReadable(value));
		OBJECT_DESEREALIZERS.put(StringSerializable.class, (fieldClass, value) -> StringDeserializer.deserialize(value));
	}

	private Codec() {}

	@FunctionalInterface public interface PrimitiveDeserealizer {
		void deserialize(StringSerializable target, Field field, String value) throws IllegalArgumentException, IllegalAccessException;
	}

	@FunctionalInterface public interface ObjectDeserealizer {
		Object deserialize(Class<?> fieldClass, String value) throws Exception;
	}

	@FunctionalInterface public interface PrimitiveSerializer {
		String serialize(StringSerializable source, Field field) throws IllegalAccessException;
	}

	@FunctionalInterface public interface ObjectSerializer {
		String serialize(Object value) throws IllegalAccessException;
	}
}
