package tk.bolovsrol.utils.xml.soap;

public class FaultSoapEnvelopeProcessor implements SoapEnvelopeProcessor {
    public static final FaultSoapEnvelopeProcessor INSTANCE = new FaultSoapEnvelopeProcessor();

    @Override public void process(SoapEnvelope request, SoapEnvelope response) {
        response.setSoapFault(new SoapFault(FaultCode.CLIENT, request.bodyActions().get(0).getName()));
    }
}
