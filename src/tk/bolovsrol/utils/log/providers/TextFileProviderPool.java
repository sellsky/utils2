package tk.bolovsrol.utils.log.providers;

import java.io.File;

/** Пул обычных текстовых файлов. */
public class TextFileProviderPool extends AbstractFileProviderPool {

    @Override protected LogWriterProvider newStreamProvider(File file) {
        return new TextFileWriterProvider(file);
    }

}
