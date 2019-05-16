package tk.bolovsrol.utils.log.providers;

import java.io.File;

/** Пул текстовых файлов, упакованных гзипом. */
public class GzipFileProviderPool extends AbstractFileProviderPool {

    @Override protected LogWriterProvider newStreamProvider(File file) {
        return new GzipFileWriterProvider(file);
    }

}
