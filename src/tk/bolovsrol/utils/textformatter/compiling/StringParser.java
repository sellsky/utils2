package tk.bolovsrol.utils.textformatter.compiling;

import tk.bolovsrol.utils.StringUtils;

/** Как {@link java.util.StringTokenizer}, только понимает маскировку и кавычки символов. */
class StringParser {

    private final char delimiter;
    private final char mask;
    private final char[] quotes;

    private final String source;
    private final char[] sourceChars;
    private final int len;

    private int from;
    private int to;
    private String word;

    public StringParser(String source, char delimiter, char mask, char[] quotes) {
        this.source = source;
        this.delimiter = delimiter;
        this.mask = mask;
        this.quotes = quotes;
        this.sourceChars = source.toCharArray();
        this.len = source.length();
        this.from = -1;
        this.to = 0;
    }

    public boolean next() {
        while (true) {
            if (to >= len) {
                return false;
            }
            if (sourceChars[to] != delimiter) {
                break;
            }
            to++;
        }

        from = to;

        to = StringUtils.getClosingPosition(source, '\0', delimiter, from, mask, quotes);
        if (to < 0) {
            to = len;
        }
        word = StringUtils.unmask(source.substring(from, to), mask, quotes);
        return true;
    }

    public String getWord() {
        return word;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

}
