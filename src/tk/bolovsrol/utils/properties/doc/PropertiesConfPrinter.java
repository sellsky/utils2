package tk.bolovsrol.utils.properties.doc;

import tk.bolovsrol.utils.conf.Param;

import java.lang.reflect.Field;

/**
 * # desc; mandatory
 * key=value
 */
public class PropertiesConfPrinter extends AbstractConfPrinter {
    public PropertiesConfPrinter() {
        this(RUSSIAN_CAPTIONS);
    }

    public PropertiesConfPrinter(Captions captions) {
        super(captions);
    }

    @Override
    protected void appendField(StringBuilder sb, Field field, Param p, String fieldName, String fieldValue, String fieldDescription) {
        if (sb.length() != 0) {
            sb.append('\n');
        }
        sb.append("# ")
                .append(Character.toUpperCase(fieldDescription.charAt(0)))
                .append(fieldDescription.substring(1))
                .append('\n');
        if (!p.mandatory()) {
            sb.append('#');
        }
        sb.append(fieldName)
                .append('=')
                .append(fieldValue)
                .append('\n');
    }
}
