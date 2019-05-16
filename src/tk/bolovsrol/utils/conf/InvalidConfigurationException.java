package tk.bolovsrol.utils.conf;

/**
 * В конфигурации какая-либо ошибка.
 * Исключение выкидывается при инициализации конфигурации.
 */
public class InvalidConfigurationException extends Exception {

    public InvalidConfigurationException(String message) {
        super(message);
    }

    public InvalidConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidConfigurationException(Throwable cause) {
        super(cause);
    }
}
