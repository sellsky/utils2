package tk.bolovsrol.utils.properties.doc;

import tk.bolovsrol.utils.conf.AutoConfiguration;
import tk.bolovsrol.utils.conf.Include;
import tk.bolovsrol.utils.conf.Param;
import tk.bolovsrol.utils.textformatter.compiling.CompiledFormatter;
import tk.bolovsrol.utils.textformatter.compiling.InvalidTemplateException;
import tk.bolovsrol.utils.textformatter.compiling.ProxyingCompiledFormatter;
import tk.bolovsrol.utils.textformatter.compiling.evaluators.SingletonEvaluator;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractConfPrinter {

    private CompiledFormatter numericFormatter;
    private CompiledFormatter stringFormatter;

    public static class Captions {
        public final String mandatoryParameter;
        public final String defaultNumericValueTpl;
        public final String defaultStringValueTpl;
        public final String undocumentedParameter;


        public Captions(String mandatoryParameter, String defaultNumericValueTpl, String defaultStringValueTpl, String undocumentedParameter) {
            this.mandatoryParameter = mandatoryParameter;
            this.defaultNumericValueTpl = defaultNumericValueTpl;
            this.defaultStringValueTpl = defaultStringValueTpl;
            this.undocumentedParameter = undocumentedParameter;
        }
    }

    public static final Captions RUSSIAN_CAPTIONS = new Captions(
          " (обязательный параметр)",
          ", по умолчанию {value}",
          ", по умолчанию «{value}»",
          "очевидно"
    );

    public static final Captions ENGLISH_CAPTIONS = new Captions(
          " (mandatory parameter)",
          ", default {value}",
          ", default '{value}'",
          "obvious"
    );

    protected final Captions captions;

    protected AbstractConfPrinter(Captions captions) {
        this.captions = captions;
    }

    public String print(AutoConfiguration conf) throws IllegalAccessException, InvalidTemplateException {
        numericFormatter = ProxyingCompiledFormatter.create(captions.defaultNumericValueTpl);
        stringFormatter = ProxyingCompiledFormatter.create(captions.defaultStringValueTpl);
        StringBuilder sb = new StringBuilder(2048);
        append(sb, conf, "", new HashSet<Field>());
        return sb.toString();
    }

    private void append(StringBuilder sb, AutoConfiguration autoConfiguration, String prefix, Set<Field> mentionedFields) throws IllegalAccessException {
        StringBuilder descSb = new StringBuilder(256);
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
                        descSb.setLength(0);
                        if (p.desc().isEmpty()) {
                            descSb.append(captions.undocumentedParameter);
                        } else {
                            descSb.append(p.desc());
                        }

                        if (fieldValue == null) {
                            printableFieldValue = "";
                            if (p.mandatory()) {
                                descSb.append(captions.mandatoryParameter);
                            }
                        } else {
                            printableFieldValue = p.transf().transformPrintable(fieldValue);
                            if (!p.hideDefValue()) {
                                CompiledFormatter formatter;
                                try {
                                    //noinspection ResultOfMethodCallIgnored
                                    Integer.parseInt(printableFieldValue);
                                    formatter = numericFormatter;
                                } catch (Exception ignored) {
                                    formatter = stringFormatter;
                                }
                                descSb.append(formatter.format(new SingletonEvaluator(printableFieldValue)));
                            }
                        }
                        appendField(sb, f, p, fieldName, printableFieldValue, descSb.toString());
                    }
                } else {
                    Include i = f.getAnnotation(Include.class);
                    if (!i.doNotPrint()) {
                        append(sb, include, prefix + i.prefix(), mentionedFields);
                    }
                }
            }
        }
    }

    protected abstract void appendField(StringBuilder sb, Field field, Param p, String fieldName, String fieldValue, String fieldDescription);

}
