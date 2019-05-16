package tk.bolovsrol.utils.xml.soap;

public interface BodySoapActionProcessor {

    void process(SoapAction bodyAction, SoapAction responseAction) throws ActionFaultException, InterruptedException;
}
