package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.Spell;

/**
 * Пользовательское исключение на случай обработки хттп-запроса,
 * содержащее в себе {@link HttpStatus статус} и, опционально, коротенькое сообщение для ответа на запрос.
 */
public class HttpRequestProcessingException extends Exception {
    private final HttpStatus responseStatus;
    private final String responseMessage;

    public HttpRequestProcessingException(String message, HttpStatus responseStatus, String responseMessage) {
        super(message);
        this.responseStatus = responseStatus;
        this.responseMessage = responseMessage;
    }

    public HttpRequestProcessingException(String message, Throwable cause, HttpStatus responseStatus, String responseMessage) {
        super(message, cause);
        this.responseStatus = responseStatus;
        this.responseMessage = responseMessage;
    }

    public HttpRequestProcessingException(Throwable cause, HttpStatus responseStatus, String responseMessage) {
        super(cause);
        this.responseStatus = responseStatus;
        this.responseMessage = responseMessage;
    }

    public HttpRequestProcessingException(String message, HttpStatus responseStatus) {
        this(message, responseStatus, message);
    }

    public HttpRequestProcessingException(String message, Throwable cause, HttpStatus responseStatus) {
        this(message, cause, responseStatus, message);
    }

    public HttpRequestProcessingException(Throwable cause, HttpStatus responseStatus) {
        this(cause, responseStatus, cause.getMessage());
    }

    public HttpStatus getResponseStatus() {
        return responseStatus;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    @Override public String toString() {
        return super.toString() + "; responseStatus " + Spell.get(responseStatus) + ", responseMessage " + Spell.get(responseMessage);
    }
}
