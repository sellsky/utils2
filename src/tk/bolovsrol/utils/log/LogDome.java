package tk.bolovsrol.utils.log;

import tk.bolovsrol.utils.log.out.Out;
import tk.bolovsrol.utils.log.out.OutParser;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.properties.ReadOnlyProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

/**
 * Лёгкий лог.
 * <p/>
 * Формат вывода строк:
 * &lt;дата&gt;[,&lt;уровень&gt;][,&lt;тред&gt;][,&lt;класс&gt;[,&lt;метод&gt;]],&lt;сообщение&gt;
 * <p/>
 * По умолчанию выводится строка вида:
 * &lt;дата&gt;,&lt;уровень&gt;,&lt;класс&gt;,&lt;сообщение&gt;
 * <p/>
 * Класс содержит статические методы для вывода в лог с различной детализацией.
 * <p/>
 * Инстанция этого класса создаётся автоматически статическим классом {@link Log},
 * и все запросы прозрачно транслируются. Для лёгких задач рекомендуется пользоваться им.
 * <p/>
 * Если же необходимо вести более одного лога в пределах одной Java-машины, нужно вручную создать
 * и отконфигурировать экземпляры этого класса и делать вывод лога уже через них.
 */
public class LogDome {

    static {
        UtilityLogClasses.registerLoggerClass(LogDome.class);
    }

    /** Ключ пропертей, в которых ожидается настройка. */
    public static final String LOG_OUT = "log.out";
    public static final String DEF_OUT = Cfg.get(LOG_OUT, "stream:stdout");


    // ----- Параметры.
    /** Настройки логгера. */
    private final Collection<Out> outs;
    private final LogLevel lowestLevel;

    private Date latestDate = null;

    /**
     * Создаёт логгер для указанного вывода.
     * <p/>
     * Переданные настройки парсятся при помощи {@link OutParser#parse(String)}.
     *
     * @param outLine настройки
     */
    public LogDome(String outLine) {
        this(OutParser.parse(outLine));
    }

    /**
     * Создаёт логгер для указанного вывода.
     *
     * @param out вывод
     */
    public LogDome(Out out) {
        this(Collections.singleton(out));
    }

    /**
     * Создаёт логгер для указанных выводов.
     *
     * @param outs выводы
     */
    public LogDome(Collection<Out> outs) {
        this.outs = outs;
        LogLevel lowestLevel = LogLevel.EXCEPTION;
        for (Out out : outs) {
            if (out.level.ordinal() <= lowestLevel.ordinal()) {
                lowestLevel = out.level;
            }
        }
        this.lowestLevel = lowestLevel;
    }

    /**
     * Если в переданных пропертях <code>cfg</code> содержится ключ
     * с именем {@link #LOG_OUT}, то метод вернёт новый логгер,
     * созданный для этого ключа.
     * <p/>
     * Иначе метод вернёт лог по умолчанию <code>defaultLog</code>
     * <p/>
     * Предполагается использование этого метода для создания логгеров
     * при иерархическом модульном разделении логов.
     *
     * @param cfg        конфигурация для проверки
     * @param defaultLog лог по умолчанию
     * @return актуальный логгер
     */
    public static LogDome coalesce(ReadOnlyProperties cfg, LogDome defaultLog) {
        return coalesce(cfg.get(LOG_OUT), defaultLog);
    }

    /**
     * Возвращает лог, сформированный на основании лога по умолчаню в соответствии
     * с настройками в переданной строке.
     * <p/>
     * Если в качестве строки передан нул, то возвращается лог по умолчанию как есть.
     * В остальных случаях возвращается новый объект.
     * <p/>
     * Если в переданной строке первый символ плюс «+», то указанные
     * в строке новые точки вывода добавляются к точкам исходного лога.
     * В остальных случаях используются только точки, описанные в переданной
     * строке.
     *
     * @param outlineOrNull
     * @param defaultLog
     * @return логгер
     */
    public static LogDome coalesce(String outlineOrNull, LogDome defaultLog) {
        if (outlineOrNull == null) {
            return defaultLog;
        }
        ArrayList<Out> outs = new ArrayList<>();
        if (outlineOrNull.startsWith("+")) {
            outs.addAll(defaultLog.outs);
            outlineOrNull = outlineOrNull.substring(1).trim();
        }
        OutParser.parse(outlineOrNull, outs);
        outs.trimToSize();
        return new LogDome(outs);
    }

