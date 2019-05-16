package tk.bolovsrol.utils.mail.smtp;

import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.mail.MailAddress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/** Примитивное текстовое сообщение для отправки по smtp. */
public class SmtpMessage {

    public static final SmtpContentTransferEncoder DEFAULT_CONTENT_TRANSFER_ENCODER = new Base64ContentTransferEncoder();
    public static final SmtpContentTypeEncoder DEFAULT_CONTENT_TYPE_ENCODER = new PlaintextSmtpContentTypeEncoder();

    private String id;
    private MailAddress from;
    private final List<MailAddress> to = new ArrayList<MailAddress>();
    private final List<MailAddress> cc = new ArrayList<MailAddress>();
    private final List<MailAddress> bcc = new ArrayList<MailAddress>();
    private Date date = new Date();
    private SmtpContentTransferEncoder contentTransferEncoder = DEFAULT_CONTENT_TRANSFER_ENCODER;
    private SmtpContentTypeEncoder contentTypeEncoder = DEFAULT_CONTENT_TYPE_ENCODER;
    private String subject;
    private String payload;

    @Override
    public String toString() {
        return new StringDumpBuilder()
                .append("id", id)
                .append("from", from)
                .append("to", to)
                .append("cc", cc)
                .append("bcc", bcc)
                .append("date", date)
                .append("contentTransferEncoder", contentTransferEncoder)
                .append("contentTypeEncoder", contentTypeEncoder)
                .append("subject", subject)
                .append("payload", payload)
                .toString();
    }

    public SmtpMessage copy() {
        SmtpMessage copy = new SmtpMessage();
        copy.id = this.id;
        copy.from = this.from;
        copy.to.addAll(this.to);
        copy.cc.addAll(this.cc);
        copy.bcc.addAll(this.bcc);
        copy.date = this.date;
        copy.contentTransferEncoder = this.contentTransferEncoder;
        copy.contentTypeEncoder = this.contentTypeEncoder;
        copy.subject = this.subject;
        copy.payload = this.payload;
        return copy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MailAddress getFrom() {
        return from;
    }

    public void setFrom(String from) {
        setFrom(MailAddress.parse(from));
    }

    public void setFrom(MailAddress from) {
        this.from = from;
    }

    public List<MailAddress> getTo() {
        return to;
    }

    public void addTo(MailAddress... to) {
        if (to != null) {
            this.addTo(Arrays.asList(to));
        }
    }

    public void addTo(Collection<MailAddress> to) {
        if (to != null) {
            this.to.addAll(to);
        }
    }

    public void addTo(MailAddress to) {
        if (to != null) {
            this.to.add(to);
        }
    }

    public void addTo(String to) {
        if (to != null) {
            addTo(MailAddress.parseMulti(to));
        }
    }

    public List<MailAddress> getCc() {
        return cc;
    }

    public void addCc(MailAddress... cc) {
        if (cc != null) {
            this.addCc(Arrays.asList(cc));
        }
    }

    public void addCc(Collection<MailAddress> cc) {
        if (cc != null) {
            this.cc.addAll(cc);
        }
    }

    public void addCc(MailAddress cc) {
        if (cc != null) {
            this.cc.add(cc);
        }
    }

    public void addCc(String cc) {
        if (cc != null) {
            addCc(MailAddress.parseMulti(cc));
        }
    }

    public List<MailAddress> getBcc() {
        return bcc;
    }

    public void addBcc(MailAddress... bcc) {
        if (bcc != null) {
            this.addBcc(Arrays.asList(bcc));
        }
    }

    public void addBcc(Collection<MailAddress> bcc) {
        if (bcc != null) {
            this.bcc.addAll(bcc);
        }
    }

    public void addBcc(MailAddress bcc) {
        if (bcc != null) {
            this.bcc.add(bcc);
        }
    }

    public void addBcc(String bcc) {
        if (bcc != null) {
            addBcc(MailAddress.parseMulti(bcc));
        }
    }

    public Date getDate() {
        return date;
    }

//    public void setDate(Date date) {
//        this.date = date;
//    }

    public SmtpContentTransferEncoder getContentTransferEncoder() {
        return contentTransferEncoder;
    }

    public void setContentTransferEncoder(SmtpContentTransferEncoder contentTransferEncoder) {
        this.contentTransferEncoder = contentTransferEncoder;
    }

    public SmtpContentTypeEncoder getContentTypeEncoder() {
        return contentTypeEncoder;
    }

    public void setContentTypeEncoder(SmtpContentTypeEncoder contentTypeEncoder) {
        this.contentTypeEncoder = contentTypeEncoder;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
