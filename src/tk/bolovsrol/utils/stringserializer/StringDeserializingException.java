package tk.bolovsrol.utils.stringserializer;

import tk.bolovsrol.utils.Spell;

import java.lang.reflect.Field;

public class StringDeserializingException extends Exception {

	public StringDeserializingException(String message, Throwable cause) {
		super(message, cause);
	}

	public static StringDeserializingException forField(Field field, String value, Class<?> asClass, Throwable cause) {
		return new StringDeserializingException("Error deserializing field " + Spell.get(field) + ", cannot parse " + Spell.get(value) + " as " + asClass.getSimpleName(), cause);
	}
}