    // -- trace --
    public void trace(Object message) {
        write(LogLevel.TRACE, message);
    }

    public void trace(Object... messages) {
        write(LogLevel.TRACE, messages);
    }

    // -- info --
    public void info(Object message) {
        write(LogLevel.INFO, message);
    }

    public void info(Object... messages) {
        write(LogLevel.INFO, messages);
    }

    // -- hint --
    public void hint(Object message) {
        write(LogLevel.HINT, message);
    }

    public void hint(Object... messages) {
        write(LogLevel.HINT, messages);
    }

    // -- warning --
    public void warning(Object message) {
        write(LogLevel.WARNING, message);
    }

    public void warning(Object... messages) {
        write(LogLevel.WARNING, messages);
    }

    // -- exception --
    public void exception(Object message) {
        write(LogLevel.EXCEPTION, message);
    }

    public void exception(Object... messages) {
        write(LogLevel.EXCEPTION, messages);
    }

    // -- write --

    /**
     * Записывает в лог строковое представление переданного объекта-сообщения.
     *
     * @param level   уровень записи
     * @param message сообщение
     */
    public void write(LogLevel level, Object message) {
        write(level, false, message);
    }

    /**
     * Записывает в лог строковое представление переданных объектов-сообщений.
     *
     * @param level    уровень записи
     * @param messages сообщения
     */
    public void write(LogLevel level, Object... messages) {
        write(level, false, (Object) messages);
    }

    /**
     * Записывает в лог строковое представление переданных объектов-сообщений.
     *
     * @param level           уровень записи
     * @param forceStackTrace показывать стектрейс для переданных {@link Throwable}.
     * @param messages        сообщения
     */
    public void write(LogLevel level, boolean forceStackTrace, Object... messages) {
        write(level, forceStackTrace, (Object) messages);
    }

    /**
     * Записывает в лог строковое представление переданного объекта-сообщения.
     *
     * @param level           уровень записи
     * @param forceStackTrace показывать стектрейс для переданных {@link Throwable}.
     * @param message         сообщение
     */
    public void write(LogLevel level, boolean forceStackTrace, Object message) {
        if (message != null && level.ordinal() >= lowestLevel.ordinal()) {
            Date now = new Date();
            latestDate = now;
            String threadName = Thread.currentThread().getName();
            StackTraceElement[] threadStackTrace = Out.DO_STACKTACE ? new Throwable().getStackTrace() : null;
            for (Out out : outs) {
                if (level.ordinal() >= out.level.ordinal()) {
                    out.writer.write(new LogData(out, level, now, message, forceStackTrace, threadName, threadStackTrace));
                }
            }
        }
    }

    /**
     * Возвращает дату нижней строчки в логе.
     * Точнее говоря, это дата прошлого выведенного в лог пакета.
     * <p/>
     * Если в лог ничего не выводили, то null.
     * <p/>
     * Можно использовать для выведения в лог строк «Я ещё жив!»
     * через некоторый интервал.
     *
     * @return дата прошлого выведенного в лог пакета или null.
     */
    public Date getLatestDate() {
        return latestDate;
    }

    /**
     * Проверяет, разрешают ли текущие настройки сделать запись
     * указанного уровня детализации в лог.
     *
     * @param level желаемый уровень
     * @return true, если и только если можно
     */
    public boolean isAllowed(LogLevel level) {
        return level.ordinal() >= lowestLevel.ordinal();
    }
}
