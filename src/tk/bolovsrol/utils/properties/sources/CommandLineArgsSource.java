package tk.bolovsrol.utils.properties.sources;

import tk.bolovsrol.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Читатель пропертей из командной строки.
 * <p/>
 * Правила такие. Каждый элемент: ключ=значение. Если знака равно нету,
 * то значение — следующий элемент. Если ключ повторяется, то значения складываются
 * через перевод строки. Если ключ последний элемент, то его значение нул.
 */
public class CommandLineArgsSource extends MapReadOnlySource {

    private static final DashFilter DASH_FILTER = new DashFilter();

    public CommandLineArgsSource(String[] args) {
        super(parse(args, 0, false));
    }

    public CommandLineArgsSource(String[] args, int from) {
        super(parse(args, from, false));
    }

    public CommandLineArgsSource(String[] args, boolean cutLeadingKeyDashes) {
        super(parse(args, 0, cutLeadingKeyDashes));
    }

    public CommandLineArgsSource(String[] args, int from, boolean cutLeadingKeyDashes) {
        super(parse(args, from, cutLeadingKeyDashes));
    }

    @Override public String expand(String localBranchKey) {
        return localBranchKey;
    }

    private static Map<String, String> parse(String[] args, int from, boolean cutLeadingKeyDashes) {
        Map<String, String> result = new HashMap<>(args.length);
        for (int i = from; i < args.length; i++) {
            String arg = args[i];
            if (cutLeadingKeyDashes) {
                StringUtils.trim(arg, DASH_FILTER, StringUtils.TrimMode.FROM_LEFT);
            }
            int delim = arg.indexOf((int) '=');
            if (delim == -1) {
                i++;
                try {
                    addMapping(result, arg, args[i]);
                } catch (IndexOutOfBoundsException ignored) {
                    addMapping(result, arg, null);
                }
            } else {
                addMapping(result, arg.substring(0, delim), arg.substring(delim + 1));
            }
        }
        return result;
    }

    private static void addMapping(Map<String, String> target, String key, String value) {
        String existingValue = target.put(key, value);
        if (existingValue != null) {
            target.put(key, existingValue + ';' + value);
        }
    }

    private static class DashFilter implements StringUtils.TrimFilter {
        @Override
        public boolean allowTrim(char ch, String source, int pos, StringUtils.TrimMode direction) {
            return ch == '-';
        }
    }
}
