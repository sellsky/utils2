package tk.bolovsrol.utils.log.out;


import tk.bolovsrol.utils.log.LogLevel;
import tk.bolovsrol.utils.log.LogWriter;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.reflectiondump.ReflectionDump;

import java.text.SimpleDateFormat;

/**
 * Контейнер с конфигурацией и оперативными данными точки вывода.
 * <p/>
 * Также содержит статические поля — значения по умолчанию, вычитанные из конфига.
 */
public class Out {

    public static final String LOG_LEVEL = "log.defaultLevel";
    public static final String LOG_THREADS = "log.threads";
    public static final String LOG_CLASSES = "log.classes";
    public static final String LOG_METHODS = "log.methods";
    public static final String LOG_DATE_FORMAT = "log.dateFormat";

    public static final boolean THREADS = Cfg.getBoolean(LOG_THREADS, true);
    public static final boolean CLASSES = Cfg.getBoolean(LOG_METHODS, false);
    public static final boolean METHODS = Cfg.getBoolean(LOG_METHODS, false);
    public static final boolean DO_STACKTACE = CLASSES | METHODS;

	public static final LogLevel DEF_LEVEL = Cfg.getEnum(LOG_LEVEL, LogLevel.TRACE, null);
	public static final String DEF_DATE_FORMAT_STRING = Cfg.get(LOG_DATE_FORMAT, "yyyy-MM-dd','HH:mm:ss.SSS");

	public static int levelTextWidth = 0;

    public LogLevel level = DEF_LEVEL;
    public SimpleDateFormat dateFormat = new SimpleDateFormat(DEF_DATE_FORMAT_STRING);
    public LogWriter writer;

    @Override public String toString() {
        return ReflectionDump.getFor(this);
    }

}
