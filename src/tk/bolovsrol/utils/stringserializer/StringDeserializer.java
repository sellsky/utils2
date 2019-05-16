package tk.bolovsrol.utils.stringserializer;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.UnexpectedBehaviourException;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

/** Статическое устройство для воссоздания объектов {@link StringSerializable}. */
public final class StringDeserializer {

	private StringDeserializer() {
	}

    /**
	 * Воссоздаёт объект, описываемый переданной строкой.
	 *
	 * @param item
	 * @return десериализованный entity
	 * @throws UnexpectedBehaviourException ошибка воссоздания
	 */
	@SuppressWarnings({"unchecked"})
	public static <E extends StringSerializable> E deserialize(String item) throws UnexpectedBehaviourException {
        try {
            int po = item.indexOf((int) Const.CLASS_DELIMITER);
            Class<E> entityClass = (Class<E>) Class.forName(item.substring(0, po));
            Constructor<E> constructor = entityClass.getConstructor();
            constructor.setAccessible(true);
            E result = constructor.newInstance();

			String[] fieldAndValues = StringUtils.parseDelimited(item.substring(po + 1), Const.CONNECTOR_CHAR, Const.MASK_CHAR, null);
			for (String fieldAndValue : fieldAndValues) {
				int eqPo = fieldAndValue.indexOf(Const.EQ_CHAR);
				String fieldName = fieldAndValue.substring(0, eqPo);
				String fieldValue = fieldAndValue.substring(eqPo + 1);
				Field field = pickField(entityClass, fieldName);
				field.setAccessible(true);
				putValue(result, field, fieldValue);
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new UnexpectedBehaviourException("Cannot deserialize item " + Spell.get(item) + ". " + Spell.get(e));
        }
    }

	private static Field pickField(Class<?> entityClass, String fieldName) throws NoSuchFieldException {
		while (true) {
			try {
				try {
					return entityClass.getDeclaredField(fieldName);
				} catch (NoSuchFieldException e) {
					// возможно, поле определено под старым именем
					for (Field f : entityClass.getDeclaredFields()) {
						PreviousNames c = f.getAnnotation(PreviousNames.class);
						if (c != null) {
							for (String previousName : c.value()) {
								if (previousName.equals(fieldName)) {
									// gotcha!
									return f;
								}
							}
						}
					}
					throw e;
				}
			} catch (NoSuchFieldException e) {
				entityClass = entityClass.getSuperclass();
				if (entityClass == null || entityClass == Object.class) {
					throw e;
				}
			}
		}
	}

	@SuppressWarnings({"RawUseOfParameterizedType", "unchecked", "rawtypes"})
	private static void putValue(StringSerializable target, Field field, String value) throws IllegalAccessException, UnsupportedOperationException, StringDeserializingException {
		Class<?> fieldClass = field.getType();
		if (fieldClass.isPrimitive()) {
			putPrimitiveValue(target, field, value, fieldClass);
		} else if (fieldClass.isArray()) {
			putObjectArrayValue(target, field, value, fieldClass);
		} else {
			putObjectValue(target, field, value, fieldClass);
		}
	}

	private static void putPrimitiveValue(StringSerializable target, Field field, String value, Class<?> fieldClass) throws IllegalAccessException {
		for (Map.Entry<Class<?>, Codec.PrimitiveDeserealizer> entry : Codec.PRIMITIVE_DESEREALIZERS.entrySet()) {
			if (entry.getKey().isAssignableFrom(fieldClass)) {
				entry.getValue().deserialize(target, field, value);
				return;
			}
		}
		throw new UnsupportedOperationException("Don't know how to deserialize primitive " + Spell.get(field));
	}

	private static void putObjectArrayValue(StringSerializable target, Field field, String value, Class<?> fieldClass) throws IllegalAccessException, StringDeserializingException {
		Class<?> componentType = fieldClass.getComponentType();
		for (Map.Entry<Class<?>, Codec.ObjectDeserealizer> entry : Codec.OBJECT_DESEREALIZERS.entrySet()) {
			if (entry.getKey().isAssignableFrom(componentType)) {
				try {
					String[] arrayValues = StringUtils.parseDelimited(value, Const.ARRAY_ITEM_DELIMITER);
					int len = arrayValues.length;
					Object arr = Array.newInstance(componentType, len);
					for (int i = 0; i < len; i++) {
						Array.set(arr, i, entry.getValue().deserialize(componentType, arrayValues[i]));
					}
					field.set(target, arr);
					return;
				} catch (IllegalAccessException | IllegalArgumentException e) {
					throw e;
				} catch (Exception e) {
					throw StringDeserializingException.forField(field, value, entry.getKey(), e);
				}
			}
		}
		throw new UnsupportedOperationException("Don't know how to deserialize array of " + Spell.get(componentType));
	}

	private static void putObjectValue(StringSerializable target, Field field, String value, Class<?> fieldClass) throws IllegalAccessException, StringDeserializingException {
		for (Map.Entry<Class<?>, Codec.ObjectDeserealizer> entry : Codec.OBJECT_DESEREALIZERS.entrySet()) {
			if (entry.getKey().isAssignableFrom(fieldClass)) {
				try {
					field.set(target, entry.getValue().deserialize(fieldClass, value));
					return;
				} catch (IllegalAccessException | IllegalArgumentException e) {
					throw e;
				} catch (Exception e) {
					throw StringDeserializingException.forField(field, value, entry.getKey(), e);
				}
			}
		}
		throw new UnsupportedOperationException("Don't know how to deserialize " + Spell.get(fieldClass));
	}
}
