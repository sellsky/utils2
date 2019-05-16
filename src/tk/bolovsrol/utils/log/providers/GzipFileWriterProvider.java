package tk.bolovsrol.utils.log.providers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/** Провайдер, записывающий данные в файл в гзип-формате. */
class GzipFileWriterProvider extends AbstractFileWriterProvider {

    GzipFileWriterProvider(File outFile) {
        super(outFile);
    }

    @Override protected OutputStream newOutputStream() throws IOException {
        return new GZIPOutputStream(new FileOutputStream(outFile, true));
    }
}

