package tk.bolovsrol.utils.xml.soap;

public enum FaultCode {
    VERSION_MISMATCH("VersionMismatch"),
    MUST_UNDERSTAND("MustUnderstand"),
    CLIENT("Client"),
    SERVER("Server");

    private final String caption;

    FaultCode(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }
}
