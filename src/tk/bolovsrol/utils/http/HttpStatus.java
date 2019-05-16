package tk.bolovsrol.utils.http;

/**
 * Статус-строка для {@link HttpResponse}: цифровой код <code>statusCode</code> и комментарий <code>reasonPhrase</code>.
 * <p>
 * Класс содержит предопределённые стандартные ответы, которые уместно использовать в создаваемых HttpResponse.
 * <p>
 * Однако это не енум, нельзя сравнивать объекты этого класса простым равенством: <code>reasonPhrase</code> ответа, вообще говоря,
 * может быть произвольным, и при разборе входящего HttpResponse создаётся новый экземпляр класса
 * с <code>reasonPhrase</code>, предоставленным удалённым сервером.
 * <p>
 * Класс считает, что два ответа с одинаковым <code>statusCode</code> равны независимо от <code>reasonPhrase</code>.
 */
public class HttpStatus implements Comparable<HttpStatus> {

    public static final HttpStatus _100_CONTINUE = new HttpStatus(100, "Continue");
    public static final HttpStatus _101_SWITCHING_PROTOCOLS = new HttpStatus(101, "Switching Protocols");
    public static final HttpStatus _200_OK = new HttpStatus(200, "OK");
    public static final HttpStatus _201_CREATED = new HttpStatus(201, "Created");
    public static final HttpStatus _202_ACCEPTED = new HttpStatus(202, "Accepted");
    public static final HttpStatus _203_NONAUTHORITATIVE_INFORMATION = new HttpStatus(203, "Non-Authoritative Information");
    public static final HttpStatus _204_NO_CONTENT = new HttpStatus(204, "No Content");
    public static final HttpStatus _205_RESET_CONTENT = new HttpStatus(205, "Reset Content");
    public static final HttpStatus _206_PARTIAL_CONTENT = new HttpStatus(206, "Partial Content");
    public static final HttpStatus _300_MULTIPLE_CHOICES = new HttpStatus(300, "Multiple Choices");
    public static final HttpStatus _301_MOVED_PERMANENTLY = new HttpStatus(301, "Moved Permanently");
    public static final HttpStatus _302_FOUND = new HttpStatus(302, "Found");
    public static final HttpStatus _303_SEE_OTHER = new HttpStatus(303, "See Other");
    public static final HttpStatus _304_NOT_MODIFIED = new HttpStatus(304, "Not Modified");
    public static final HttpStatus _305_USE_PROXY = new HttpStatus(305, "Use Proxy");
    public static final HttpStatus _307_TEMPORARY_REDIRECT = new HttpStatus(307, "Temporary Redirect");
    public static final HttpStatus _308_PERMANENT_REDIRECT = new HttpStatus(308, "Permanent Redirect");
    public static final HttpStatus _400_BAD_REQUEST = new HttpStatus(400, "Bad Request");
    public static final HttpStatus _401_UNAUTHORIZED = new HttpStatus(401, "Unauthorized");
    public static final HttpStatus _403_FORBIDDEN = new HttpStatus(403, "Forbidden");
    public static final HttpStatus _404_NOT_FOUND = new HttpStatus(404, "Not Found");
    public static final HttpStatus _405_METHOD_NOT_ALLOWED = new HttpStatus(405, "Method Not Allowed");
    public static final HttpStatus _406_NOT_ACCEPTABLE = new HttpStatus(406, "Not Acceptable");
    public static final HttpStatus _407_PROXY_AUTHENTICATION_REQUIRED = new HttpStatus(407, "Proxy Authentication Required");
    public static final HttpStatus _408_REQUEST_TIMEOUT = new HttpStatus(408, "Request Timeout");
    public static final HttpStatus _409_CONFLICT = new HttpStatus(409, "Conflict");
    public static final HttpStatus _410_GONE = new HttpStatus(410, "Gone");
    public static final HttpStatus _411_LENGTH_REQUIRED = new HttpStatus(411, "Length Required");
    public static final HttpStatus _412_PRECONDITION_FAILED = new HttpStatus(412, "Precondition Failed");
    public static final HttpStatus _413_REQUEST_ENTITY_TOO_LARGE = new HttpStatus(413, "Request Entity Too Large");
    public static final HttpStatus _414_REQUESTURI_TOO_LONG = new HttpStatus(414, "Request-URI Too Long");
    public static final HttpStatus _415_UNSUPPORTED_MEDIA_TYPE = new HttpStatus(415, "Unsupported Media Type");
    public static final HttpStatus _416_REQUESTED_RANGE_NOT_SATISFIABLE = new HttpStatus(416, "Requested Range Not Satisfiable");
    public static final HttpStatus _417_EXPECTATION_FAILED = new HttpStatus(417, "Expectation Failed");
    public static final HttpStatus _500_INTERNAL_SERVER_ERROR = new HttpStatus(500, "Internal Server Error");
    public static final HttpStatus _501_NOT_IMPLEMENTED = new HttpStatus(501, "Not Implemented");
    public static final HttpStatus _502_BAD_GATEWAY = new HttpStatus(502, "Bad Gateway");
    public static final HttpStatus _503_SERVICE_UNAVAILABLE = new HttpStatus(503, "Service Unavailable");
    public static final HttpStatus _504_GATEWAY_TIMEOUT = new HttpStatus(504, "Gateway Timeout");
    public static final HttpStatus _505_HTTP_VERSION_NOT_SUPPORTED = new HttpStatus(505, "HTTP Version Not Supported");

    public enum StatusCodeClass {
        INFORMATIONAL,
        SUCCESS,
        REDIRECTION,
        CLIENT_ERROR,
        SERVER_ERROR;

        public static StatusCodeClass pick(int statusCodeValue) {
            switch (statusCodeValue / 100) {
            case 1:
                return INFORMATIONAL;
            case 2:
                return SUCCESS;
            case 3:
                return REDIRECTION;
            case 4:
                return CLIENT_ERROR;
            case 5:
                return SERVER_ERROR;
            default:
                throw new IllegalArgumentException("Unexpected status code value " + statusCodeValue);
            }
        }
    }

    public final int statusCode;
    public final String reasonPhrase;
    public final StatusCodeClass statusCodeClass;

    public HttpStatus(int statusCode, String reasonPhrase) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.statusCodeClass = StatusCodeClass.pick(statusCode);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HttpStatus && ((HttpStatus) obj).statusCode == statusCode;
    }

    @Override
    public int hashCode() {
        return statusCode;
    }

    @Override public int compareTo(HttpStatus o) {
        return this.statusCode < o.statusCode ? -1 : this.statusCode > o.statusCode ? 1 : 0;
    }

    public String getStatusCodeAsString() {
        return String.valueOf(statusCode);
    }

    public StatusCodeClass getStatusCodeClass() {
        return statusCodeClass;
    }

    public boolean isInformational() {
        return statusCodeClass == StatusCodeClass.INFORMATIONAL;
    }

    public boolean isSuccess() {
        return statusCodeClass == StatusCodeClass.SUCCESS;
    }

    public boolean isRedirection() {
        return statusCodeClass == StatusCodeClass.REDIRECTION;
    }

    public boolean isClientError() {
        return statusCodeClass == StatusCodeClass.CLIENT_ERROR;
    }

    public boolean isServerError() {
        return statusCodeClass == StatusCodeClass.SERVER_ERROR;
    }

    @Override
    public String toString() {
        return String.valueOf(statusCode) + ' ' + reasonPhrase;
    }

}
