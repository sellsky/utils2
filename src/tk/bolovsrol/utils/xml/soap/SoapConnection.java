package tk.bolovsrol.utils.xml.soap;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.Uri;
import tk.bolovsrol.utils.http.HttpConst;
import tk.bolovsrol.utils.http.HttpEntityParsingException;
import tk.bolovsrol.utils.http.HttpRequest;
import tk.bolovsrol.utils.http.HttpResponse;
import tk.bolovsrol.utils.http.HttpVersion;
import tk.bolovsrol.utils.http.InvalidHttpEntityException;
import tk.bolovsrol.utils.http.Method;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.xml.ElementParsingException;
import tk.bolovsrol.utils.xml.XmlParser;
import tk.bolovsrol.utils.xml.XmlPrinter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SoapConnection {

    public static final int DEFAULT_TIMEOUT = 60000;

    private final LogDome log;
    private final Uri url;
    private final String hostname;
    private int port;
    private final int timeout;

    private final XmlPrinter xmlPrinter = new XmlPrinter();
    private final XmlParser xmlParser = new XmlParser();

    private Socket socket;

    public SoapConnection(LogDome log, Uri url) {
        this(log, url, DEFAULT_TIMEOUT);
    }

    public SoapConnection(LogDome log, Uri url, int timeout) {
        this.log = log;
        this.url = url;
        this.hostname = url.getHostname();
        this.port = url.getPortIntValue(HttpConst.DEFAULT_PORT);
        this.timeout = timeout;
    }

//    private static String getContentType(SoapEnvelope envelope) {
//        envelope.getNsAttrName()
//    }

    public SoapEnvelope send(SoapEnvelope requestEnvelope) throws IOException, InterruptedException, SoapConnectionException {
        log.trace("Sending Envelope " + Spell.get(requestEnvelope));
        HttpRequest httpRequest = new HttpRequest(HttpVersion.HTTP_1_1, Method.POST);
        httpRequest.setUrl(url);


        try {
            httpRequest.setBody(xmlPrinter.toBytes(SoapPrinter.print(requestEnvelope)), "application/soap+xml; charset=utf-8");
        } catch (SoapException e) {
            throw new SoapConnectionException("Invalid Soap Envelope", e);
        }
        log.trace("Sending HTTP Request " + Spell.get(httpRequest));

        open();
        try {
            httpRequest.writeToStream(socket.getOutputStream(), timeout);
        } catch (InvalidHttpEntityException e) {
            throw new SoapConnectionException("Created invalid Http Request from Soap Envelope", e);
        }

        HttpResponse httpResponse;
        try {
            httpResponse = HttpResponse.parse(socket.getInputStream(), timeout);
        } catch (HttpEntityParsingException e) {
            throw new SoapConnectionException("Invalid HTTP Response received", e);
        }
        log.trace("Received HTTP Response " + Spell.get(httpResponse));

        if (!httpResponse.getStatus().isSuccess()) {
            throw new SoapConnectionException("HTTP Response has non-success status code "
                    + Spell.get(httpResponse.getStatus()));
        }

        SoapEnvelope responseEnvelope;
        try {
            responseEnvelope = SoapParser.parse(xmlParser.parse(httpResponse.getBody()));
        } catch (ElementParsingException e) {
            throw new SoapConnectionException("Invalid XML document received", e);
        } catch (SoapException e) {
            throw new SoapConnectionException("Invalid SOAP envelope received", e);
        }
        log.trace("Parsed Envelope " + Spell.get(responseEnvelope));

        return responseEnvelope;
    }

    @SuppressWarnings({"SocketOpenedButNotSafelyClosed"})
    public void open() throws IOException {
        if (socket == null) {
            log.trace("Opening connection to " + hostname + ':' + port + ", timeout " + timeout);
            socket = new Socket();
            socket.setSoTimeout(timeout);
            socket.connect(new InetSocketAddress(hostname, port), timeout);
        }
    }

    public void close() throws IOException {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }
}
