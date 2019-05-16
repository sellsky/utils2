package tk.bolovsrol.utils.log;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.log.out.Out;

import java.io.PrintWriter;
import java.util.Date;

/** Информации о строке или пачке строк лога. */
class LogData {

    public static final char[] NEWLINE_DELIMITER = {'\n'};

    /** Некоторые настройки печати. */
    private final Out out;
    /** Дата сообщения. */
    public final Date when;
    /** Уровень сообщения. */
    public final LogLevel level;
    /** Сообщение. Мы специально обрабатываем массивы и исключения. Остальное печатаем как toString(). */
    private final Object message;
    /** Если встретится исключение, печатать полный стектрейс в любом случае. */
    private final boolean forceStackTrace;

    /** Тред, из которого вызвали логгер. */
    public final String threadName;
    /** Стектрейс на момент вызова для определения методов и т. п.. */
    public StackTraceElement[] threadStackTraceOrNull;
    /** Флажок для автоматизации слияния частей сообщения в единую строку. */
    boolean printedAnything;
    /** Последний напечатанный символ или -1. */
    int lastChar;

    protected LogData(Out out, LogLevel level, Date date, Object message, boolean forceStackTrace, String threadName, StackTraceElement[] threadStackTraceOrNull) {
        this.out = out;
        this.when = date;
        this.level = level;
        this.message = message;
        this.forceStackTrace = forceStackTrace;
        this.threadName = threadName;
        this.threadStackTraceOrNull = threadStackTraceOrNull;
    }

    /**
     * Записывает данные в поток в виде одной строки.
     * Throwables со стектрейсом записываются в несколько строк.
     *
     * @param writer куда писать
     */
    public void print(PrintWriter writer) {
        printedAnything = false;
        try {
            printAbstract(writer, this.message);
        } finally {
            printIsDone(writer);
        }
    }

    private void printAbstract(PrintWriter writer, Object message) {
        if (message != null) {
            if (message.getClass().isArray()) {
                printArray(writer, (Object[]) message);
            } else if (message instanceof Throwable) {
                printThrowable(writer, (Throwable) message);
            } else {
                printString(writer, message.toString());
            }
        }
    }

    private void printString(PrintWriter writer, String message) {
        if (message.isEmpty()) {
            return;
        }
        beforePrinting(writer);
        String flattenMessage = StringUtils.flatten(message);
        writer.print(flattenMessage);
        lastChar = flattenMessage.charAt(flattenMessage.length() - 1);
    }

    private void printArray(PrintWriter writer, Object[] messages) {
        for (Object message : messages) {
            printAbstract(writer, message);
        }
    }

    public void printThrowable(PrintWriter writer, Throwable throwable) {
        if (forceStackTrace || level.isFullStackTrace() || StackTracedThrowables.isStackTraceFor(throwable)) {
            beforePrinting(writer);
            throwable.printStackTrace(writer);
            lastChar = ')'; // читерство :)
        } else {
            printString(writer, Spell.get(throwable));
        }
    }

    private void beforePrinting(PrintWriter writer) {
        if (!printedAnything) {
            writer.write(getPrefix());
            printedAnything = true;
        } else {
            switch (lastChar) {
            case ' ':
                break;
            case '.':
            case ':':
            case ';':
            case ',':
            case '!':
            case '?':
                writer.write(' ');
                break;
            default:
                writer.write(". ");
                break;
            }
            lastChar = ' ';
        }
    }

    private void printIsDone(PrintWriter writer) {
        if (printedAnything) {
            writer.println();
        }
    }

    protected String getPrefix() {
        StringBuilder sb = new StringBuilder(128);
        sb.append(out.dateFormat.format(when));
        sb.append(',');
        sb.append(level.name());
        if (Out.levelTextWidth > 0) {
            int count = Out.levelTextWidth - level.name().length();
            while (--count >= 0) sb.append(' '); }
        sb.append(',');
        if (Out.THREADS) {
            sb.append(threadName);
            sb.append(',');
        }

        if (threadStackTraceOrNull != null) {
            int deep = 2; // как минимум 2 верхних уровня занимают служебные методы LogDome#write(...) и Thread#getStackTrace().
            try {
                while (UtilityLogClasses.isLoggerClass(threadStackTraceOrNull[deep].getClassName())) {
                    deep++;
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {
                deep = threadStackTraceOrNull.length - 1;
            }
            if (Out.CLASSES) {
                String callerClassName = threadStackTraceOrNull[deep].getClassName();
                sb.append(callerClassName.substring(callerClassName.lastIndexOf((int) '.') + 1));
                sb.append(',');
            }
            if (Out.METHODS) {
                sb.append(threadStackTraceOrNull[deep].getMethodName());
                sb.append(',');
            }
        }
        return sb.toString();
    }

}