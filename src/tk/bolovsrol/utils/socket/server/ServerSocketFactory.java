package tk.bolovsrol.utils.socket.server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Устройство для создания сокетов.
 * <p/>
 * Умеет создавать клиентские и серверные сокеты. Два в одном, так сказать.
 * Инициализация, если она вообще нужна, во многом общая для обоих типов.
 */
public interface ServerSocketFactory {

    /**
     * Создаёт и возвращает незабинденный серверный сокет.
     *
     * @return новый серверный сокет
     * @throws IOException
     */
    ServerSocket newServerSocket() throws IOException;

    /**
     * Возвращает краткое (несколько букв) описание-идентификатор фабрики
     * для человеческого восприятия.
     *
     * @return краткое описание фабрики
     */
    String getCaption();
}