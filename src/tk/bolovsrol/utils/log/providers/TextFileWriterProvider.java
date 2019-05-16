package tk.bolovsrol.utils.log.providers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/** Провайдер, записывающий данные в текстовый файл. */
class TextFileWriterProvider extends AbstractFileWriterProvider {

    TextFileWriterProvider(File outFile) {
        super(outFile);
    }

    @Override protected OutputStream newOutputStream() throws IOException {
        return new FileOutputStream(outFile, true);
    }
}

