package tk.bolovsrol.utils.xml.soap;

public class ActionFaultException extends Exception {
    private final SoapFault soapFault;

    public ActionFaultException(String message, SoapFault soapFault) {
        super(message);
        this.soapFault = soapFault;
    }

    public ActionFaultException(String message, Throwable cause, SoapFault soapFault) {
        super(message, cause);
        this.soapFault = soapFault;
    }

    public ActionFaultException(Throwable cause, SoapFault soapFault) {
        super(cause);
        this.soapFault = soapFault;
    }

    public SoapFault getSoapFault() {
        return soapFault;
    }
}
