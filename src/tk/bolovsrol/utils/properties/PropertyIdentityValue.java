package tk.bolovsrol.utils.properties;

import tk.bolovsrol.utils.StringDumpBuilder;

/**
 * Класс для хранения значения и идентити свойства.
 */
public class PropertyIdentityValue {
    private final String value;
    private final String identity;

    public PropertyIdentityValue(String value, String identity) {
        this.value = value;
        this.identity = identity;
    }

    public String getValue(){
        return value;
    }

    public String getIdentity(){
        return identity;
    }

    @Override
    public String toString() {
        return new StringDumpBuilder()
                .append("value", value)
                .append("identity", identity)
                .toString();
    }
}
