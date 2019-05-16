package tk.bolovsrol.utils;

import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.properties.PropertyException;
import tk.bolovsrol.utils.properties.PropertyNotFoundException;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;

/**
 * Класс контролирует, что запущено не более 1 копии локальной машинки.
 * <p/>
 * Контролирует, пытаясь захватить серверный порт.
 */
public final class LocalInstance {
    private LocalInstance() {
    }

    private static ServerSocket ss = null;

    /**
     * Проверяет уникальность запущенной копии по порту для контроля
     * из проперти <code>guard.port</code> общего конфига.
     * <p/>
     * Если номер порта 0 или меньше, то считает себя уникальным всегда.
     *
     * @return true, если это единственная запущенная копия, false, если нет.
     * @throws IOException
     * @throws PropertyNotFoundException проперть <code>guard.port</code> не установлена
     * @see #isTheOnlyLocalInstance(int)
     */
    public static boolean isTheOnlyLocalInstance() throws IOException, PropertyException {
		return isTheOnlyLocalInstance(Cfg.getIntegerOrDie("guard.port"));
	}

    /**
     * Проверяет уникальность запущенной копии по переданному номеру tcp-порта.
     * <p/>
     * Если номер порта 0 или меньше, то считает себя уникальным всегда.
     *
     * @param port tcp-порт для контроля
     * @return true, если это единственная запущенная копия, false, если нет.
     * @throws IOException
     */
    public static boolean isTheOnlyLocalInstance(int port) throws IOException {
        if (ss != null || port <= 0) {
            return true;
        }

        try {
            ss = new ServerSocket(port, 1);
        } catch (BindException e) {
            return false;
        }
        return true;
    }

    /**
     * Проверяет уникальность запущенной копии по переданному номеру tcp-порта.
     * Если это не единственная запущенная копия, выкидывает {@link IllegalStateException}.
     * <p/>
     * Если номер порта 0 или меньше, то считает себя уникальным всегда.
     *
     * @param port tcp-порт для контроля
     * @throws IOException
     * @throws IllegalStateException это не единственная запущенная копия.
     * @see #isTheOnlyLocalInstance(int)
     */
    public static void checkTheOnlyLocalInstanceOrDie(int port) throws IOException, IllegalStateException {
        if (!isTheOnlyLocalInstance(port)) {
            throw new IllegalStateException("Another running instance detected.");
        }
    }
}
