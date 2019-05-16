package tk.bolovsrol.utils.xml.soap;

public interface HeaderSoapActionProcessor {

    void process(SoapAction bodyAction) throws ActionFaultException, InterruptedException;
}
