package tk.bolovsrol.utils.xml.soap;

import tk.bolovsrol.utils.Nullable;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.xml.Element;

public class SoapFault {

    private final FaultCode faultCode;
    private final String faultString;
    private final String faultActor;
    private final Element faultDetail;

    public SoapFault(FaultCode faultCode, String faultString) {
        this(faultCode, faultString, null, null);
    }

    public SoapFault(FaultCode faultCode, String faultString, @Nullable String faultActor) {
        this(faultCode, faultString, faultActor, null);
    }

    public SoapFault(FaultCode faultCode, String faultString, @Nullable Element faultDetail) {
        this(faultCode, faultString, null, faultDetail);
    }

    public SoapFault(FaultCode faultCode, String faultString,
                     @Nullable String faultActor, @Nullable Element faultDetail) {
        this.faultCode = faultCode;
        this.faultString = faultString;
        this.faultActor = faultActor;
        this.faultDetail = faultDetail;
    }

    public FaultCode getFaultCode() {
        return faultCode;
    }

    public String getFaultString() {
        return faultString;
    }

    public String getFaultActor() {
        return faultActor;
    }

    public Element getFaultDetail() {
        return faultDetail;
    }

    @Override public String toString() {
        return new StringDumpBuilder()
                .append("faultCode", faultCode)
                .append("faultString", faultString)
                .append("faultActor", faultActor)
                .append("faultDetail", faultDetail)
                .toString();
    }
}
