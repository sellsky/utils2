package tk.bolovsrol.utils.xml.soap;

import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.properties.PlainProperties;
import tk.bolovsrol.utils.xml.NamespaceUtils;

public class SoapAction {

    private final String nsUri;
    private final String name;
    private final PlainProperties attributes;
    private final PlainProperties params;

    public SoapAction(String nsUri, String name, PlainProperties attributes, PlainProperties params) {
        this.nsUri = nsUri;
        this.name = name;
        this.attributes = attributes;
        this.params = params;
    }

    public String getNsUri() {
        return nsUri;
    }

    public String getName() {
        return name;
    }

    public PlainProperties attributes() {
        return attributes;
    }

    public PlainProperties params() {
        return params;
    }

    public static SoapAction newAndNsAttr(String nsUri, String name) {
        SoapAction se = new SoapAction(nsUri, name, new PlainProperties(), new PlainProperties());
        se.attributes().set(NamespaceUtils.getNamespaceAttr(NamespaceUtils.extractNamespace(name)), nsUri);
        return se;
    }

    @Override public String toString() {
        return new StringDumpBuilder()
                .append("nsUri", nsUri)
                .append("name", name)
                .append("attributes", attributes)
                .append("params", params)
                .toString();
    }
}
