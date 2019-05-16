package tk.bolovsrol.utils.xml.soap;

import tk.bolovsrol.utils.NotNull;
import tk.bolovsrol.utils.Nullable;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.properties.PlainProperties;
import tk.bolovsrol.utils.xml.NamespaceUtils;

import java.util.ArrayList;
import java.util.List;

public class SoapEnvelope {

    private final String nsPrefix;
    private final String nsAttrName;
    private final PlainProperties attributes;
    private final List<SoapAction> headerActions;
    private final List<SoapAction> bodyActions;
    private SoapFault soapFault;

    /** Создаёт пустой конверт, который затем можно наполнять. */
    public SoapEnvelope() {
        this(null);
    }

    /**
     * Создаёт пустой конверт, который затем можно наполнять.
     *
     * @param nsPrefix
     */
    public SoapEnvelope(@Nullable String nsPrefix) {
        this(nsPrefix, new PlainProperties(), new ArrayList<SoapAction>(), new ArrayList<SoapAction>());
    }

    public SoapEnvelope(
            @Nullable String namespace,
            @NotNull PlainProperties attributes,
            @NotNull List<SoapAction> headerActions,
            @NotNull List<SoapAction> bodyActions) {
        this.nsPrefix = NamespaceUtils.getNamespacePrefix(namespace);
        this.nsAttrName = NamespaceUtils.getNamespaceAttr(namespace);
        this.attributes = attributes;
        this.headerActions = headerActions;
        this.bodyActions = bodyActions;
    }

    public String getNsPrefix() {
        return nsPrefix;
    }

    public String getNsAttrName() {
        return nsAttrName;
    }

    public PlainProperties attributes() {
        return attributes;
    }

    public List<SoapAction> headerActions() {
        return headerActions;
    }

    public List<SoapAction> bodyActions() {
        return bodyActions;
    }

    public SoapFault getSoapFault() {
        return soapFault;
    }

    public void setSoapFault(SoapFault soapFault) {
        this.soapFault = soapFault;
    }

    public static SoapEnvelope newAndNsAttr(@Nullable String nsPrefix, String nsUriSoap) {
        SoapEnvelope se = new SoapEnvelope(nsPrefix);
        se.attributes().set(se.getNsAttrName(), nsUriSoap);
        return se;
    }

    @Override public String toString() {
        return new StringDumpBuilder()
                .append("nsPrefix", nsPrefix)
                .append("nsAttrName", nsAttrName)
                .append("attributes", attributes)
                .append("headerEntries", headerActions)
                .append("bodyEntries", bodyActions)
                .append("soapFault", soapFault)
                .toString();
    }
}
