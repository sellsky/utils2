package tk.bolovsrol.utils.mail.pop3;

import tk.bolovsrol.utils.MimeUtils;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.properties.PlainProperties;
import tk.bolovsrol.utils.properties.PropertyNotFoundException;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Сообщение, у которого есть только те поля, которые нас интересуют. */
public class Pop3Message {

    private final PlainProperties kludges = new PlainProperties();
    private byte[] payload;

    public PlainProperties getKludges() {
        return kludges;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return new StringDumpBuilder()
                .append("kludges", kludges)
                .append("payload", payload)
                .toString();
    }

    //--------- parts

    public boolean isMultipart() {
        return checkContentType("multipart");
    }

    @SuppressWarnings({"StringEquality"})
    public boolean checkContentType(String type) {
        String contentType = getContentType();
        if (contentType == null || type == null) {
            return contentType == type;
        } else {
            if (type.indexOf((int) '/') == -1) {
                type += "/";
            }
            return contentType.startsWith(type);
        }
    }

    public String getContentType() {
        return kludges.get("Content-Type");
    }

    public String getContentTransferEncoding() {
        return kludges.get("Content-Transfer-Encoding");
    }

    public String getMessageId() {
        return kludges.get("Message-Id");
    }

    public String getFrom() {
        return kludges.get("From");
    }

    public String getTo() {
        return kludges.get("To");
    }

    public String getCC() {
        return kludges.get("CC");
    }

    public Date getDate() throws ParseException {
        return parsePop3Date(kludges.get("Date"));
    }

    public String getSubject() {
        return kludges.get("Subject");
    }

    public String getContentTypeOrDie() throws PropertyNotFoundException {
        return kludges.getOrDie("Content-Type");
    }

    public String getContentTransferEncodingOrDie() throws PropertyNotFoundException {
        return kludges.getOrDie("Content-Transfer-Encoding");
    }

    public String getMessageIdOrDie() throws PropertyNotFoundException {
        return kludges.getOrDie("Message-Id");
    }

    public String getFromOrDie() throws PropertyNotFoundException {
        return kludges.getOrDie("From");
    }

    public String getToOrDie() throws PropertyNotFoundException {
        return kludges.getOrDie("To");
    }

    public String getCCOrDie() throws PropertyNotFoundException {
        return kludges.getOrDie("CC");
    }

    public Date getDateOrDie() throws PropertyNotFoundException, ParseException {
        return parsePop3Date(kludges.getOrDie("Date"));
    }

    public String getSubjectOrDie() throws PropertyNotFoundException {
        return kludges.getOrDie("Subject");
    }

    public String getDecodedSubjectOrDie() throws PropertyNotFoundException, UnsupportedEncodingException {
        return MimeUtils.decode(getSubjectOrDie());
    }

    public String getDecodedFromOrDie() throws PropertyNotFoundException, UnsupportedEncodingException {
        return MimeUtils.decode(getFromOrDie());
    }

    public String getDecodedToOrDie() throws PropertyNotFoundException, UnsupportedEncodingException {
        return MimeUtils.decode(getToOrDie());
    }

    public String getDecodedCCOrDie() throws PropertyNotFoundException, UnsupportedEncodingException {
        return MimeUtils.decode(getCCOrDie());
    }

    public String getDecodedSubject() throws UnsupportedEncodingException {
        return MimeUtils.decode(getSubject());
    }

    public String getDecodedFrom() throws UnsupportedEncodingException {
        return MimeUtils.decode(getFrom());
    }

    public String getDecodedTo() throws UnsupportedEncodingException {
        return MimeUtils.decode(getTo());
    }

    public String getDecodedCC() throws UnsupportedEncodingException {
        return MimeUtils.decode(getCC());
    }

    private static Date parsePop3Date(String dateString) throws ParseException {
        if (dateString == null) {
            return null;
        }
        // отрежем день недели, если он есть, тогда четвёртый символ - запятая.
        if (dateString.length() > 4 && dateString.charAt(3) == ',') {
            dateString = dateString.substring(4);
        }
        return new SimpleDateFormat("d MMM yyyy HH:mm:ss Z", Locale.ENGLISH).parse(dateString);
    }
}
