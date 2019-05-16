package tk.bolovsrol.utils.properties.files;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.UnexpectedBehaviourException;

import java.io.File;

/** Ошибка разбора файла. */
public class ConfFileParsingException extends UnexpectedBehaviourException {
    private ConfFileParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ConfFileParsingException getForFile(File file, Exception e) {
        return new ConfFileParsingException("Error parsing file " + Spell.get(file), e);
    }

    public static ConfFileParsingException getForFile(File file, int lineNumber, String line, Exception e) {
        return new ConfFileParsingException("Error parsing file " + Spell.get(file) + ", line " + lineNumber + ' ' + Spell.get(line), e);
    }

    public static ConfFileParsingException getForTemplate(String name, Exception e) {
        return new ConfFileParsingException("Error parsing template " + Spell.get(name), e);
    }

    public static ConfFileParsingException getForTemplate(String name, int lineNumber, String line, Exception e) {
        return new ConfFileParsingException("Error parsing template " + Spell.get(name) + ", line " + lineNumber + ' ' + Spell.get(line), e);
    }
}
