package tk.bolovsrol.utils.xml.soap;

import tk.bolovsrol.utils.box.Box;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.syncro.QueuedKey;
import tk.bolovsrol.utils.syncro.UniQueuedSynchronizer;

import java.util.Map;
import java.util.TreeMap;

/**
 * Процессор для каждого элемента заголовка и тела вызывает
 * соответствующий зарегистрированный процессор.
 * <p/>
 * Может работать как синхронно, так и асинхронно.
 */
public class MetaSoapEnvelopeProcessor implements SoapEnvelopeProcessor {

    private final LogDome log;

    private final UniQueuedSynchronizer synchronizer;

    private final Map<String, HeaderSoapActionProcessor> headerProcessors = new TreeMap<String, HeaderSoapActionProcessor>();

    private HeaderSoapActionProcessor defaultHeaderProcessor = VoidHeaderSoapActionProcessor.getInstance();

    private final Map<String, BodySoapActionProcessor> bodyProcessors = new TreeMap<String, BodySoapActionProcessor>();

    private BodySoapActionProcessor defaultBodyProcessor = FaultBodySoapActionProcessor.getInstance();

    public MetaSoapEnvelopeProcessor(LogDome log) {
        this(log, true);
    }

    public MetaSoapEnvelopeProcessor(LogDome log, boolean async) {
        this.log = log;
        this.synchronizer = async ? null : new UniQueuedSynchronizer();
    }

    @Override
    public void process(SoapEnvelope requestEnvelope, SoapEnvelope responseEnvelope) throws SoapException, InterruptedException {
        try {
            QueuedKey key = synchronizer == null ? null : synchronizer.enter();
            try {
                for (SoapAction headerAction : requestEnvelope.headerActions()) {
                    Box.with(headerProcessors.get(headerAction.getName())).getOr(defaultHeaderProcessor).process(headerAction);
                }

                for (SoapAction requestAction : requestEnvelope.bodyActions()) {
                    SoapAction responseAction = SoapAction.newAndNsAttr(requestAction.getNsUri(), requestAction.getName() + "Response");
                    Box.with(bodyProcessors.get(requestAction.getName())).getOr(defaultBodyProcessor).process(requestAction, responseAction);
                    requestEnvelope.bodyActions().add(responseAction);
                }
            } finally {
                if (key != null) {
                    key.release();
                }
            }
        } catch (ActionFaultException e) {
            log.warning(e);
            responseEnvelope.bodyActions().clear();
            responseEnvelope.setSoapFault(e.getSoapFault());
        }
    }

    public void registerHeaderProcessor(String actionName, HeaderSoapActionProcessor processor) {
        headerProcessors.put(actionName, processor);
    }

    public void registerBodyProcessor(String actionName, BodySoapActionProcessor processor) {
        bodyProcessors.put(actionName, processor);
    }

    public HeaderSoapActionProcessor getDefaultHeaderProcessor() {
        return defaultHeaderProcessor;
    }

    public void setDefaultHeaderProcessor(HeaderSoapActionProcessor defaultHeaderProcessor) {
        this.defaultHeaderProcessor = defaultHeaderProcessor;
    }

    public BodySoapActionProcessor getDefaultBodyProcessor() {
        return defaultBodyProcessor;
    }

    public void setDefaultBodyProcessor(BodySoapActionProcessor defaultBodyProcessor) {
        this.defaultBodyProcessor = defaultBodyProcessor;
    }

}
