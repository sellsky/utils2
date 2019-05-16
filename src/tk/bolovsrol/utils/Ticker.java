package tk.bolovsrol.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Простейший класс для замера и журналирования времени выполнения некоторого алгоритма.
 * Пример использования:
 * <pre>
 * Ticker ticker = new Ticker();
 * ...{code}...
 * Log.trace("Code done " + ticker); // Выведет в журнал: "Code done (1,23ms)"</pre> */
public class Ticker {
	private static DecimalFormat format = new DecimalFormat( "(#,##0.00ms)",
			DecimalFormatSymbols.getInstance(Locale.ROOT) );

	private long tick;
	public Ticker() { reset(); }

	public void reset() { tick = System.nanoTime(); }
	@Override public String toString() { return toString(tick); }

	/** Формирует строку вида "(1,23ms)" с временем от указанного до текущего момента. */
	public static String toString(long tick) {
		return format.format( (System.nanoTime() - tick) / 1_000_000f ); }
}


