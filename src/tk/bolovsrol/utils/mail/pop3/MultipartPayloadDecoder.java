package tk.bolovsrol.utils.mail.pop3;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.io.LineInputStream;
import tk.bolovsrol.utils.properties.PlainProperties;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/** Выковыривает части сообщений. */
public class MultipartPayloadDecoder {
    private static final String BOUNDARY_PREFIX = "boundary=";
    private static final int BOUNDARY_PREFIX_LEN = BOUNDARY_PREFIX.length();

    private static final StringUtils.TrimFilter QUOTE_FILTER = new StringUtils.TrimFilter() {
        public boolean allowTrim(char ch, String source, int pos, StringUtils.TrimMode direction) {
            return ch == '"';
        }
    };

    private static String retrieveBoundary(String contentType) throws Pop3MessageException {
        StringTokenizer st = new StringTokenizer(contentType, "; \n\r\t");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (s.startsWith(BOUNDARY_PREFIX)) {
                return StringUtils.trim(s.substring(BOUNDARY_PREFIX_LEN), QUOTE_FILTER, StringUtils.TrimMode.BOTH);
            }
        }
        throw new Pop3MessageException("Boundary not specified in Content Type " + Spell.get(contentType));
    }

    public Pop3Message[] decode(Pop3Message message) throws Pop3MessageException {
        String contentType = message.getContentType();
        if (contentType == null || !contentType.startsWith("multipart/")) {
            throw new Pop3MessageException("Unexpected content type " + Spell.get(contentType));
        }

        String boundary = retrieveBoundary(contentType);
        ArrayList<Pop3Message> al = new ArrayList<Pop3Message>();
        startBoundary = "--" + boundary;
        endBoundary = startBoundary + "--";
        lis = new LineInputStream(new ByteArrayInputStream(message.getPayload()));
        try {
            try {
                if (readUntilBoundary()) {
                    while (true) {
                        nextMessage = new Pop3Message();
                        al.add(nextMessage);
                        readKludges();
                        if (!readPayload()) {
                            break;
                        }
                    }
                }
            } finally {
                lis.close();
            }
        } catch (IOException e) {
            throw new Pop3MessageException(e);
        }
        return al.toArray(new Pop3Message[al.size()]);
    }

    //    private BufferedReader br;
    private LineInputStream lis;

    private String endBoundary;
    private String startBoundary;

    private Pop3Message nextMessage;

    private boolean readUntilBoundary() throws IOException {
        while (true) {
            String l = lis.readLine();
            if (l == null) {
                throw new EOFException();
            }
            if (l.equals(endBoundary)) {
                return false;
            }
            if (l.equals(startBoundary)) {
                return true;
            }
        }
    }

    private void readKludges() throws IOException {
        PlainProperties kl = nextMessage.getKludges();
        String latestKey = null;
        while (true) {
            String l = lis.readLine();
            if (l == null) {
                throw new EOFException();
            }
            if (l.length() == 0) {
                return;
            }
            if (Character.isWhitespace(l.charAt(0))) {
                if (latestKey == null) {
                    //log.warning("Invalid kludge line ignored: " + Spell.get(l));
                } else {
                    kl.set(latestKey, kl.get(latestKey) + '\n' + l);
                }
            } else {
                int colonPos = l.indexOf((int) ':');
                if (colonPos == -1 || colonPos >= l.length()) {
                    //log.warning("Invalid kludge line ignored: " + Spell.get(l));
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

    private boolean readPayload() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            while (true) {
                byte[] l = lis.readUntilLineSeparator();
                if (l == null) {
                    throw new EOFException();
                }
                String s = new String(l);
                if (s.equals(startBoundary)) {
                    return true;
                } else if (s.equals(endBoundary)) {
                    return false;
                }
                if (baos.size() != 0) {
                    baos.write((int) '\n');
                }
                baos.write(l);
            }
        } finally {
            nextMessage.setPayload(baos.toByteArray());
        }
    }

    public static Pop3Message pickByContentType(String contentType, Pop3Message multipartMessage) throws Pop3MessageException {
        return pickByContentType(contentType, new MultipartPayloadDecoder().decode(multipartMessage));
    }

    public static Pop3Message pickByContentType(String contentType, Pop3Message[] messages) {
        for (Pop3Message message : messages) {
            if (message.checkContentType(contentType)) {
                return message;
            }
        }
        return null;
    }
}
