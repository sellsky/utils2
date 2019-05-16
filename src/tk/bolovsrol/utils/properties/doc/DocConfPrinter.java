package tk.bolovsrol.utils.properties.doc;

import tk.bolovsrol.utils.conf.Param;

import java.lang.reflect.Field;

/** key — desc */
public class DocConfPrinter extends AbstractConfPrinter {

    public DocConfPrinter() {
        this(RUSSIAN_CAPTIONS);
    }

    public DocConfPrinter(Captions captions) {
        super(captions);
    }

    @Override
    protected void appendField(StringBuilder sb, Field field, Param p, String fieldName, String fieldValue, String fieldDescription) {
        if (sb.length() > 0) {
            sb.setCharAt(sb.length() - 1, ';');
            sb.append('\n');
        }
        sb.append(fieldName);
        for (String aka : p.aka()) {
            sb.append(", ").append(aka);
        }
        sb.append(" — ").append(fieldDescription).append('.');
    }
}
