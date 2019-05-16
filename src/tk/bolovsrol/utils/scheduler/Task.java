package tk.bolovsrol.utils.scheduler;

import java.util.Date;

/** Таск, который надо выполнить. */
public interface Task {

    /**
     * Выполняет таск. Если метод вернёт не нул, то таск будет
     * снова помещён в расписание в эту дату.
     *
     * @return дату, по наступлению которого нужно снова запустить таск, или нул, если не нужно запускать
     */
    Date execute();

    /**
     * Создаёт таск, который выполнит переданную задачу однажды (вернёт нул в качестве следующей даты).
     *
     * @param runnable
     * @return одноразовый таск
     */
    static Task once(Runnable runnable) {
        return () -> {
            runnable.run();
            return null;
        };
    }
}
