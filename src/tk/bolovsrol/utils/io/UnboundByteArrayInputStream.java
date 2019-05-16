package tk.bolovsrol.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Поток, который можно питать байтами, массивами байт
 * и другими входящими потоками в процессе его существования.
 * <p/>
 * Поток не ограничивает размер внутреннего буфера
 * и не контролирует переполнение памяти.
 * <p/>
 * Память для хранения данных, ожидающих чтения,
 * выделяется страницами одинаковой длины,
 * по умолчанию 64 кб.
 * <p/>
 * Метод {@link #available()} всегда возвращает количество находящихся
 * в буфере потока данных, а методы {@link #read(byte[], int, int)} и {@link #read(byte[])}
 * всегда отдают максимально возможное количество данных.
 */
public class UnboundByteArrayInputStream extends InputStream {

    public static final int DEFAULT_PAGE_SIZE = 65536;

    /** Размер страницы. */
    private final int pageSize;
    /** Синхронизатор ожидания появления хоть каких-то данных. */
    private final Object dataLock = new Object();
    /** Промежуточные страницы. */
    private final Queue<byte[]> pages = new LinkedList<byte[]>();
    /** Страница для чтения. */
    private byte[] readPage;
    /** Позиция чтения на странице. */
    private int readPos = 0;
    /** Страница для записи. */
    private byte[] writePage;
    /** Позиция записи на странице. */
    private int writePos = 0;
    /**
     * Конец потока. Установленный флажок значит, что новых данных в поток добавить нельзя,
     * а после опрожнения буфера попытка чтения из потока вернёт EOF.
     */
    private boolean eos = false;
    /** Болванка потока, которым можно пополнять буфер. */
    private OutputStream outputStreamStub;

    /** Создаёт поток с размером страницы по умолчанию {@link #DEFAULT_PAGE_SIZE}. */
    public UnboundByteArrayInputStream() {
        this(DEFAULT_PAGE_SIZE);
    }

    /**
     * Создаёт поток с указанным размером страницы.
     *
     * @param pageSize желаемый размер страницы.
     */
    public UnboundByteArrayInputStream(int pageSize) {
        this.pageSize = pageSize;
        this.readPage = new byte[pageSize];
        this.writePage = readPage;
    }

    // read (interface) suite -------------------------------------------------------------------------------
    @Override public int available() throws IOException {
        return getAvailableSize();
    }

    @Override public int read(byte buf[], int off, int len) throws IOException {
        int unreadSize;
        while (true) {
            unreadSize = getAvailableSize();
            if (unreadSize > 0) {
                break;
            }
            if (eos) {
                return -1;
            }
            waitForAnyDataAvailable();
        }
        int totalRead = 0;
        while (len > 0) {
            checkReadPage();
            int count = Math.min(len, (readPage == writePage ? writePos : pageSize) - readPos);
            if (count <= 0) {
                break;
            }
            System.arraycopy(readPage, readPos, buf, off, count);
            readPos += count;
            totalRead += count;
            off += count;
            len -= count;
        }
        return totalRead;
    }

    public int read() throws IOException {
        while (getAvailableSize() == 0) {
            if (eos) {
                return -1;
            }
            waitForAnyDataAvailable();
        }
        checkReadPage();
        return ((int) readPage[readPos++]) & 0xff;
    }

    /** Проверяем, что из текущей страницы можно читать, а если нельзя, находим следующую страницу. */
    private void checkReadPage() {
        if (readPos == pageSize && readPage != writePage) {
            synchronized (pages) {
                if (pages.isEmpty()) {
                    readPage = writePage;
                } else {
                    readPage = pages.remove();
                }
                readPos = 0;
            }
        }
    }

    /** Ждём, что что-нибудь запишут. */
    private void waitForAnyDataAvailable() {
        try {
            synchronized (dataLock) {
                dataLock.wait();
            }
        } catch (InterruptedException e) {
            new IOException(e);
        }
    }

    /** @return объём данных буфера */
    private int getAvailableSize() {
        if (readPage == writePage) {
            // читаем и пишем в пределах одной страницы
            int unreadCount = writePos - readPos;
            return unreadCount > 0 ? unreadCount : 0;
        } else {
            // страницы разные, значит читать есть хоть чего-то
            return pageSize - readPos + writePos + pages.size() * pageSize;
        }
    }

    public void eos() {
        eos = true;
    }

    // write suite ------------------------------------------------------------------------------------------------------------------------
    /**
     * Выкладывает в поток один байт.
     *
     * @param b байт
     */
    public void write(int b) {
        checkWritePage();
        writePage[writePos++] = (byte) b;
        notifyReader();
    }

    /**
     * Выкладывает в поток содержимое массива.
     *
     * @param buf
     */
    public void write(byte[] buf) {
        write(buf, 0, buf.length);
    }

    /**
     * Выкладывает в поток содержимое указанного фрагмента массива.
     *
     * @param buf
     * @param off
     * @param len
     */
    public void write(byte[] buf, int off, int len) {
        while (len > 0) {
            checkWritePage();
            int toCopy = Math.min(len, pageSize - writePos);
            System.arraycopy(buf, off, writePage, writePos, toCopy);
            writePos += toCopy;
            off += toCopy;
            len -= toCopy;
            notifyReader();
        }
    }

    /**
     * Читает входящий поток до самого конца.
     *
     * @param is входящий поток
     * @return количество прочитанных из потока байтов
     * @throws IOException
     */
    public long write(InputStream is) throws IOException {
        return write(is, Long.MAX_VALUE);
    }

    /**
     * Читает входящий поток до самого конца или до указанного количества считанных байтов,
     * что случится раньше.
     *
     * @param is  входящий поток
     * @param len максимальное количество байтов, которые можно считать
     * @return количество прочитанных из потока байтов
     * @throws IOException
     */
    public long write(InputStream is, long len) throws IOException {
        long totalRead = 0;
        while (len > 0) {
            checkWritePage();
            long toRead = Math.min(len, pageSize - writePos);
            int read = is.read(writePage, writePos, (int) toRead);
            if (read < 0) {
                break;
            }
            writePos += read;
            totalRead += read;
            len -= read;
            notifyReader();
        }
        return totalRead;
    }

    /** Проверяет, что писать есть куда. */
    private void checkWritePage() {
        if (eos) {
            throw new IllegalStateException("Stream closed.");
        }
        if (writePos == readPos && readPage == writePage) {
            // используем один и тот же буфер, который сейчас пуст.
            writePos = 0;
            readPos = 0;
        }
        if (writePos == pageSize) {
            synchronized (pages) {
                if (readPage != writePage) {
                    pages.add(writePage);
                }
                writePage = new byte[pageSize];
                writePos = 0;
            }
        }
    }

    /**
     * Уведомляет ожидающего читателя, что в поток, вероятно,
     * выложены какие-нибудь данные.
     */
    private void notifyReader() {
        synchronized (dataLock) {
            dataLock.notifyAll();
        }
    }

    /**
     * Некоторым удобней писать в {@link OutputStream}.
     * Мы можем предоставить им такой интерфейс.
     * <p/>
     * Запись в «поток», который возвращает этот метод,
     * равносильна обычному вызову методов пополнения буфера.
     * <p/>
     * Методы {@link java.io.OutputStream#flush()} и {@link java.io.OutputStream#close()}
     * никакого эффекта не имеют.
     *
     * @return исходящий поток, связанный с буфером
     */
    public OutputStream getOutputStreamStub() {
        if (outputStreamStub == null) {
            outputStreamStub = new OutputStream() {
                @Override public void write(int b) throws IOException {
                    UnboundByteArrayInputStream.this.write(b);
                }

                @Override public void write(byte b[]) throws IOException {
                    UnboundByteArrayInputStream.this.write(b);
                }

                @Override public void write(byte b[], int off, int len) throws IOException {
                    UnboundByteArrayInputStream.this.write(b, off, len);
                }
            };
        }
        return outputStreamStub;
    }
}
