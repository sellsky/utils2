package tk.bolovsrol.utils.xml.soap;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.Uri;
import tk.bolovsrol.utils.http.HttpConst;
import tk.bolovsrol.utils.http.HttpEntityParsingException;
import tk.bolovsrol.utils.http.HttpRequest;
import tk.bolovsrol.utils.http.HttpResponse;
import tk.bolovsrol.utils.http.HttpStatus;
import tk.bolovsrol.utils.http.HttpVersion;
import tk.bolovsrol.utils.http.InvalidHttpEntityException;
import tk.bolovsrol.utils.http.Method;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.xml.Element;
import tk.bolovsrol.utils.xml.XmlParser;
import tk.bolovsrol.utils.xml.XmlPrinter;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;

public class SoapSender {

    private final LogDome log;
    private final Uri url;
    private boolean useSsl;

    private final XmlPrinter printer = new XmlPrinter();

    public SoapSender(LogDome log, Uri url) {
        this(log, url, false);
    }

    public SoapSender(LogDome log, Uri url, boolean useSsl) {
        this.log = log;
        this.url = url;
        this.useSsl = useSsl;
    }

    public SoapEnvelope rendRequest(SoapEnvelope envelope, String soapAction) throws SoapException, InvalidHttpEntityException, IOException, InterruptedException {
        return SoapParser.parse(sendRequest(SoapPrinter.print(envelope), soapAction));
    }

    /**
     * Отправляет soap-запрос и дожидается ответа.
     *
     * @param requestEl
     * @return полученный ответ
     */
    public Element sendRequest(Element requestEl, String soapAction) throws IOException, InvalidHttpEntityException, InterruptedException, SoapException {
        EnvelopeHelper envelopeHelper = new EnvelopeHelper("soap");
        Element envelopeEl = envelopeHelper.createEnvelope(requestEl);

        log.info("Sending SOAP request " + Spell.get(printer.toXmlString(envelopeEl)) + "...");

        HttpRequest httpRequest = new HttpRequest(HttpVersion.HTTP_1_1, Method.POST);
        httpRequest.setUrl(url);
        httpRequest.setBody(envelopeEl);
        httpRequest.headers().set("SOAPAction", soapAction);

        HttpResponse response;
        Socket socket;
        if (useSsl) {
            socket = SSLSocketFactory.getDefault().createSocket(url.getHostname(), url.getPortIntValue(HttpConst.DEFAULT_PORT));
        } else {
            socket = new Socket(url.getHostname(), url.getPortIntValue(HttpConst.DEFAULT_PORT));
        }
        try {
            log.info("Sending request " + Spell.get(httpRequest) + "...");
            httpRequest.writeToStream(socket.getOutputStream());
            response = HttpResponse.parse(socket.getInputStream());
        } catch (IOException e) {
            throw new SoapException("Error Sending Request.", e);
        } catch (HttpEntityParsingException e) {
            throw new SoapException("Error reading Response", e);
        } finally {
            socket.close();
        }
        log.info("Response obtained " + Spell.get(response));

        if (response.getStatus().equals(HttpStatus._200_OK)) {
            try {
//                new XmlParser().setTrimWhitespaces(true);
                Element responseEl = new XmlParser().parse(new String(response.getBody(), "UTF-8"));
                log.info("Parsed payload element " + Spell.get(printer.toXmlString(responseEl)));
                return responseEl;
            } catch (Exception e) {
                throw new SoapException("Invalid response body received " + Spell.get(response.getBody()) + ". " + Spell.get(e));
            }
        } else {
            throw new SoapException("Invalid response status code received " + Spell.get(response));
        }
    }
}
