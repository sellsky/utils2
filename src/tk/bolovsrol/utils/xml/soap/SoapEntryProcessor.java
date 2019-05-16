package tk.bolovsrol.utils.xml.soap;

public interface SoapEntryProcessor {

    void process(SoapAction bodyAction, SoapAction responseAction) throws ActionFaultException, InterruptedException;
}
