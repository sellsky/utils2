package tk.bolovsrol.utils.properties;

import tk.bolovsrol.utils.Spell;

public class PropertyNotFoundException extends PropertyException {
    public PropertyNotFoundException(String propertyName) {
        super(propertyName, "Property " + Spell.get(propertyName) + " not specified.");
    }
}

