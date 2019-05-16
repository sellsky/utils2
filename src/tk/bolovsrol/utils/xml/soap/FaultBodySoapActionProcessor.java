package tk.bolovsrol.utils.xml.soap;

/** Безусловно выкидывает фаулт. */
public class FaultBodySoapActionProcessor implements BodySoapActionProcessor {
    public static final FaultBodySoapActionProcessor INSTANCE = new FaultBodySoapActionProcessor();

    public static FaultBodySoapActionProcessor getInstance() {
        return INSTANCE;
    }

    private FaultBodySoapActionProcessor() {
    }

    @Override
    public void process(SoapAction bodyAction, SoapAction responseAction) throws ActionFaultException, InterruptedException {
        throw new ActionFaultException("Unexpected action " + bodyAction.getName(),
                new SoapFault(FaultCode.CLIENT, bodyAction.getName()));

    }
}
