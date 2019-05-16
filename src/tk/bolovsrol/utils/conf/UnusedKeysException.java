package tk.bolovsrol.utils.conf;

import tk.bolovsrol.utils.StringUtils;

import java.util.Set;

public class UnusedKeysException extends InvalidConfigurationException {

    private final Set<String> unusedKeys;

    public UnusedKeysException(String message, Set<String> unusedKeys) {
        super(message);
        this.unusedKeys = unusedKeys;
    }

    public Set<String> getUnusedKeys() {
        return unusedKeys;
    }

    public static UnusedKeysException forUnusedKeys(Set<String> unusedKeys) {
        return new UnusedKeysException(enumerateUnusedKeys(unusedKeys), unusedKeys);
    }

    public static String enumerateUnusedKeys(Set<String> unusedKeys) {
        return (unusedKeys.size() == 1 ?
              "key '" + unusedKeys.iterator().next() :
              "keys '" + StringUtils.enlistCollection(unusedKeys, "', '", "' and '"))
              + "' not used";
    }

}
