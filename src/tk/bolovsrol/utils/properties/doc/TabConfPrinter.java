package tk.bolovsrol.utils.properties.doc;

import tk.bolovsrol.utils.conf.Param;

import java.lang.reflect.Field;

/**
 * key\t\tx\tdef\tdesc
 * key=value
 */
public class TabConfPrinter extends AbstractConfPrinter {
    public TabConfPrinter() {
        this(RUSSIAN_CAPTIONS);
    }

    public TabConfPrinter(Captions captions) {
        super(captions);
    }

    @Override
    protected void appendField(StringBuilder sb, Field field, Param p, String fieldName, String fieldValue, String fieldDescription) {
        if (sb.length() != 0) {
            sb.append('\n');
        }
        sb.append(fieldName);
        sb.append('\t');
        sb.append(field.getType().getSimpleName().toLowerCase());
        sb.append('\t');
        if (p.mandatory()) {
            sb.append('x');
        }
        sb.append('\t');
        if (!p.mandatory() && fieldValue != null) {
            if (!fieldValue.isEmpty()) {
                try {
                    sb.append(Integer.parseInt(fieldValue));
                } catch (NumberFormatException ignored) {
                    sb.append('"').append(fieldValue).append('"');
                }
            } else {
                sb.append("null");
            }
        }
        sb.append('\t');
        sb.append(p.desc());
    }
}
