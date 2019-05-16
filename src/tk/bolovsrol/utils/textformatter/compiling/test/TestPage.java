package tk.bolovsrol.utils.textformatter.compiling.test;

import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.Uri;
import tk.bolovsrol.utils.http.HttpConst;
import tk.bolovsrol.utils.http.HttpRequest;
import tk.bolovsrol.utils.http.HttpRequestProcessor;
import tk.bolovsrol.utils.http.HttpResponse;
import tk.bolovsrol.utils.http.HttpStatus;
import tk.bolovsrol.utils.http.Method;
import tk.bolovsrol.utils.http.server.HttpServer;
import tk.bolovsrol.utils.textformatter.compiling.CompiledFormatter;
import tk.bolovsrol.utils.textformatter.compiling.TextFormatCompiler;
import tk.bolovsrol.utils.textformatter.compiling.evaluators.MapEvaluator;

import java.net.Socket;
import java.util.Map;

public class TestPage implements HttpRequestProcessor {
    @Override public HttpResponse process(Socket socket, HttpRequest httpRequest) {
        try {
            Map<String, String> params = Uri.parseQuery(httpRequest.getQuery());
            String tpl = params.get("tpl");
            if (tpl == null) {
                throw new UnexpectedBehaviourException("Template parameter missing.");
            }
            params.remove("tpl");
            CompiledFormatter formatter = new TextFormatCompiler().compile(tpl);
            String result = formatter.format(new MapEvaluator(params));

            return httpRequest.createResponse(HttpStatus._200_OK, result);

        } catch (Exception e) {
            return httpRequest.createResponse(HttpStatus._400_BAD_REQUEST, e.getMessage());
        }
    }

    public static void main(String[] args) throws UnexpectedBehaviourException, InterruptedException {
        int port = args.length == 0 ? HttpConst.DEFAULT_PORT : Integer.parseInt(args[0]);
        HttpServer.server().registerProcessor(
                null, port, Method.GET, "/", false, new TestPage()
        );
        while (true) {
            Thread.sleep(Long.MAX_VALUE);
        }
    }
}
