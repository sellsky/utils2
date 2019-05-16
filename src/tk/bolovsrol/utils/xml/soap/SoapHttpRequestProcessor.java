package tk.bolovsrol.utils.xml.soap;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.http.HttpRequest;
import tk.bolovsrol.utils.http.HttpRequestProcessor;
import tk.bolovsrol.utils.http.HttpResponse;
import tk.bolovsrol.utils.http.HttpStatus;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.xml.ElementParsingException;
import tk.bolovsrol.utils.xml.XmlParser;
import tk.bolovsrol.utils.xml.XmlPrinter;

import java.io.UnsupportedEncodingException;
import java.net.Socket;

/**
 * Парсит хттп-запрос и вызывает процессор мыльных конвертов
 * для обработки и наполнения ответа.
 */
public class SoapHttpRequestProcessor implements HttpRequestProcessor {
    private final LogDome log;
    private final SoapEnvelopeProcessor envelopeProcessor;

    private final XmlParser xmlParser = new XmlParser();

    {
        xmlParser.setTrimWhitespaces(true);
    }

    public SoapHttpRequestProcessor(LogDome log, SoapEnvelopeProcessor envelopeProcessor) {
        this.log = log;
        this.envelopeProcessor = envelopeProcessor;
    }

    @Override public HttpResponse process(Socket socket, HttpRequest httpRequest) {
        try {
            SoapEnvelope requestEnvelope = SoapParser.parse(xmlParser.parse(httpRequest));
            log.hint("Parsed Envelope " + Spell.get(requestEnvelope));

            SoapEnvelope responseEnvelope = SoapEnvelope.newAndNsAttr(
                    requestEnvelope.getNsPrefix(),
                    requestEnvelope.attributes().getOrDie(requestEnvelope.getNsAttrName())
            );
            envelopeProcessor.process(requestEnvelope, responseEnvelope);

            log.hint("Response Envelope " + Spell.get(responseEnvelope));
            HttpResponse httpResponse = httpRequest.createResponse(HttpStatus._200_OK);
            httpResponse.setBody(
                    new XmlPrinter().toBytes(SoapPrinter.print(responseEnvelope)),
                    httpRequest.getContentTypeMime() + "; charset=utf-8"
            );
            return httpResponse;
        } catch (UnexpectedBehaviourException e) {
            log.warning(e);
            return httpRequest.createResponse(HttpStatus._500_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (ElementParsingException e) {
            log.warning(e);
            return httpRequest.createResponse(HttpStatus._400_BAD_REQUEST, e.getMessage());
        } catch (UnsupportedEncodingException e) {
            log.warning(e);
            return httpRequest.createResponse(HttpStatus._400_BAD_REQUEST, e.getMessage());
        } catch (InterruptedException e) {
            log.warning(e);
            Thread.currentThread().interrupt();
            return httpRequest.createResponse(HttpStatus._503_SERVICE_UNAVAILABLE, e.getMessage());
        }
    }
}
