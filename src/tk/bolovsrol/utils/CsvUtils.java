package tk.bolovsrol.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CsvUtils {

    public static final String UNIX_LINE_DELIMITER = "\n";
    public static final String DOS_LINE_DELIMITER = "\r\n";
    public static final String COMMA = ",";
    public static final String TAB = "\t";

    private CsvUtils() {
    }

    public static List<Map<String, String>> parseWithHeader(String csv, String lineDelimiter, String itemDelimiter, boolean emptyIsNull) {
        String[] lines = StringUtils.parseDelimited(csv, lineDelimiter);
        if (lines.length < 1) {
            return null;
        }
        if (lines.length < 2) {
            return Collections.emptyList();
        }
        List<Map<String, String>> result = new ArrayList<>(lines.length - 1);
        String[] headers = StringUtils.parseDelimited(lines[0], itemDelimiter);
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isEmpty()) {
                continue;
            }
            String[] values = StringUtils.parseDelimited(lines[i], itemDelimiter);

            Map<String, String> entry = new HashMap<>();
            result.add(entry);
            int u = Math.min(headers.length, values.length);
            while (u > 0) {
                u--;
                String value = values[u];
                entry.put(headers[u], emptyIsNull && value.isEmpty() ? null : value);
            }
        }
        return result;
    }

//    public static void main(String[] args) throws IOException {
//        FileChannel fc = FileChannel.open(FileSystems.getDefault().getPath("/home/sk/work/nmp/Port_Increment_201401131800_657.csv"), StandardOpenOption.READ);
//        String s = Charset.defaultCharset().decode(fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size())).toString();
//
//        List<Map<String, String>> parsed = parseWithHeader(s, DOS_LINE_DELIMITER, COMMA, true);
//        int i = 1;
//        for (Map<String, String> stringStringMap : parsed) {
//            System.out.println(i);
//            i++;
//            for (Map.Entry<String, String> entry : stringStringMap.entrySet()) {
//                System.out.println(Spell.get(entry.getKey()) + '→' + Spell.get(entry.getValue()));
//            }
//        }
//
//
//    }
}
