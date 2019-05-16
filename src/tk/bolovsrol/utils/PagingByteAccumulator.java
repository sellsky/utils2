package tk.bolovsrol.utils;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Компактный аккумулятор, предназначенный для записи массива байтов с минимальным разумным оверхедом, но быстро.
 * <p>
 * Накапливает данные в страницах — массивах фиксированной длины, создавая новые по мере необходимости.
 * <p>
 * Предполагается использовать цепочкой вместо стринг-билдера {@link StringBuilder} в случаях, когда строка собирается ради преобразования её в байты.
 * <p>
 * Можно добавлять строки, массивы байтов и отдельные байты.
 * <p>
 * Также можно добавлять числа, символы и произвольные объекты — они будут преобразованы в строку.
 */
public class PagingByteAccumulator {
    /** Заполненные страницы. */
    private final ArrayList<byte[]> fullPages = new ArrayList<>(256);
    /** Размер страницы. */
    private final int pageSize;

    /** Актуальный чарсет для преобразования строк. */
    private Charset charset = StandardCharsets.UTF_8;

    /** Недозаполненная страница. */
    private byte[] page;
    /** Курсор. */
    private int pos;
    /** Обратный курсор. */
    private int left;

    /** Создаёт аккумулятор с размером страницы в 1 мебагайт. */
    public PagingByteAccumulator() {
        this(1048576);
    }

    /** Создаёт аккумулятор указанным с размером страницы. */
    public PagingByteAccumulator(int pageSize) {
        this.pageSize = pageSize;
        this.page = new byte[pageSize];
        this.pos = 0;
        this.left = pageSize;
    }

    private void nextPage() {
        fullPages.add(page);
        page = new byte[pageSize];
        pos = 0;
        left = pageSize;
    }

    /**
     * Добавляет байт в аккумулятор.
     *
     * @param b
     * @return this
     */
    public PagingByteAccumulator append(byte b) {
        if (left == 0) { nextPage(); }
        page[pos] = b;
        pos++;
        left--;
        return this;
    }

    /**
     * Меняет значение последнего записанного байта на указанное.
     * <p>
     * Если вызвать на пустом аккумуляторе, выкинет {@link ArrayIndexOutOfBoundsException}. Не надо так.
     *
     * @param b
     * @return this
     */
    public PagingByteAccumulator setLastByte(byte b) {
        page[pos - 1] = b;
        return this;
    }

    /**
     * Добавляет массив байтов в аккумулятор.
     *
     * @param bytes
     * @return this
     */
    public PagingByteAccumulator append(byte[] bytes) {
        int from = 0;
        do {
            int toCopyLen = bytes.length - from;
            if (toCopyLen > left) {
                System.arraycopy(bytes, from, page, pos, left);
                from += left;
                nextPage();
            } else {
                System.arraycopy(bytes, from, page, pos, toCopyLen);
                pos += toCopyLen;
                left -= toCopyLen;
                return this;
            }
        } while (true);
    }

    /**
     * Добавляет строку, преобразованную в массив байтов актуальным чарсетом.
     *
     * @param s
     * @return this
     * @see #getCharset()
     * @see #setCharset(Charset)
     */
    public PagingByteAccumulator append(String s) {
        append(s.getBytes(charset));
        return this;
    }

    /**
     * Возвращает содержимое аккумулятора.
     *
     * @return содержимое аккумулятора
     */
    public byte[] getBytes() {
        byte[] result = new byte[length()];
        int resultPos = 0;
        for (byte[] bytes : fullPages) {
            System.arraycopy(bytes, 0, result, resultPos, pageSize);
            resultPos += pageSize;
        }
        System.arraycopy(page, 0, result, resultPos, pos);
        return result;
    }

    /** @return количество добавленных в аккумулятор данных, в байтах */
    public int length() {
        return fullPages.size() * pageSize + pos;
    }

    /** @return размер страницы. */
    public int getPageSize() {
        return pageSize;
    }

    /** @return актуальный чарсет для кодирования строк */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Устанавливает чарсет для кодирования строк.
     *
     * @param charset
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * Преобразует символ в строку и записывает её в аккумулятор.
     *
     * @param character
     * @return this
     */
    public PagingByteAccumulator appendSpell(char character) {
        return append(String.valueOf(character));
    }

    /**
     * Преобразует число в десятичную строку и записывает её в аккумулятор.
     *
     * @param number
     * @return this
     */
    public PagingByteAccumulator appendSpell(int number) {
        return append(String.valueOf(number));
    }

    /**
     * Преобразует число в десятичную строку и записывает её в аккумулятор.
     *
     * @param number
     * @return this
     */
    public PagingByteAccumulator appendSpell(long number) {
        return append(String.valueOf(number));
    }

    /**
     * Преобразует число в простую десятичную строку ({@link BigDecimal#toPlainString()} и записывает её в аккумулятор.
     *
     * @param number
     * @return this
     */
    public PagingByteAccumulator appendSpell(BigDecimal number) {
        return append(number == null ? null : number.toPlainString());
    }

    /**
     * Преобразует число в десятичную строку и записывает её в аккумулятор.
     *
     * @param number
     * @return this
     */
    public PagingByteAccumulator appendSpell(double number) {
        return append(String.valueOf(number));
    }

    /**
     * Преобразует объект в строку и записывает её в аккумулятор.
     *
     * @param object
     * @return this
     */
    public PagingByteAccumulator append(Object object) {
        return append(String.valueOf(object));
    }
}
