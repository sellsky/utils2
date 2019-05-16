package tk.bolovsrol.utils.conf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Конфигурация, вложенная в конфигурацию. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Include {

    /** @return подветка пропертей для конфига */
    String prefix() default "";

    /** @return если true, описываемый параметр не должен быть виден человекам (в toString() и подобных вещах), иначе false */
    boolean doNotPrint() default false;

}
