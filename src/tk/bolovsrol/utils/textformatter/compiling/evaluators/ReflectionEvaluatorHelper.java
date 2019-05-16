package tk.bolovsrol.utils.textformatter.compiling.evaluators;

import tk.bolovsrol.utils.time.Duration;

import java.math.BigDecimal;
import java.util.Date;

class ReflectionEvaluatorHelper {
    private ReflectionEvaluatorHelper() {
    }

    public static String formatValue(Object value, Class<?> type) {
        if (String.class.isAssignableFrom(type)) {
            return (String) value;
        } else if (BigDecimal.class.isAssignableFrom(type)) {
            return ((BigDecimal) value).toPlainString();
        } else if (Duration.class.isAssignableFrom(type)) {
            return String.valueOf(((Duration) value).getMillis());
//        } else if (type.isPrimitive() || Number.class.isAssignableFrom(type) || type.isEnum() || Boolean.class.isAssignableFrom(type)) {
//            return value.toString();
        } else if (Date.class.isAssignableFrom(type)) {
            return String.valueOf(((Date) value).getTime());
        } else {
            return value.toString();
        }
    }
}
