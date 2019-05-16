package tk.bolovsrol.utils.textformatter.compiling;

import tk.bolovsrol.utils.Spell;

/** Ошибка в шаблоне. */
public class InvalidTemplateException extends TextFormatterException {
    public InvalidTemplateException(String message) {
        super(message);
    }

    public InvalidTemplateException(String message, Throwable cause) {
        super(message, cause);
    }

    private static String getForSourcePrefix(String globalSource, int globalPos, String localSource, int localPos) {
        if (globalSource == localSource) {
            return "Error in template near " + Spell.get(globalSource.substring(globalPos + localPos));
        } else {
            return "Error in template fragment " + Spell.get(localSource) + " near " + Spell.get(globalSource.substring(globalPos + localPos));
        }
    }

    public static InvalidTemplateException forSource(String globalSource, int globalPos, String localSource, int localPos, String message) {
        return new InvalidTemplateException(getForSourcePrefix(globalSource, globalPos, localSource, localPos) + ". " + message);
    }

    public static InvalidTemplateException forSource(String globalSource, int globalPos, String localSource, int localPos, Throwable cause) {
        return new InvalidTemplateException(getForSourcePrefix(globalSource, globalPos, localSource, localPos), cause);
    }
}
