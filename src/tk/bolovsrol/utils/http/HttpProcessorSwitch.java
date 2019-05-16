package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.http.server.HttpEndpoint;
import tk.bolovsrol.utils.http.server.HttpServer;
import tk.bolovsrol.utils.socket.EndpointAlreadyBoundException;
import tk.bolovsrol.utils.socket.EndpointBindFailedException;
import tk.bolovsrol.utils.threads.Suspendable;

import java.util.ArrayList;
import java.util.List;

/**
 * Выключатель для регистрации, активизации и рагрегистрации нескольких хттп-процессоров одним махом.
 */
public class HttpProcessorSwitch implements Suspendable {

    private final List<Container> registered = new ArrayList<>();

    @Override public boolean isAlive() {
        for (Container c : registered) {
            if (c.processor.isActive()) {
                return true;
            }
        }
        return false;
    }

    @Override public void start() {
        for (Container container : registered) {
            container.processor.setActive(true);
        }
    }

    @Override public void shutdown() {
        for (Container container : registered) {
            HttpServer.server().unregisterProcessor(container.endpoint, container.processor);
        }
    }

    @Override public String getName() {
        return "HttpProcessorSwitch-" + Thread.currentThread().hashCode();
    }

    private final static class Container {
        public final HttpEndpoint endpoint;
        public final SwitchableHttpRequestProcessor processor;

        public Container(HttpEndpoint endpoint, SwitchableHttpRequestProcessor processor) {
            this.endpoint = endpoint;
            this.processor = processor;
        }
    }

    /**
     * Регистрирует процессор по указанному эндпоинту, оборачивая его в {@link SwitchableHttpRequestProcessor} в неактивном состоянии.
     *
     * @param endpoint
     * @param processor
     * @throws EndpointBindFailedException
     * @throws AmbigousRequestProcessorMappingException
     * @throws EndpointAlreadyBoundException
     * @see #on()
     */
    public void addAndRegister(HttpEndpoint endpoint, HttpRequestProcessor processor) throws EndpointBindFailedException, AmbigousRequestProcessorMappingException, EndpointAlreadyBoundException {
        SwitchableHttpRequestProcessor sw = new SwitchableHttpRequestProcessor(processor);
        HttpServer.server().registerProcessor(endpoint, sw);
        registered.add(new Container(endpoint, sw));
    }

}
