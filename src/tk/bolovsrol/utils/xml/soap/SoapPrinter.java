package tk.bolovsrol.utils.xml.soap;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.xml.Element;
import tk.bolovsrol.utils.xml.NamespaceUtils;

import java.util.List;
import java.util.Map;

public class SoapPrinter {

    private SoapPrinter() {
    }

    public static Element print(SoapEnvelope envelope) throws SoapException {
        String nsPrefix = envelope.getNsPrefix();
        Element envelopeEl = new Element(nsPrefix + SoapConst.ENVELOPE_NAME);
        envelopeEl.a().setAll(envelope.attributes());
        String namespaceUri = NamespaceUtils.getNamespaceUri(envelopeEl);
        if (!(StringUtils.equals(namespaceUri, SoapConst.NS_URI_SOAP) ||
                StringUtils.equals(namespaceUri, SoapConst.NS_URI_SOAP2))) {
            throw new SoapException("Namespace " + Spell.get(SoapConst.NS_URI_SOAP) +
                    " or " + Spell.get(SoapConst.NS_URI_SOAP2) + " not defined in the Envelope");
        }
        printEntries(nsPrefix, envelopeEl, SoapConst.HEADER_NAME, envelope.headerActions());
        printEntries(nsPrefix, envelopeEl, SoapConst.BODY_NAME, envelope.bodyActions());
        printFault(nsPrefix, envelopeEl, envelope.getSoapFault());
        return envelopeEl;
    }

    private static void printEntries(String soapNsPrefix, Element envelopeEl, String name, List<SoapAction> bodyActions) throws SoapException {
        if (!bodyActions.isEmpty()) {
            Element headerEl = envelopeEl.newChild(soapNsPrefix + name);
            for (SoapAction soapAction : bodyActions) {
                Element entryEl = headerEl.newChild(soapAction.getName());
                entryEl.a().setAll(soapAction.attributes());
                if (!StringUtils.equals(NamespaceUtils.getNamespaceUri(entryEl), soapAction.getNsUri())) {
                    throw new SoapException("Namespace " + Spell.get(soapAction.getNsUri()) +
                            " not defined in the Envelope");
                }
                for (Map.Entry<String, String> paramEntry : soapAction.params().dump().entrySet()) {
                    entryEl.newChild(paramEntry.getKey()).addTextData(paramEntry.getValue());
                }
            }
        }
    }

    /*
    */
    private static void printFault(String soapNsPrefix, Element envelopeEl, SoapFault fault) {
        if (fault != null) {
            Element faultEl = envelopeEl.newChild(soapNsPrefix + SoapConst.FAULT);
            faultEl.newChild("faultcode").addTextData(fault.getFaultCode().getCaption());
            faultEl.newChild("faultstring").addTextData(fault.getFaultString());
            if (fault.getFaultActor() != null) {
                faultEl.newChild("faultactor").addTextData(fault.getFaultActor());
            }
            if (fault.getFaultDetail() != null) {
                faultEl.newChild("faultdetail").addChild(fault.getFaultDetail().copy());
            }
        }
    }
}
