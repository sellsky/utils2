package tk.bolovsrol.utils.stringserializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Содержит список прежних имён поля.
 * Если {@link StringDeserializer десериализатор} не нашёл поле в классе по имени,
 * он ищет прежнее название поля в этой аннотации.
 * <p/>
 * Механизм позволяет переименовывать поля, сохраняя совместимость
 * с прежде сделанными сериализациями.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PreviousNames {
    String[] value() default {};
}
