package tk.bolovsrol.utils.stringserializer;

import java.text.SimpleDateFormat;

/** Константы сериализатора. */
final class Const {
	/** Формат даты для полей типа {@link java.util.Date} */
	static final ThreadLocal<SimpleDateFormat> DATE_FORMAT_TL = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS"));

	/**
	 * Символ, отделяющий название класса от его полей.
	 */
	static final char CLASS_DELIMITER = ':';

	static final char ARRAY_ITEM_DELIMITER = ';';
	static final String ARRAY_ITEM_DELIMITER_STR = String.valueOf(ARRAY_ITEM_DELIMITER);
	static final char EQ_CHAR = '=';
	static final char CONNECTOR_CHAR = '&';
	static final char[] CONNECTOR_CHAR_AS_ARRAY = new char[]{CONNECTOR_CHAR};
	static final String CONNECTOR_CHAR_AS_STRING = String.valueOf(CONNECTOR_CHAR_AS_ARRAY);
	static final char MASK_CHAR = '\\';

	private Const() {
	}
}
