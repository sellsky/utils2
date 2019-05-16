package tk.bolovsrol.utils.conf;

import tk.bolovsrol.utils.textformatter.compiling.InvalidTemplateException;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AutoConfigurationPrinter {
    private final Accumulator accumulator;

    public AutoConfigurationPrinter(Accumulator accumulator) {
        this.accumulator = accumulator;
    }

    public void print(AutoConfiguration conf) throws IllegalAccessException, InvalidTemplateException {
        append(conf, "", new HashSet<>());
    }

    private void append(AutoConfiguration autoConfiguration, String prefix, Set<Field> mentionedFields) throws IllegalAccessException {
        for (Map.Entry<Field, AutoConfiguration> entry : autoConfiguration.confFields().entrySet()) {
            Field f = entry.getKey();
            AutoConfiguration include = entry.getValue();
            if (mentionedFields.add(f)) {
                Param p = f.getAnnotation(Param.class);
                if (p != null) {
                    if (!p.hidden()) {
                        String fieldName = prefix + (p.key().isEmpty() ? f.getName() : p.key());
                        Object fieldValue = f.get(autoConfiguration);
                        String printableFieldValue;

                        if (fieldValue == null) {
                            printableFieldValue = "";
                        } else {
                            printableFieldValue = p.transf().transformPrintable(fieldValue);
                        }
                        accumulator.appendField(f, p, fieldName, printableFieldValue);
                    }
                } else {
                    Include i = f.getAnnotation(Include.class);
                    if (!i.doNotPrint()) {
                        append(include, prefix + i.prefix(), mentionedFields);
                    }
                }
            }
        }
    }

    public interface Accumulator {
        void appendField(Field field, Param p, String fieldName, String printableFieldValue);
    }
}
