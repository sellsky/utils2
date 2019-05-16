package tk.bolovsrol.utils.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Писатель-фильтр, который пишет в потоки указанное количество
 * информации (или меньше), после чего требует поменять поток.
 * <p/>
 * Места потока, в которых его можно резать, нужно отмечать
 * методом checkPoint(). Соответственно, не нужно использовать
 * буфер над этим объектом, иначе checkPoint() может
 * отметить поток в произвольном месте.
 */
public class SectionOutputStream extends OutputStream {

    public interface StreamProvider {
        /**
         * Отдаёт новый поток, в который нужно писать
         * следующую секцию.
         *
         * @return writer
         */
        public OutputStream nextStream() throws IOException;

        /**
         * Работа с потоком завершена. Его надо финализировать как-то,
         * как минимум, закрыть.
         *
         * @param stream
         */
        public void streamIsDone(OutputStream stream) throws IOException;

        /**
         * Возвращает заголовок секции.
         *
         * @return заголовок секции
         */
        public byte[] getSectionHeader();

        /**
         * Возвращает футер секции.
         *
         * @return футер секции
         */
        public byte[] getSectionFooter();
    }

    // провайдер, который даёт нам райтеры и сопутствующие услуги
    private final StreamProvider streamProvider;
    // ограничение размера сеции
    private int limit;

    // актуальный райтер
    private OutputStream currentStream = null;
    // сколько ещё можно записать, не уперевшись в лимит
    private int bytesLeft;
    // футер для записи после выборки лимита
    private byte[] footer = null;

    // буфер накапливает данные между чекпоинтами
    private ByteArrayOutputStream buffer;

    public SectionOutputStream(StreamProvider streamProvider, int limit) {
        this.streamProvider = streamProvider;
        this.limit = limit;
        this.buffer = new ByteArrayOutputStream(limit);
    }

    /**
     * Закрываем старый поток (если он был) и создаём новый.
     *
     * @throws IOException
     */
    private void newStream() throws IOException {
        finishStream();

        currentStream = streamProvider.nextStream();
        bytesLeft = limit;
        byte[] header = streamProvider.getSectionHeader();
        if (header != null) {
            currentStream.write(header);
            bytesLeft -= header.length;
        }
        footer = streamProvider.getSectionFooter();
        if (footer != null) {
            bytesLeft -= footer.length;
        }
    }

    private void finishStream() throws IOException {
        if (currentStream != null) {
            if (footer != null) {
                currentStream.write(footer);
            }
            streamProvider.streamIsDone(currentStream);
        }
    }

    /**
     * Сохраняем занесённые в буфер изменения в секцию.
     * Однако, если размер буфера превышает оставшийся лимит байтов,
     * то мы поменяем поток на более новый.
     */
    public void checkPoint() throws IOException {
        if (buffer.size() > 0) {
            if (currentStream == null || buffer.size() > bytesLeft) {
                newStream();
            }
            buffer.writeTo(currentStream);
            bytesLeft -= buffer.size();
            buffer.reset();
        }
    }

    public void write(byte b[], int off, int len) throws IOException {
        buffer.write(b, off, len);
    }

    public void write(byte b[]) throws IOException {
        buffer.write(b);
    }

    public void flush() throws IOException {
        checkPoint();
    }

    public void close() throws IOException {
        flush();
        finishStream();
    }

    public void write(int b) throws IOException {
        buffer.write(b);
    }

//    //------------ test ------------
//    public static StreamProvider testSp = new StreamProvider() {
//        int count = 0;
//        public OutputStream nextStream() throws IOException {
//            return new BufferedOutputStream(new FileOutputStream("W:/test-" + (++count) + ".txt"));
//        }
//
//        public void streamIsDone(OutputStream stream) throws IOException {
//            stream.close();
//        }
//
//        public byte[] getSectionHeader() {
//            try {
//                return ("[-- section " + count + "[[\n").getBytes("cp1251");
//            } catch (UnsupportedEncodingException e) {
//                return null;
//            }
//        }
//
//        public byte[] getSectionFooter() {
//            try {
//                return ("]] section " + count + "--]").getBytes("cp1251");
//            } catch (UnsupportedEncodingException e) {
//                return null;
//            }
//        }
//    };
//
//    public static void main(String[] args) throws Exception {
//        SectionOutputStream sectioner = new SectionOutputStream(testSp, 256);
//
//        for(int i = 0; i <= 19; i++) {
//            sectioner.write(("Ехал грека через реку видит грека в реке рак номер " + i + "!\n").getBytes("cp1251"));
//            sectioner.checkPoint();
//        }
//
//        sectioner.close();
//    }
}
