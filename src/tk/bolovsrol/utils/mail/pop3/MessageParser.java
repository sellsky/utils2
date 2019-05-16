package tk.bolovsrol.utils.mail.pop3;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.io.LineInputStream;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.properties.PlainProperties;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/** Собственно читатель тела сообщения из потока. */
public class MessageParser {

    private final LogDome log;

    public MessageParser(LogDome log) {
        this.log = log;
    }

    public Pop3Message read(byte[] raw) throws IOException, UnexpectedBehaviourException {
        return read(raw, "UTF-8");
    }

    public Pop3Message read(byte[] raw, String charset) throws IOException, UnexpectedBehaviourException {
        ByteArrayInputStream bais = new ByteArrayInputStream(raw);
        LineInputStream lis = new LineInputStream(bais, charset);
        Pop3Message m = new Pop3Message();
        PlainProperties kl = m.getKludges();
        String latestKey = null;
        while (true) {
            String l = lis.readLine();
//            log.trace(">+ " + Spell.get(l));
            if (l == null || l.length() == 0) {
                break;
            } else {
                char firstChar = l.charAt(0);
                if (Character.isWhitespace(firstChar)) {
                    if (latestKey == null) {
                        log.warning("Invalid kludge line ignored: " + Spell.get(l));
                    } else {
                        kl.set(latestKey, kl.get(latestKey) + '\n' + l);
                    }
                } else {
                    int colonPos = l.indexOf((int) ':');
                    if (colonPos == -1 || colonPos >= l.length()) {
                        log.warning("Invalid kludge line ignored: " + Spell.get(l));
                        latestKey = null;
                    } else if (colonPos == l.length() - 1) {
                        latestKey = l.substring(0, colonPos);
                        kl.set(latestKey, "");
                    } else {
                        latestKey = l.substring(0, colonPos);
                        kl.set(latestKey, l.substring(colonPos + 2));
                    }
                }
            }
        }

        // всё, что осталось -- тело сообщения.
        byte[] body = new byte[bais.available()];
        //noinspection ResultOfMethodCallIgnored
        bais.read(body);
        m.setPayload(body);

        return m;
    }

//    public static void main(String[] args) throws Exception {
//        FileInputStream fis = new FileInputStream("W:\\work\\pop3\\message2");
//        LineInputStream lis = new LineInputStream(fis);
//        try {
//            TextPayloadDecoder tpd = new TextPayloadDecoder();
//            Pop3Message m = read(lis);
////            Log.hint(Spell.get(m));
//            Log.hint("Msg from: " + Spell.get(m.getDecodedFrom()));
//            Log.hint("Msg to: " + Spell.get(m.getDecodedTo()));
//            Log.hint("Msg subject: " + Spell.get(m.getDecodedSubject()));
//            Log.hint("Msg cc: " + Spell.get(m.getDecodedCC()));
//            if (m.isMultipart()) {
//                Pop3Message[] subm = new MultipartPayloadDecoder().decode(m);
//                Log.hint("Multipart, submessages count: " + subm.length);
//                for (int i = 0; i < subm.length; i++) {
//                    Pop3Message submessage = subm[i];
////                    Log.hint("submsg " + i + ": " + Spell.get(submessage));
//                    if (submessage.checkContentType("text/plain")) {
//                        Log.hint("Submsg " + i + " plain text payload: " + tpd.decode(submessage));
//                    }
//                }
//            } else {
//                Log.hint("Message is single part. ");
//                if (m.checkContentType("text/plain")) {
//                    Log.hint("Plain text payload: " + tpd.decode(m));
//                }
//            }
//        } finally {
//            lis.close();
//            fis.close();
//        }
//
//    }
}
