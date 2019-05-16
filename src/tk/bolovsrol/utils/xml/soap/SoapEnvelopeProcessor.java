package tk.bolovsrol.utils.xml.soap;

public interface SoapEnvelopeProcessor {

    void process(SoapEnvelope request, SoapEnvelope response) throws SoapException, InterruptedException;

}
