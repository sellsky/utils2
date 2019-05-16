package tk.bolovsrol.utils.io;

import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.Spell;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Насос, перекачивает информацию из {@link InputStream}:а в {@link OutputStream} через внутренний буфер.
 * <p/>
 * Насос представляет собой тред, после создания и настройки
 * его нужно запустить методом {@link #start()}.
 * Насос автоматически сдохнет, когда входящий поток вернёт EOF.
 * <p/>
 * Насос может закрыть один или оба потока после копирования.
 * По умолчанию — оставит их открытыми.
 * <p/>
 * Также насос может выключиться в случае, если при операциях с потоками вывалится {@link IOException}.
 * Это исключение можно получить при помощи {@link #getIoException()}.
 */
public class InputStreamToOutputStreamPump extends Thread {

    public static final int DEFAULT_BUFFER_SIZE = 8192;

    private final InputStream inputStream;
    private final OutputStream[] outputStreams;
    private int bufferSize;
    private boolean autocloseInputStream = false;
    private boolean autocloseOutputStreams = false;
    private IOException ioException;
    private int counter = 0;
    private boolean finished = false;

    public InputStreamToOutputStreamPump(InputStream inputStream, OutputStream... outputStreams) {
        this(inputStream, DEFAULT_BUFFER_SIZE, outputStreams);
    }

    public InputStreamToOutputStreamPump(InputStream inputStream, int bufferSize, OutputStream... outputStreams) {
        this("StreamPump-" + inputStream.toString(), inputStream, bufferSize, outputStreams);
    }

    public InputStreamToOutputStreamPump(String name, InputStream inputStream, OutputStream... outputStreams) {
        this(name, inputStream, DEFAULT_BUFFER_SIZE, outputStreams);
    }

    public InputStreamToOutputStreamPump(String name, InputStream inputStream, int bufferSize, OutputStream... outputStreams) {
        super(name);
        this.inputStream = inputStream;
        this.outputStreams = outputStreams;
        this.bufferSize = bufferSize;
    }

    @Override
    public void run() {
        try {
            byte[] buf = new byte[bufferSize];
            while (!isInterrupted()) {
                int read = inputStream.read(buf);
                if (read < 0) {
                    break;
                }
                for (OutputStream outputStream : outputStreams) {
                    outputStream.write(buf, 0, read);
                    outputStream.flush();
                }
                counter += read;
            }
        } catch (IOException e) {
            Log.exception(e);
            ioException = e;
        } finally {
            if (autocloseInputStream) {
                closeQuietly(inputStream);
            }
            if (autocloseOutputStreams) {
                for (OutputStream outputStream : outputStreams) {
                    closeQuietly(outputStream);
                }
            }
            finished = true;
        }
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            Log.trace(e);
            // void
        }
    }

    public IOException getIoException() {
        return ioException;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public InputStreamToOutputStreamPump setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public boolean isAutocloseInputStream() {
        return autocloseInputStream;
    }

    public InputStreamToOutputStreamPump setAutocloseInputStream(boolean autocloseInputStream) {
        this.autocloseInputStream = autocloseInputStream;
        return this;
    }

    public boolean isAutocloseOutputStreams() {
        return autocloseOutputStreams;
    }

    public InputStreamToOutputStreamPump setAutocloseOutputStreams(boolean autocloseOutputStreams) {
        this.autocloseOutputStreams = autocloseOutputStreams;
        return this;
    }

    public boolean isFinished() {
        return finished;
    }

    public int getTraffic() {
        return counter;
    }

    public static class StatThread extends Thread {
        private final InputStreamToOutputStreamPump pump;

        public StatThread(InputStreamToOutputStreamPump pump) {
            super("Stat-" + pump.getName());
            this.pump = pump;
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    if (pump.isFinished()) {
                        if (pump.getIoException() != null) {
                            Log.trace("Pump died: " + Spell.get(pump.getIoException()));
                        }
                        return;
                    }
                    if (pump.isAlive()) {
                        pump.join(5000L);
                    } else {
                        Thread.sleep(5000L);
                    }
                    Log.trace("Pump " + pump.getName() + " traffic=" + pump.getTraffic());
                }
            } catch (InterruptedException e) {
            } finally {
                Log.trace("Pumpstat " + getName() + " is over, pump traffic=" + pump.getTraffic() + ", isOver=" + pump.isFinished() + ", ioException=" + pump.getIoException());
            }
        }
    }
}