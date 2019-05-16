package tk.bolovsrol.utils.stringserializer;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.StringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/** Статическое устройство для дешёвой сериализации объектов {@link StringSerializable}. */
public final class StringSerializer {

	private StringSerializer() {
	}

	/**
	 * Сериализирует объект.
	 *
	 * @param source
	 * @return сериализированный список
	 * @throws IllegalAccessException
	 */
	public static String serialize(StringSerializable source) throws IllegalAccessException {
		Class<?> entityClass = source.getClass();

		StringDumpBuilder sdb = new StringDumpBuilder(Const.CONNECTOR_CHAR_AS_STRING);
		do {
			Field[] fields = entityClass.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				if ((field.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) == 0) {
					String value = retrieveValue(source, field);
					if (value != null) {
						sdb.append(field.getName() + Const.EQ_CHAR + StringUtils.mask(value, Const.MASK_CHAR, Const.CONNECTOR_CHAR_AS_ARRAY));
					}
				}
			}
		} while ((entityClass = entityClass.getSuperclass()) != null && entityClass != Object.class);

		return source.getClass().getName() + Const.CLASS_DELIMITER + sdb.toString();
	}

	private static String retrieveValue(StringSerializable source, Field field) throws IllegalAccessException, UnsupportedOperationException {
		Class<?> fieldClass = field.getType();
		if (fieldClass.isPrimitive()) {
			return retrievePrimitiveValue(source, field, fieldClass);
		} else {
			Object fieldValue = field.get(source);
			if (fieldValue == null) {
				return null;
			} else if (fieldClass.isArray()) {
				return retrieveObjectArrayValue(fieldClass, fieldValue);
			} else {
				return retrieveObjectValue(fieldClass, fieldValue);
			}
		}
	}

	private static String retrievePrimitiveValue(StringSerializable source, Field field, Class<?> fieldClass) throws IllegalAccessException {
		for (Map.Entry<Class<?>, Codec.PrimitiveSerializer> entry : Codec.PRIMITIVE_SERIALIZERS.entrySet()) {
			if (entry.getKey().isAssignableFrom(fieldClass)) {
				return entry.getValue().serialize(source, field);
			}
		}
		throw new UnsupportedOperationException("Don't know how to serialize primitive " + Spell.get(fieldClass.toString()));
	}

	private static String retrieveObjectValue(Class<?> fieldClass, Object value) throws IllegalAccessException {
		for (Map.Entry<Class<?>, Codec.ObjectSerializer> entry : Codec.OBJECT_SERIALIZERS.entrySet()) {
			if (entry.getKey().isAssignableFrom(fieldClass)) {
				return entry.getValue().serialize(value);
			}
		}
		throw new UnsupportedOperationException("Don't know how to serialize " + Spell.get(fieldClass.toString()));
	}

	private static String retrieveObjectArrayValue(Class<?> fieldClass, Object array) throws IllegalAccessException {
		Class<?> componentType = fieldClass.getComponentType();
		for (Map.Entry<Class<?>, Codec.ObjectSerializer> entry : Codec.OBJECT_SERIALIZERS.entrySet()) {
			if (entry.getKey().isAssignableFrom(componentType)) {
				StringDumpBuilder sdb = new StringDumpBuilder(Const.ARRAY_ITEM_DELIMITER_STR);
				int len = Array.getLength(array);
				for (int i = 0; i < len; i++) {
					sdb.append(entry.getValue().serialize(Array.get(array, i)));
				}
				return sdb.toString();
			}
		}
		throw new UnsupportedOperationException("Don't know how to serialize array of " + Spell.get(componentType.toString()));
	}
}
