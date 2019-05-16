package tk.bolovsrol.utils;

public class JsonValueOfOtherTypeException extends RuntimeException {
    private final Json json;

    public JsonValueOfOtherTypeException(Json json) {
        super(Spell.get(json));
        this.json = json;
    }

    public JsonValueOfOtherTypeException(Json json, Throwable cause) {
        super(Spell.get(json), cause);
        this.json = json;
    }

    public Json getJson() {
        return json;
    }
}
