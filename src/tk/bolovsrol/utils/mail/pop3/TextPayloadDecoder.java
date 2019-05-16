package tk.bolovsrol.utils.mail.pop3;

import tk.bolovsrol.utils.Base64;
import tk.bolovsrol.utils.QuotedPrintable;
import tk.bolovsrol.utils.Spell;

import java.io.UnsupportedEncodingException;

/** Выковыривает текстовое содержимое сообщения. */
public final class TextPayloadDecoder {
    private static final String CHARSET_PREFIX = "charset=";

    private TextPayloadDecoder() {
    }

    public static String decode(Pop3Message message) throws Pop3MessageException, UnsupportedEncodingException {
        String contentType = message.getContentType();
        if (contentType != null && !contentType.startsWith("text/")) {
            throw new Pop3MessageException("Unexpected content type " + Spell.get(contentType));
        }

        String charset = "UTF-8";
        if (contentType != null) {
            int charsetFrom = contentType.indexOf(CHARSET_PREFIX);
            if (charsetFrom > 0 && Character.isWhitespace(contentType.charAt(charsetFrom - 1))) {
                charsetFrom += CHARSET_PREFIX.length();
                if (contentType.charAt(charsetFrom) == '"') {
                    charsetFrom++;
                }
                int charsetTo;
                charsetTo = contentType.indexOf((int) ';', charsetFrom);
                if (charsetTo == -1) {
                    charsetTo = contentType.length();
                }
                if (contentType.charAt(charsetTo - 1) == '"') {
                    charsetTo--;
                }
                charset = contentType.substring(charsetFrom, charsetTo);
            }
        }

        String cte = message.getContentTransferEncoding();
        byte[] payload = message.getPayload();
        int payloadLen = payload.length;
        if (contentType != null && payloadLen > 0 && payload[payloadLen - 1] == (byte) '\n') {
            payloadLen--;
        }
        if (cte == null) {
            return new String(payload, 0, payloadLen);
        } else if (cte.equals("quoted-printable")) {
            return new String(QuotedPrintable.decode(new String(payload, 0, payloadLen)), charset);
        } else if (cte.equals("base64")) {
            return new String(Base64.base64ToByteArray(new String(payload, 0, payloadLen)), charset);
        } else if (cte.equals("8bit") || cte.equals("7bit")) {
            return new String(payload, 0, payloadLen, charset);
        } else {
            throw new Pop3MessageException("Unknown Content Transfer Encoding " + Spell.get(cte));
        }
    }

}
