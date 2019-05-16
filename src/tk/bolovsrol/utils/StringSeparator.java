package tk.bolovsrol.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Разделяет строки на наименьшее количество сегментов,
 * длина каждого из которых не превышает заданного ограничения,
 * разделяя по возможности строку по словам, а при невозможности — наполняя по максимуму.
 * <p/>
 * Я использовал эту задачку в качестве тестового задания весной 2010.
 */
public class StringSeparator {

    public static List<String> separate(String source, int maxSegLen) throws IllegalArgumentException {
        if (source == null || source.length() <= maxSegLen) {
            return Collections.singletonList(source);
        }
        return formatSegments(source, aquireSegments(source, maxSegLen));
    }

    private static List<String> formatSegments(String source, List<Segment> segments) {
        ArrayList<String> substrings = new ArrayList<String>(segments.size());
        for (int i = 0; i < segments.size(); i++) {
            Segment segment = segments.get(i);
            substrings.add(getPrefix(i + 1, segments.size()) + source.substring(segment.getFrom(), segment.getTo()));
        }
        return substrings;
    }

    private static List<Segment> aquireSegments(String source, int maxSegLen) throws IllegalArgumentException {
        ArrayList<Segment> segments = new ArrayList<Segment>();
        int maxSegNum = 9;
        while (true) {
            int segNum = 1;
            int pos = 0;
            while (true) {
                int prefLen = getPrefix(segNum, maxSegNum).length();
                if (prefLen >= maxSegLen) {
                    throw new IllegalArgumentException("Segment length too low.");
                }
                Segment segment = Segment.aquire(source, pos, maxSegLen - prefLen);
                if (segment == null) {
                    return segments;
                }
                segments.add(segment);
                pos = segment.getTo();

                segNum++;
                if (segNum > maxSegNum) {
                    maxSegNum *= 10;
                    segments.clear();
                    break;
                }
            }
        }
    }

    private static String getPrefix(int segNum, int maxSegNum) {
        return ("" + segNum + '/' + maxSegNum + ' ');
    }

    private static class Segment {
        final int from;
        final int to;

        private Segment(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public static Segment aquire(String source, int from, int maxLen) {
            while (true) {
                if (from >= source.length()) {
                    return null;
                }
                if (!Character.isWhitespace(source.charAt(from))) {
                    break;
                }
                from++;
            }
            return new Segment(from, foundSegEnd(source, from, maxLen));
        }

        private static int foundSegEnd(String source, int from, int maxLen) {
            int to = from + maxLen;
            if (to >= source.length()) {
                return source.length();
            }
            int wordEnd = to;
            while (true) {
                if (wordEnd <= from) {
                    return to;
                }
                if (Character.isWhitespace(source.charAt(wordEnd))) {
                    break;
                }
                wordEnd--;
            }
            while (true) {
                if (wordEnd <= from) {
                    return to;
                }
                if (!Character.isWhitespace(source.charAt(wordEnd))) {
                    return wordEnd + 1;
                }
                wordEnd--;
            }
        }

        public int getFrom() {
            return from;
        }

        public int getTo() {
            return to;
        }
    }

    public static void main(String[] args) throws Exception {
        String s = "one two three four five six seven eight nine ten eleven twelve thirteen fourteen fiveteen sixteen seventeen eighteen nineteen twenty";
//        String s = "123456789012345 123456789012345 123456789012345 123456789012345 123456789012345 123456789012345 123456789012345 123456789012345 123456789012345 123456789012345";
//        String s = "one two three four five sixabcd";
        int limit = 20;

        System.out.println(Spell.get(s));
        for (String segs : separate(s, limit)) {
            System.out.println(Spell.get(segs) + ' ' + segs.length());
        }
    }
}
