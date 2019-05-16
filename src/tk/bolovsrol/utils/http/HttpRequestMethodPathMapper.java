package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.RegexUtils;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.log.LogDome;

import java.net.Socket;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Процессор-фильтр, маршрутизирует запросы по методам и путям.
 * <p/>
 * Этот процессор надо зарегистрировать в {@link tk.bolovsrol.utils.http.server.HttpServer},
 * а полезные процессоре надо регистрировать уже в этом процессоре,
 * указывая путь, по которому они будут вызваны.
 * <p/>
 * В правилах маппинга начальные и финальные слэши в пути игнорируем;
 * так, пути «/foo/bar/» и «foo/bar» считаем одинаковыми.
 * <p/>
 * Можно определить процессор, которому будут переданы все незаматченные запросы;
 * если такого процессора нет, то будет возвращён ответ
 * {@link HttpStatus#_400_BAD_REQUEST} без тела, а в логе будет варнинг.
 */
public class HttpRequestMethodPathMapper implements HttpRequestProcessor {

    private final LogDome log;
    private final Object methodProcessorsLock = new Object();
    private final Map<Method, List<MappingContainer>> methodProcessors = new EnumMap<>(Method.class);
    private HttpRequestProcessor orphanProcessor;
    @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
    // чтение в isEmpty() такое, но там синхронизация снаружи
    private int mappingsCount = 0;

    public static class MappingContainer {
        public final String path;
        public final Pattern pathPattern;
        public final HttpAuthorization authorization;
        public final HttpRequestProcessor processor;

        public MappingContainer(String path, Pattern pathPattern, HttpAuthorization authorization, HttpRequestProcessor processor) {
            this.path = path;
            this.pathPattern = pathPattern;
            this.authorization = authorization;
            this.processor = processor;
        }

        public MappingContainer(String path, Pattern pathPattern, HttpRequestProcessor processor) {
            this(path, pathPattern, null, processor);
        }

		@Override public String toString() {
			return new StringDumpBuilder().append("Mapping")
					.append("path"         , path         )
					.append("pattern"      , pathPattern  )
					.append("authorization", authorization)
					.append("processor"    , processor    ).toString();
		}
    }

    /**
     * Создаёт маппер с указанным логом и без процессора для незаматченных запросов.
     *
     * @param log
     */
    public HttpRequestMethodPathMapper(LogDome log) {
        this.log = log;
    }

    /**
     * Создаёт маппер с указанными логом и процессором для незаматченных запросов.
     *
     * @param log
     * @param orphanProcessor
     */
    public HttpRequestMethodPathMapper(LogDome log, HttpRequestProcessor orphanProcessor) {
        this.log = log;
        this.orphanProcessor = orphanProcessor;
    }

    /**
     * Ищет процессор, который назначен на обработку указанного в запросе пути
     * и делегирует обработку запроса ему.
     * <p/>
     * Если запрос не найден, то обработка либо делегируется процессору
     * для незаматченных запросов, либо, если такого процессора нет,
     * возвращается ответ {@link HttpStatus#_400_BAD_REQUEST} без тела, а в логе пишется варнинг.
     *
     * @param socket
     * @param httpRequest
     * @return
     * @throws UnexpectedBehaviourException
     */
    @Override
    public HttpResponse process(Socket socket, HttpRequest httpRequest) throws InterruptedException {
        HttpRequestProcessor processor;
        try {
            processor = retrieveProcessor(httpRequest);
        } catch (AmbigousRequestProcessorMappingException e) {
            log.warning(e);
            return httpRequest.createResponse(HttpStatus._500_INTERNAL_SERVER_ERROR);
        } catch (UnknownLoginException e) {
            log.warning(e);
            return createAuthorizationResponse(httpRequest, HttpStatus._401_UNAUTHORIZED, e.getWwwAuthenticate());
        } catch (InvalidPasswordException e) {
            log.warning(e);
            return createAuthorizationResponse(httpRequest, HttpStatus._403_FORBIDDEN, null);
        } catch (AuthorizationMissingException e) {
            log.info(e);
            return createAuthorizationResponse(httpRequest, HttpStatus._401_UNAUTHORIZED, e.getWwwAuthenticate());
        }

        if (processor != null) {
            return processor.process(socket, httpRequest);
        } else if (orphanProcessor != null) {
            log.info("No specific processor is found for request, using orphan processor");
            return orphanProcessor.process(socket, httpRequest);
        } else {
            log.hint("No processor is found for request, sending Not Found response; request " + Spell.get(httpRequest));
            return httpRequest.createResponse(HttpStatus._404_NOT_FOUND);
        }
    }

    private static HttpResponse createAuthorizationResponse(HttpRequest httpRequest, HttpStatus statusCode, String wwwAuthenticate) {
        HttpResponse httpResponse = httpRequest.createResponse(statusCode);
        httpResponse.headers().set("WWW-Authenticate", wwwAuthenticate);
        return httpResponse;
    }

    private HttpRequestProcessor retrieveProcessor(HttpRequest httpRequest) throws UnknownLoginException, AmbigousRequestProcessorMappingException, InvalidPasswordException, AuthorizationMissingException {
        boolean unknownLoginHappened = false;
        String wwwAuthenticate = null;
        String path = httpRequest.getPath();
        log.trace("Looking for processor bound to path " + Spell.get(path) + "...");
        List<MappingContainer> containers;
        synchronized (methodProcessorsLock) {
            containers = methodProcessors.get(httpRequest.getMethod());
            if (containers == null) {
                return null;
            }
            containers = new ArrayList<>(containers); // avoiding further sync
        }
        MappingContainer candidate = null;
        for (MappingContainer container : containers) {
        //	log.trace("#TRACE# Check mapping: " + container);
            if (RegexUtils.matches(container.pathPattern, path)) {
                try {
                    if (container.authorization != null) {
                        container.authorization.checkAuthorization(httpRequest);
                    }
                } catch (UnknownLoginException e) {
                    // это не наша собака, это наркоманская
                    unknownLoginHappened = true;
                    wwwAuthenticate = e.getWwwAuthenticate();
                    continue;
                }
                if (candidate != null) {
                    throw new AmbigousRequestProcessorMappingException( "Ambiguous processor mapping. Conflict paths: "
                          + Spell.get(candidate.path) + " and " + Spell.get(container.path) + ". Request: "
                          + Spell.get(httpRequest) );
                }
                candidate = container;
            }
        }

        if (candidate != null) {
            return candidate.processor;
        } else if (unknownLoginHappened) {
            throw new UnknownLoginException(wwwAuthenticate);
        } else {
            return null;
        }
    }

    /**
     * Добавляет мапинг: запросы по указанному пути будет обрабатывать
     * указанный процессор.
     * <p/>
     * В правилах маппинга начальные и финальные слэши в пути игнорируем;
     * так, пути «/foo/bar/» и «foo/bar» считаем одинаковыми, можно указывать
     * как угодно.
     *
     * @param path
     * @param processor
     * @throws IllegalArgumentException пути уже назначен маппинг
     */
    public void addMapping(Method method, String path, HttpRequestProcessor processor) throws AmbigousRequestProcessorMappingException {
        addMapping(method, path, null, processor);
    }

    /**
     * Добавляет мапинг: запросы по указанному пути, прошедшие авторизацию,
     * будет обрабатывать указанный процессор.
     * <p/>
     * В правилах маппинга начальные и финальные слэши в пути игнорируем;
     * так, пути «/foo/bar/» и «foo/bar» считаем одинаковыми, можно указывать
     * как угодно.
     *
     * @param path
     * @param processor
     * @throws IllegalArgumentException пути уже назначен маппинг
     */
    public void addMapping(Method method, String path, HttpAuthorization authorization, HttpRequestProcessor processor) throws AmbigousRequestProcessorMappingException {
        Pattern pathPattern = RegexUtils.compileDosWildcard(path);
        synchronized (methodProcessorsLock) {
            List<MappingContainer> containers = methodProcessors.get(method);
            if (containers == null) {
                containers = new ArrayList<>();
                methodProcessors.put(method, containers);
            } else {
                for (MappingContainer existingContainer : containers) {
                    if (path.equals(existingContainer.path)) {
                        checkAuthorizationClassesOnSamePath(authorization, existingContainer.authorization);
                    }
                }
            }
            MappingContainer item = new MappingContainer(path, pathPattern, authorization, processor);
            containers.add(item);
            mappingsCount++;
        //	log.trace("#TRACE# Add mapping: " + item.toString());
        }
    }

    private static void checkAuthorizationClassesOnSamePath(HttpAuthorization auth1, HttpAuthorization auth2) throws AmbigousRequestProcessorMappingException {
        if (auth1 == null) {
            if (auth2 == null) {
                throw new AmbigousRequestProcessorMappingException("Only one non-authorized processor on the path allowed");
            } else {
                throw new AmbigousRequestProcessorMappingException("Mixing of authorized and non-authorized processors prohibited.");
            }
        } else if (auth2 == null) {
            throw new AmbigousRequestProcessorMappingException("Mixing of authorized and non-authorized processors prohibited.");
        } else if (!auth1.getWwwAuthenticate().equals(auth2.getWwwAuthenticate())) {
            throw new AmbigousRequestProcessorMappingException("Mixing of different authorization schemes on single path point prohibited.");
        }
    }

    /**
     * Удаляет маппинг и возвращает процессор, который был привязан к маппингу.
     * Если маппинг не был определён, вернётся null.
     *
     * @param path
     * @return
     */
    public boolean removeMapping(Method method, String path, HttpRequestProcessor processor) {
        List<MappingContainer> containers;
        synchronized (methodProcessorsLock) {
            containers = methodProcessors.get(method);
            if (containers == null) {
                return false;
            }
            Iterator<MappingContainer> it = containers.iterator();
            while (it.hasNext()) {
                MappingContainer container = it.next();
                if (container.path.equals(path) && container.processor == processor) {
                    it.remove();
                    mappingsCount--;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Проверяет, что определён хотя бы один маппинг.
     *
     * @return true, если определён хотя бы один маппинг, иначе false
     */
    public boolean isEmpty() {
        return mappingsCount == 0;
    }

    /**
     * @return процессор, которому делегируется обработка незаматченных запросов.
     */
    public HttpRequestProcessor getOrphanProcessor() {
        return orphanProcessor;
    }

    /**
     * Назначает процессор, которому делегируется обработка незаматченных запросов.
     *
     * @param orphanProcessor
     */
    public void setOrphanProcessor(HttpRequestProcessor orphanProcessor) {
        this.orphanProcessor = orphanProcessor;
    }
}
