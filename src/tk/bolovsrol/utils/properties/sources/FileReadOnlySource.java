package tk.bolovsrol.utils.properties.sources;

import tk.bolovsrol.utils.properties.files.ConfFileParser;
import tk.bolovsrol.utils.properties.files.ConfFileParsingException;

import java.io.File;

/**
 * Читатель пропертей из текстового файлика.
 *
 * @see ConfFileParser
 */
public class FileReadOnlySource extends IdentityMapReadOnlySource {

    public FileReadOnlySource(String fileName) throws ConfFileParsingException {
        this(new File(fileName));
    }

    public FileReadOnlySource(File file) throws ConfFileParsingException {
        super(new ConfFileParser().parse(file));
    }

}
