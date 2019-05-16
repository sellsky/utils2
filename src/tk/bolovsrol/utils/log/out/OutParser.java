package tk.bolovsrol.utils.log.out;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.log.providers.StreamProviderException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Парсер {@link Out}.
 * <p/>
 * Синтаксис строки:
 * &lt;параметры_точки&gt;[;&lt;параметры_точки&gt;]...
 * <p/>
 * Синтаксис параметров точки:
 * &lt;параметр[:значение]&gt;[;&lt;параметр[:значение]&gt;]...
 * <p/>
 * Обработкой параметров занимается сборник процессоров {@link ProcessorPool}.
 * <p/>
 * Об ошибках пишет в {@link System#err}, ошибочные параметры игнорируются.
 */
public final class OutParser {

    private OutParser() {
    }

    /**
     * Разбирает переданную строку и формирует {@link Out}-точки для вывода лога.
     * <p/>
     * Неверные подстроки игнорируются.
     *
     * @param outLine
     * @return список распарсенных строк
     */
    public static List<Out> parse(String outLine) {
        ArrayList<Out> outs = new ArrayList<>();
        parse(outLine, outs);
        outs.trimToSize();
        return outs;
    }

    /**
     * Разбирает переданную строку и формирует {@link Out}-точки для вывода лога,
     * которые дописывает в переданную коллекцию.
     * <p/>
     * Неверные подстроки игнорируются.
     *
     * @param outLine
     * @param target
     */
    public static void parse(String outLine, Collection<Out> target) {
        String[] sources = StringUtils.parseDelimited(outLine, ';', '\\', StringUtils.QUOTES);
        for (String s : sources) {
            Out out = new Out();
            if (parse(s, out)) {
                target.add(out);
            }
        }
    }

    private static boolean parse(String src, Out out) {
        String[] words = StringUtils.parseDelimited(src, ',', '\\', StringUtils.QUOTES);
        for (String word : words) {
            String key, data;
            int colonPos = word.indexOf(':');
            if (colonPos < 0) {
                key = word;
                data = null;
            } else {
                key = word.substring(0, colonPos).trim();
                data = word.substring(colonPos + 1).trim();
            }
            WordProcessor processor = ProcessorPool.getProcessor(key);
            if (processor == null) {
                System.err.println("Unexpected keyword " + Spell.get(key) + ", definition " + Spell.get(src) + " ignored");
            } else {
                try {
                    processor.process(key, data, out);
                } catch (StreamProviderException e) {
                    System.err.println("Error processing key " + Spell.get(key) + ", definition " + Spell.get(src) + " ignored. " + Spell.get(e));
                }
            }
        }

        if (out.writer == null) {
            if (src == null || !src.isEmpty()) { System.err.println("No target defined in definition " + Spell.get(src) + ", definition ignored"); }
            return false;
        } else {
            return true;
        }
    }
}
