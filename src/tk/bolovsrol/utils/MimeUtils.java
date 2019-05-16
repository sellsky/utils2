package tk.bolovsrol.utils;

import java.io.UnsupportedEncodingException;

public final class MimeUtils {

    private static final String PREFIX = "=?";
    private static final String INFIX = "?";
    private static final String SUFFIX = "?=";

    private MimeUtils() {
    }

    public static String decode(String source) throws UnsupportedEncodingException {
        if (source == null || source.isEmpty()) {
            return source;
        }

        return resolveEncodedWords(source);
    }

    private static String resolveEncodedSpaces(String source) {
        int underscorePos = source.indexOf((int) '_');
        if (underscorePos == -1) {
            return source;
        }
        while (true) {
            StringBuilder sb = new StringBuilder(source.length());
            sb.append(source);
            sb.setCharAt(underscorePos, ' ');
            underscorePos = source.indexOf((int) '_', underscorePos + 1);
            if (underscorePos == -1) {
                return sb.toString();
            }
        }
    }

    private static String resolveEncodedWords(String source) throws UnsupportedEncodingException {
        int prefixPos, suffixPos;

        prefixPos = source.indexOf(PREFIX);
        if (prefixPos == -1) {
            return source;
        }

        StringBuilder sb = new StringBuilder(source.length() * 2);
        suffixPos = 0;

        while (true) {
            sb.append(source.substring(suffixPos, prefixPos));

            // charset?encoding?text
            int firstQPos = source.indexOf(INFIX, prefixPos + PREFIX.length());
            if (firstQPos == -1) {
                sb.append(source.substring(prefixPos));
                return sb.toString();
            }
            int secondQPos = source.indexOf(INFIX, firstQPos + INFIX.length());
            if (secondQPos == -1) {
                sb.append(source.substring(prefixPos));
                return sb.toString();
            }
            suffixPos = source.indexOf(SUFFIX, secondQPos + INFIX.length());
            if (suffixPos == -1) {
                sb.append(source.substring(prefixPos));
                return sb.toString();
            }

            String charset = source.substring(prefixPos + PREFIX.length(), firstQPos);
            String encoding = source.substring(firstQPos + 1, secondQPos);
            String payload = source.substring(secondQPos + 1, suffixPos);
            switch (encoding) {
            case "Q":
                sb.append(new String(QuotedPrintable.decode(resolveEncodedSpaces(payload)), charset));
                break;
            case "B":
                sb.append(new String(Base64.base64ToByteArray(payload), charset));
                break;
            default:
                sb.append(source.substring(prefixPos));
                return sb.toString();
            }

            suffixPos += SUFFIX.length();
            prefixPos = source.indexOf(PREFIX, suffixPos);
            if (prefixPos == -1) {
                sb.append(source.substring(suffixPos));
                return sb.toString();
            }
        }
    }


}
