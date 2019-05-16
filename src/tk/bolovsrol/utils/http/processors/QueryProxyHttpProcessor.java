package tk.bolovsrol.utils.http.processors;

import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.Uri;
import tk.bolovsrol.utils.UriParsingException;
import tk.bolovsrol.utils.http.HttpRequest;
import tk.bolovsrol.utils.http.HttpRequestProcessor;
import tk.bolovsrol.utils.http.HttpResponse;
import tk.bolovsrol.utils.http.HttpStatus;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.properties.PropertiesProcessor;
import tk.bolovsrol.utils.properties.ReadOnlyProperties;
import tk.bolovsrol.utils.properties.sources.EmptyReadOnlySource;

import java.net.Socket;

/**
 * Делегирует обработку GET-запроса указанному {@link PropertiesProcessor процессору},
 * возвращая результат в виде application/x-www-form-urlencoded.
 * <p/>
 * В случае ошибки парсинга строки запроса возвращает {@link tk.bolovsrol.utils.http.HttpStatus#_400_BAD_REQUEST},
 * в случае выкинутого процессором исключения — {@link tk.bolovsrol.utils.http.HttpStatus#_500_INTERNAL_SERVER_ERROR},
 * в остальных случаях возвращает {@link tk.bolovsrol.utils.http.HttpStatus#_200_OK} с пропертями в теле ответа.
 * <p/>
 * Если процессор вернул нул, то возвращается просто {@link tk.bolovsrol.utils.http.HttpStatus#_200_OK}.
 * <p/>
 * Пишет в лог варнинги о пойманных  исключениях.
 */
public class QueryProxyHttpProcessor implements HttpRequestProcessor {
    private final LogDome log;
    private final PropertiesProcessor propertiesProcessor;

    public QueryProxyHttpProcessor(LogDome log, PropertiesProcessor propertiesProcessor) {
        this.log = log;
        this.propertiesProcessor = propertiesProcessor;
    }

    @Override public HttpResponse process(Socket socket, HttpRequest httpRequest) throws InterruptedException {
//        log.hint("Processing request " + Spell.get(httpRequest));
        ReadOnlyProperties rop;
        try {
            rop = httpRequest.getQuery() == null ?
                  EmptyReadOnlySource.EMPTY_PROPERTIES :
                  new ReadOnlyProperties(Uri.parseQuery(httpRequest.getQuery()));
        } catch (UriParsingException e) {
            log.warning(e);
            return HttpResponse.generate(httpRequest, HttpStatus._400_BAD_REQUEST);
        }

        try {
            ReadOnlyProperties result = propertiesProcessor.processProperies(rop);
            HttpResponse resp = HttpResponse.generate(httpRequest, HttpStatus._200_OK);
            if (result != null) {
                resp.setBody(Uri.compileQuery(result.dump()), "application/x-www-form-urlencoded");
            }
//            log.hint("Sending response " + Spell.get(resp));
            return resp;
        } catch (UnexpectedBehaviourException e) {
            log.warning(e);
            return HttpResponse.generate(httpRequest, HttpStatus._500_INTERNAL_SERVER_ERROR);
        }
    }
}
