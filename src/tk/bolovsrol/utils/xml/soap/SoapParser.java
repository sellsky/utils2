package tk.bolovsrol.utils.xml.soap;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.caseinsensitive.CaseInsensitiveLinkedHashMap;
import tk.bolovsrol.utils.properties.PlainProperties;
import tk.bolovsrol.utils.xml.Element;
import tk.bolovsrol.utils.xml.NamespaceUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SoapParser {

    private SoapParser() {
    }

    public static SoapEnvelope parse(Element envelopeEl) throws SoapException {
        String namespace = NamespaceUtils.getNamespace(envelopeEl, SoapConst.NS_URI_SOAP);
        if (namespace == null) {
            namespace = NamespaceUtils.getNamespace(envelopeEl, SoapConst.NS_URI_SOAP2);
            if (namespace == null) {
                throw new SoapException("Envelope has no SOAP namespace defined.");
            }
        }
        String xmlnsPrefix = NamespaceUtils.getNamespacePrefix(namespace);
        if (!(xmlnsPrefix + SoapConst.ENVELOPE_NAME).equals(envelopeEl.getName())) {
            throw new SoapException("Envelope has unexpected name " + Spell.get(envelopeEl.getName()));
        }
        List<SoapAction> headerActions = readEntries(envelopeEl, xmlnsPrefix + SoapConst.HEADER_NAME);
        List<SoapAction> bodyActions = readEntries(envelopeEl, xmlnsPrefix + SoapConst.BODY_NAME);

        return new SoapEnvelope(xmlnsPrefix, envelopeEl.a(), headerActions, bodyActions);
    }

    private static List<SoapAction> readEntries(Element envelopeEl, String name) throws SoapException {
        int count = envelopeEl.getChildrenCount(name);
        if (count == 0) {
            return new ArrayList<SoapAction>();
        } else if (count == 1) {
            List<SoapAction> result = new ArrayList<SoapAction>();
            for (Element el : envelopeEl.getFirstChild(name).getChildren()) {
                if (el.getName() != null) {
                    result.add(parseElement(el));
                }
            }
            return result;
        } else {
            throw new SoapException("Too many " + name + " elements in the Envelope");
        }
    }

    private static SoapAction parseElement(Element entryEl) {
        CaseInsensitiveLinkedHashMap<String> attributesMap = entryEl.getAttributesMap();
        LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
        for (Element paramEl : entryEl.getChildren()) {
            String key = paramEl.getName();
            if (key != null) {
                paramMap.put(key, paramEl.getFirstTextData());
            }
        }
        return new SoapAction(
                NamespaceUtils.getNamespaceUri(entryEl),
                entryEl.getName(),
                new PlainProperties(attributesMap),
                new PlainProperties(paramMap)
        );
    }


}
