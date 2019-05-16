package tk.bolovsrol.utils.xml.soap;

/** Ничего не делает. */
public class VoidHeaderSoapActionProcessor implements HeaderSoapActionProcessor {
    public static final VoidHeaderSoapActionProcessor INSTANCE = new VoidHeaderSoapActionProcessor();

    public static VoidHeaderSoapActionProcessor getInstance() {
        return INSTANCE;
    }

    public VoidHeaderSoapActionProcessor() {
    }

    @Override public void process(SoapAction bodyAction) throws ActionFaultException, InterruptedException {
    }
}
