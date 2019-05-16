package tk.bolovsrol.utils.mail.smtp;

import tk.bolovsrol.utils.Base64;

import java.io.UnsupportedEncodingException;

/**
 *
 */
public class Base64MimeEncoder {

    private Base64MimeEncoder() {}

    public static String encode(String text) {
        if (text == null) {
            return null;
        }

        char[] chars = text.toCharArray();
        for (char ch : chars) {
            if (ch < ' ' || ch > (char) 0x7f) {
                return recode(text);
            }
        }
        return text;
    }

    private static String recode(String src) {
        StringBuilder sb = new StringBuilder(src.length() * 2);
        sb.append("=?UTF-8?B?");
        try {
            sb.append(Base64.byteArrayToBase64(src.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        sb.append("?=");
        return sb.toString();
    }


    @Override
    public String toString() {
        return "mime/base64";
    }
}
