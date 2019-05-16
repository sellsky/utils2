package tk.bolovsrol.utils.xml.soap;

/**
 * Выкидывает Failure всегда.
 * Состояний не имеет, поэтому одна инстанция на всю систему.
 */
public class FaultSoapEntryProcessor implements SoapEntryProcessor {
    public static final FaultSoapEntryProcessor INSTANCE = new FaultSoapEntryProcessor();

    private FaultSoapEntryProcessor() {
    }

    public static FaultSoapEntryProcessor getInstance() {
        return INSTANCE;
    }

    @Override
    public void process(SoapAction bodyAction, SoapAction responseAction) throws ActionFaultException, InterruptedException {
        throw new ActionFaultException("Unexpected action " + bodyAction.getName(),
                new SoapFault(FaultCode.CLIENT, bodyAction.getName()));
    }
}
