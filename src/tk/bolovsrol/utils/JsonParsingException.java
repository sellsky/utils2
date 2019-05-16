package tk.bolovsrol.utils;

public class JsonParsingException extends UnexpectedBehaviourException {

    public JsonParsingException(String message, char[] source, int pos) {
        super(message + ": " + StringUtils.truncate(new String(source, pos, Math.min(101, source.length - pos)), 100, "✂"));
    }

}
