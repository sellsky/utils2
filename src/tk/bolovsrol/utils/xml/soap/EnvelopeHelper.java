package tk.bolovsrol.utils.xml.soap;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.xml.Element;
import tk.bolovsrol.utils.xml.TextData;

public class EnvelopeHelper {

    private final String SOAP_ENVELOPE_URI = "http://schemas.xmlsoap.org/soap/envelope/";
    private final String namespace;

    public EnvelopeHelper(String namespace) {
        this.namespace = namespace;
    }

    public Element getFault(FaultCode faultCode, String faultString, String faultActorOrNull, Element faultDetailOrNull) {
        Element faultEl = new Element(namespace + ":Fault");
        faultEl.setAttribute("xmlns:" + namespace, SOAP_ENVELOPE_URI);
        faultEl.newChild("faultcode").addTextData(faultCode.getCaption(), TextData.Type.TEXT);
        faultEl.newChild("faultstring").addTextData(faultString, TextData.Type.TEXT);
        if (faultActorOrNull != null) {
            faultEl.newChild("faultactor").addTextData(faultActorOrNull, TextData.Type.TEXT);
        }
        if (faultDetailOrNull != null) {
            faultEl.newChild("faultdetail").addChild(faultDetailOrNull.copy());
        }
        return faultEl;
    }

    public Element createEnvelope(Element... payloadEls) {
        Element envelopeEl = new Element(namespace + ":Envelope")
                .setAttribute("xmlns:" + namespace, SOAP_ENVELOPE_URI);
        Element bodyEl = envelopeEl.newChild(namespace + ":Body");
        for (Element requestEl : payloadEls) {
            bodyEl.addChild(requestEl);
        }
        return envelopeEl;
    }

    public Element createEnvelope(Element payloadEl) {
        Element envelopeEl = new Element(namespace + ":Envelope")
                .setAttribute("xmlns:" + namespace, SOAP_ENVELOPE_URI);
        Element bodyEl = envelopeEl.newChild(namespace + ":Body");
        bodyEl.addChild(payloadEl);
        return envelopeEl;
    }

    public Element extractFirstPayload(Element envelopeEl) throws UnexpectedBehaviourException {
        return extractBodyEl(envelopeEl).getFirstChild();
    }

    public Element[] extractPayload(Element envelopeEl) throws UnexpectedBehaviourException {
        return extractBodyEl(envelopeEl).getChildren();
    }

    public Element extractBodyEl(Element envelopeEl) throws UnexpectedBehaviourException {
//        String soap = NamespaceUtils.getNamespacePrefixOrEmptyString(envelopeEl, SOAP_ENVELOPE_URI);
        String name = envelopeEl.getName();
        int colonPos = name.indexOf(':');
        String soap = colonPos >= 0 ? name.substring(0, colonPos) + ':' : "";
        if (!envelopeEl.getName().equals(soap + "Envelope")) {
            throw new UnexpectedBehaviourException("Envelope xml has invalid root tag " + Spell.get(envelopeEl.getName()));
        }
        Element bodyEl = envelopeEl.getFirstChild(soap + "Body");
        if (bodyEl == null) {
            throw new UnexpectedBehaviourException("Envelope xml has no Body element");
        }
        return bodyEl;
    }
}