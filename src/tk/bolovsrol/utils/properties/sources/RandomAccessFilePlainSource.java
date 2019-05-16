package tk.bolovsrol.utils.properties.sources;

import tk.bolovsrol.utils.StringDumpBuilder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Плейн-соурс, который отражает записываемую информацию в файле.
 * <p/>
 * Это может быть файл с конфигом и комментариями. Все незначащие строки
 * будут сохранены, значения существующих параметров будут перезаписаны,
 * а новые параметры будут добавлены в конец файла.
 * <p/>
 * Этот соурс не имеет сложного парсера, как {@link FileReadOnlySource},
 * синтаксис самый простой: один "ключ=значение" на строке,
 * строки с первым сивмолом-решёткой считаются комментариями.
 * <p/>
 * В первую очередь назначение этого соурса -- хранить конфигурацию.
 */
public class RandomAccessFilePlainSource implements PlainSource {
    /** Файл для работы. Когда приложение активно, файл заблокирован */
    private final RandomAccessFile raf;
    /** Буфер для отображения файла в памяти. */
    private byte[] buf;
    /** Фактическая длина данных; совпадает с длиной файла. */
    private int length;
    /** Элементы по ключевым словам. */
    private final Map<String, Item> keywordToItem = new TreeMap<>();
    /** Элементы по их позиции в файле. */
    private final NavigableMap<Integer, Item> fromPositionToItem = new TreeMap<>();

    /** Элемент. Содержит ключевое слово, значение, а также позиции элемента в буфере. */
    private static class Item {
        String keyword;
        String value;
        int from;
        int valueFrom;
        int valueTo;
        int to;

        public void move(int shift) {
            this.from += shift;
            this.valueFrom += shift;
            this.valueTo += shift;
            this.to += shift;
        }

        @Override
        public String toString() {
            return new StringDumpBuilder()
                    .append("keyword", keyword)
                    .append("value", value)
                    .append("from", from)
                    .append("valueFrom", valueFrom)
                    .append("valueTo", valueTo)
                    .append("to", to)
                    .toString();
        }
    }

    /**
     * Создаёт соурс, замапленный на файл с указанным именем, открывает этот файл, читает его.
     *
     * @param targetFileName имя файла с конфигурацией
     * @throws IOException
     */
    public RandomAccessFilePlainSource(String targetFileName) throws IOException {
        this(new File(targetFileName));
    }

    /**
     * Создаёт соурс, замапленный на указанный файл, открывает этот файл, читает его.
     *
     * @param target
     * @throws IOException
     */
    public RandomAccessFilePlainSource(File target) throws IOException {
        raf = new RandomAccessFile(target, "rwd");
        readRaf();
        parseBuf();
    }

    private void readRaf() throws IOException {
        length = (int) raf.length();
        buf = new byte[length + 512];
        raf.readFully(buf, 0, length);
    }

    private void parseBuf() throws UnsupportedEncodingException {
        int pos = 0;
        if (pos >= length) {
            return;
        }
        while (true) {
            while (true) {
                byte b = buf[pos];
                if (b != (byte) '\n' && b != (byte) '\t' && b != (byte) ' ') {
                    break;
                }
                pos++;
                if (pos >= length) {
                    return;
                }
            }
            if (buf[pos] == '#') {
                // комментарий
                pos++;
                if (pos >= length) {
                    return;
                }
                while (buf[pos] != (byte) '\n') {
                    pos++;
                    if (pos >= length) {
                        return;
                    }
                }
            } else {
                // ключ=значение[\n]
                Item item = new Item();
                item.from = pos;
                while (buf[pos] != '=') {
                    pos++;
                    if (pos >= length) {
                        return;
                    }
                }
                item.keyword = new String(buf, item.from, pos - item.from, "UTF-8").trim();
                pos++;
                if (pos >= length) {
                    return;
                }
                item.valueFrom = pos;
                while (buf[pos] != (byte) '\n') {
                    pos++;
                    if (pos >= length) {
                        break;
                    }
                }
                item.valueTo = pos;
                item.to = pos;
                item.value = new String(buf, item.valueFrom, item.valueTo - item.valueFrom, "UTF-8").trim();
                keywordToItem.put(item.keyword, item);
                fromPositionToItem.put(item.from, item);

                // по возможности ещё захватим перевод строки после элемента.
                pos++;
                if (pos >= length) {
                    return;
                }
                item.to = pos;
            }
        }
    }

    /**
     * Cдвигаем содержимое буфера вперёд или назад.
     * Обновляем поля.
     *
     * @param from
     * @param to
     */
    private void shift(int from, int to) {
        if (from == to) {
            return;
        }
        int dif = to - from;
        if (from < to) {
            if (length + dif > buf.length) {
                buf = Arrays.copyOf(buf, buf.length + dif + 1024);
            }
            System.arraycopy(buf, from, buf, to, length - from);
        } else {
            System.arraycopy(buf, from, buf, to, length - to);
        }

        SortedMap<Integer, Item> itemsToMove = fromPositionToItem.tailMap(from);
        Item[] itemsArray = itemsToMove.values().toArray(new Item[itemsToMove.size()]);
        itemsToMove.clear();
        for (Item item : itemsArray) {
            item.move(dif);
            fromPositionToItem.put(item.from, item);
        }

        length += dif;
    }

