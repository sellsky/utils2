package tk.bolovsrol.utils.properties;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.UnexpectedBehaviourException;

public class SystemResourcePropertiesLoadingException extends UnexpectedBehaviourException {
    public SystemResourcePropertiesLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public static SystemResourcePropertiesLoadingException getForResourceName(String resourceName, Throwable cause) {
        return new SystemResourcePropertiesLoadingException("Error reading internal properties " + Spell.get(resourceName), cause);
    }
}
