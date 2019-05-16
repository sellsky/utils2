package tk.bolovsrol.utils.xml.soap;

/** Несколько мыльных констант. */
public class SoapConst {

    public static final String NS_URI_SOAP = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String NS_URI_SOAP2 = "http://www.w3.org/2003/05/soap-envelope";

    public static final String ENVELOPE_NAME = "Envelope";

    public static final String HEADER_NAME = "Header";

    public static final String BODY_NAME = "Body";

    public static final String SOAP_ACTION = "SOAPAction";
    public static final String FAULT = "Fault";

    private SoapConst() {
    }
}
