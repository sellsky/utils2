package tk.bolovsrol.utils.xml.soap;

import tk.bolovsrol.utils.Nullable;
import tk.bolovsrol.utils.xml.NamespaceUtils;

public class SoapFactory {
    private final String nsPrefix;
    private final String nsUri;

    public SoapFactory(@Nullable String namespaceName, String nsUri) {
        this.nsPrefix = NamespaceUtils.getNamespacePrefix(namespaceName);
        this.nsUri = nsUri;
    }

    public SoapFactory(String nsUri) {
        this(null, nsUri);
    }

    public SoapFactory() {
        this(SoapConst.NS_URI_SOAP2);
    }

    public SoapEnvelope newEnvelope() {
        return SoapEnvelope.newAndNsAttr(nsPrefix, nsUri);
    }

    public SoapAction newAction(String name) {
        return SoapAction.newAndNsAttr(nsUri, nsPrefix + name);
    }

}