    private static class SaveRange {
        int from = -1;
        int to = -1;

        public void add(int from, int to) {
            if (this.from < 0 || this.from > from) {
                this.from = from;
            }
            if (this.to < to) {
                this.to = to;
            }
        }
    }

    private void save(SaveRange saveRange) throws IOException {
        if (saveRange.from < 0 || saveRange.to < 0) {
            return;
        }
        save(saveRange.from, saveRange.to);
    }

    private void save(int from, int to) throws IOException {
        if (from != to) {
            raf.seek(from);
            raf.write(buf, from, to - from);
        }
        raf.setLength(length);
    }

    @Override public RandomAccessFilePlainSource clear() throws SourceUnavailableException {
        try {
            keywordToItem.clear();
            fromPositionToItem.clear();
            length = 0;
            raf.setLength(length);
        } catch (IOException e) {
            throw new SourceUnavailableException(e);
        }
        return this;
    }

    @Override public RandomAccessFilePlainSource drop(String key) throws SourceUnavailableException {
        try {
            Item item = keywordToItem.remove(key);
            if (item != null) {
                fromPositionToItem.remove(item.from);
                shift(item.to, item.from);
                save(item.from, length);
            }
        } catch (IOException e) {
            throw new SourceUnavailableException(e);
        }
        return this;
    }

    @Override public RandomAccessFilePlainSource set(String key, String value) throws SourceUnavailableException {
        try {
            SaveRange range = new SaveRange();
            setInternal(key, value, range);
            save(range);
        } catch (Exception e) {
            throw new SourceUnavailableException(e);
        }
        return this;
    }

    private RandomAccessFilePlainSource setInternal(String key, String value, SaveRange saveRange) throws UnsupportedEncodingException {
        Item item = keywordToItem.get(key);
        if (item == null) {
            // новое значение.

            // добавим в конце перевод строки, если его нет
            int saveFrom = length;
            if (length > 0 && buf[length - 1] != '\n') {
                shift(length, length + 1);
                buf[length - 1] = (byte) '\n';
            }

            // новые ключ и значение
            byte[] keyRaw = key.getBytes("UTF-8");
            byte[] valueRaw = value.getBytes("UTF-8");

            item = new Item();
            item.keyword = key;
            item.value = value;
            item.from = length;
            item.valueFrom = length + key.length() + 1;
            item.valueTo = item.valueFrom + valueRaw.length;
            item.to = item.valueTo + 1;

            // печатаем в буфер
            shift(length, item.to);
            System.arraycopy(keyRaw, 0, buf, item.from, keyRaw.length);
            buf[item.valueFrom - 1] = (byte) '=';
            System.arraycopy(valueRaw, 0, buf, item.valueFrom, valueRaw.length);
            buf[item.to - 1] = (byte) '\n';
            saveRange.add(saveFrom, length);

            // и добавляем в словарь
            keywordToItem.put(item.keyword, item);
            fromPositionToItem.put(item.from, item);
        } else if (!value.equals(item.value)) {
            // исправляем уже существующий элемент
            byte[] newValueRaw = value.getBytes("UTF-8");
            item.value = value;
            if (newValueRaw.length == item.valueTo - item.valueFrom) {
                // повезло, новая длина такая же как старая, ничего сдвигать не надо
                System.arraycopy(newValueRaw, 0, buf, item.valueFrom, newValueRaw.length);
                saveRange.add(item.valueFrom, item.valueTo);
            } else {
                int newValueTo = item.valueFrom + newValueRaw.length;
                int newTo = item.to - item.valueTo + newValueTo;
                shift(item.valueTo, newValueTo);
                System.arraycopy(newValueRaw, 0, buf, item.valueFrom, newValueRaw.length);
                item.valueTo = newValueTo;
                item.to = newTo;
                saveRange.add(item.valueFrom, length);
            }
        }
        return this;
    }

    @Override public RandomAccessFilePlainSource setAll(Map<String, String> matter) throws SourceUnavailableException {
        try {
            SaveRange range = new SaveRange();
            for (Map.Entry<String, String> entry : matter.entrySet()) {
                setInternal(entry.getKey(), entry.getValue(), range);
            }
            save(range);
        } catch (Exception e) {
            throw new SourceUnavailableException(e);
        }
        return this;
    }

    @Override public String get(String key) throws SourceUnavailableException {
        Item item = keywordToItem.get(key);
        return item == null ? null : item.value;
    }

    @Override public boolean has(String key) throws SourceUnavailableException {
        return keywordToItem.containsKey(key);
    }

    @Override public Map<String,String> dump() throws SourceUnavailableException {
        Map<String, String> dump = new LinkedHashMap<>();
        for (Item item : fromPositionToItem.values()) {
            dump.put(item.keyword, item.value);
        }
        return dump;
    }

    /**
     * Закрывает соурс, освобождает файл конфигурации.
     * После чего соурс может использоваться для чтения,
     * но все операции, изменяющие данные, будут выбрасывать {@link IOException}.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        raf.close();
    }
}
