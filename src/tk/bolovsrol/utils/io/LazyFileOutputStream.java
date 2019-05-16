package tk.bolovsrol.utils.io;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/** Работает как FileOutputStream, но не создаёт файл до попытки записи в него. */
public class LazyFileOutputStream extends OutputStream {

    private final FileOutputStreamCreator fileOutputStreamCreator;
    private FileOutputStream fileOutputStream;

    private interface FileOutputStreamCreator {
        FileOutputStream create() throws FileNotFoundException;
    }

    /**
     * Creates an output file stream to write to the file with the
     * specified name. A new <code>FileDescriptor</code> object is
     * created to represent this file connection.
     * <p/>
     * First, if there is a security manager, its <code>checkWrite</code>
     * method is called with <code>name</code> as its argument.
     * <p/>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     *
     * @param name the system-dependent filename
     */
    public LazyFileOutputStream(String name) {
        this.fileOutputStreamCreator = () -> new FileOutputStream(name);
    }

    /**
     * Creates an output file stream to write to the file with the specified
     * <code>name</code>.  If the second argument is <code>true</code>, then
     * bytes will be written to the end of the file rather than the beginning.
     * A new <code>FileDescriptor</code> object is created to represent this
     * file connection.
     * <p/>
     * First, if there is a security manager, its <code>checkWrite</code>
     * method is called with <code>name</code> as its argument.
     * <p/>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     *
     * @param name   the system-dependent file name
     * @param append if <code>true</code>, then bytes will be written
     *               to the end of the file rather than the beginning
     */
    public LazyFileOutputStream(String name, boolean append) {
        this.fileOutputStreamCreator = () -> new FileOutputStream(name, append);
    }

    /**
     * Creates a file output stream to write to the file represented by
     * the specified <code>File</code> object. A new
     * <code>FileDescriptor</code> object is created to represent this
     * file connection.
     * <p/>
     * First, if there is a security manager, its <code>checkWrite</code>
     * method is called with the path represented by the <code>file</code>
     * argument as its argument.
     * <p/>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     *
     * @param file the file to be opened for writing.
     * @see File#getPath()
     */
    public LazyFileOutputStream(File file) {
        this.fileOutputStreamCreator = () -> new FileOutputStream(file);
    }

    /**
     * Creates a file output stream to write to the file represented by
     * the specified <code>File</code> object. If the second argument is
     * <code>true</code>, then bytes will be written to the end of the file
     * rather than the beginning. A new <code>FileDescriptor</code> object is
     * created to represent this file connection.
     * <p/>
     * First, if there is a security manager, its <code>checkWrite</code>
     * method is called with the path represented by the <code>file</code>
     * argument as its argument.
     * <p/>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     *
     * @param file   the file to be opened for writing.
     * @param append if <code>true</code>, then bytes will be written
     *               to the end of the file rather than the beginning
     * @see File#getPath()
     * @since 1.4
     */
    public LazyFileOutputStream(File file, boolean append) {
        this.fileOutputStreamCreator = () -> new FileOutputStream(file, append);
    }

    /**
     * Creates an output file stream to write to the specified file
     * descriptor, which represents an existing connection to an actual
     * file in the file system.
     * <p/>
     * First, if there is a security manager, its <code>checkWrite</code>
     * method is called with the file descriptor <code>fdObj</code>
     * argument as its argument.
     *
     * @param fdObj the file descriptor to be opened for writing
     */
    public LazyFileOutputStream(FileDescriptor fdObj) {
        this.fileOutputStreamCreator = () -> new FileOutputStream(fdObj);
    }

    private void ensureOpen() throws FileNotFoundException {
        if (fileOutputStream == null) {
            fileOutputStream = fileOutputStreamCreator.create();
        }
    }

    @Override
    public void write(int b) throws IOException {
        ensureOpen();
        fileOutputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        ensureOpen();
        fileOutputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        fileOutputStream.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        if (fileOutputStream != null) {
            fileOutputStream.close();
            fileOutputStream = null;
        }
    }

    @Override
    public void flush() throws IOException {
        if (fileOutputStream != null) {
            fileOutputStream.flush();
        }
    }

    public FileDescriptor getFD() throws IOException {
        return fileOutputStream == null ? null : fileOutputStream.getFD();
    }

    public FileChannel getChannel() {
        return fileOutputStream == null ? null : fileOutputStream.getChannel();
    }
}
