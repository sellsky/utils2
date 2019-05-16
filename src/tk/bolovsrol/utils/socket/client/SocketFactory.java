package tk.bolovsrol.utils.socket.client;

import java.io.IOException;
import java.net.Socket;

/**
 * Устройство для создания сокетов.
 * <p/>
 * Умеет создавать клиентские сокеты.
 */
public interface SocketFactory {

    /**
     * Создаёт и возвращает неподключённый клиентский сокет.
     *
     * @return новый клиентский сокет
     * @throws IOException
     */
    Socket newSocket() throws IOException;

    /**
     * Возвращает краткое (несколько букв) описание-идентификатор фабрики
     * для человеческого восприятия.
     *
     * @return краткое описание фабрики
     */
    String getCaption();
}