package tk.bolovsrol.utils.xml;

import tk.bolovsrol.utils.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/** Преобразования текста, связанные с XML. */
public final class XmlUtils {

    private static final String[] INVALIDATE_KEYS = new String[]{"&", "\"", "'", "<", ">"};
    private static final String[] INVALIDATE_VALUES = new String[]{"&amp;", "&quot;", "&apos;", "&lt;", "&gt;"};
    private static final String[] DEINVALIDATE_KEYS = new String[]{"&quot;", "&apos;", "&lt;", "&gt;", "&amp;"};
    private static final String[] DEINVALIDATE_VALUES = new String[]{"\"", "'", "<", ">", "&"};

    private XmlUtils() {}

    /**
     * Инвалидирует текст так, чтобы его можно было отправить в xml или html,
     * и он не потёр бы контрольные символы.
     * <p/>
     * Фактически, «&amp;» заменяется на «&amp;amp;»,
     * «&quot;» заменяется на «&amp;quot;»,
     * «&apos;» заменяется на «&amp;apos;»,
     * «&lt;» заменяется на «&amp;gt;»,
     * «&gt;» заменяется на «&amp;lt;».
     *
     * @param original нормальная строка
     * @return инвалидированная строка
     * @see #xmlDeinvalidate(String)
     */
    public static String xmlInvalidate(String original) {
        return StringUtils.substitute(original, INVALIDATE_KEYS, INVALIDATE_VALUES);
    }

    /**
     * Преобразует символы, не помещающиеся в кодировку, в последовательность «&#код;»
     *
     * @param source      исходныне данные
     * @param charsetName предполагаемая кодировка
     * @return
     */
    public static String encodeNumericCharacterReferences(String source, String charsetName) {
        return encodeNumericCharacterReferences(source, Charset.forName(charsetName));
    }

    /**
     * Преобразует символы, не помещающиеся в кодировку, в последовательность «&#код;»
     *
     * @param source  исходныне данные
     * @param charset предполагаемая кодировка
     * @return
     */
    public static String encodeNumericCharacterReferences(String source, Charset charset) {
        CharsetEncoder charsetEncoder = charset.newEncoder();
        boolean changed = false;
        StringBuilder sb = new StringBuilder(source.length() * 4);
        sb.append(source);
        int i = 0;
        while (i < sb.length()) {
            char ch = sb.charAt(i);
            if (!charsetEncoder.canEncode(ch)) {
                String replacement = "&#" + (int) ch + ';';
                sb.replace(i, i + 1, replacement);
                i += replacement.length();
                changed = true;
            } else {
                i++;
            }
        }
        return changed ? sb.toString() : source;
    }


    /**
     * Восстанавливает инвалидированный для xml или html текст.
     * <p/>
     * Фактически, «&amp;amp;» заменяется на «&amp;»,
     * «&amp;quot;» заменяется на «&quot;»,
     * «&amp;apos;» заменяется на «&apos;»,
     * «&amp;gt;» заменяется на «&lt;»,
     * «&amp;lt;» заменяется на «&gt;».
     * <p/>
     * Также последовательности &#XX заменяются на собственные значения.
     *
     * @param invalidated покоцанная строка
     * @return нормальная строка
     */
    public static String xmlDeinvalidate(String invalidated) {
        return StringUtils.substitute(invalidated, DEINVALIDATE_KEYS, DEINVALIDATE_VALUES);
    }

    /**
     * Преобразует последовательности «&#код;» и «&#xкод;» в символы.
     *
     * @param source исходная строка
     * @return строка с символами вместо кодов
     */
    public static String decodeNumericCharacterReferences(String source) {
        int i = source.indexOf("&#");
        if (i < 0) {
            return source;
        }

        StringBuilder sb = new StringBuilder(source.length());
        sb.append(source);
        int[] codepoints = new int[1];
        do {
            int from = i + "&#".length();
            if (source.length() <= from) {
                break;
            }
            int to = sb.indexOf(";", from);
            if (to <= i) {
                break;
            }

            int radix;
            char firstChar = sb.charAt(from);
            if (firstChar == 'x' || firstChar == 'X') {
                radix = 16;
                from++;
            } else {
                radix = 10;
            }

            try {
                codepoints[0] = Integer.parseInt(sb.substring(from, to), radix);
            } catch (NumberFormatException e) {
                break;
            }
            String str = new String(codepoints, 0, 1);
            sb.replace(i, to + 1, str);

            i = sb.indexOf("&#");
        } while (i >= 0);

        return sb.toString();
    }
}
