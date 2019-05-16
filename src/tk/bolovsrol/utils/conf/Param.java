package tk.bolovsrol.utils.conf;

import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.properties.ReadOnlyProperties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/** Поле конфига {@link AutoConfiguration}, значение которого некоторым образом достаётся при загрузке из пропертей. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Param {

    /**
     * Тут можно указать ключ: название поля в попертях. Если тут пустая строка (по умолчанию), то ключом
     * считается название описываемого поля в классе конфигурации.
     * <p>
     * К полученному ключу ещё прилепляют {@link #keyPrefix()}.
     *
     * @return название поля в конфиге
     * @see #keyPrefix()
     */
    String key() default "";

    /**
     * @return префикс, который будет приклеен ключу (см. {@link #key()}), чтобы получить название поля в конфиге
     * @see #key()
     */
    String keyPrefix() default "";

    /** @return true, если поле обязательно должно быть указано, иначе false */
    boolean mandatory() default false;

    /**
     * @return true, если поле инициализируется какой-то третьей силой, и загрузчик трогать его не должен
     * @see AutoConfiguration#afterLoad(LogDome, ReadOnlyProperties, Map, UnusedKeyAction)
     * @deprecated слишком неочевидный ход; следует объявлять текстовое загружаемое поле и компилировать его в {@link AutoConfiguration#afterLoad(LogDome, ReadOnlyProperties, Map, UnusedKeyAction)}.
     */
    @Deprecated boolean custom() default false;

    /**
     * Для загрузки значения из пропертей будет вызыван метод <code>public void parse<em>FieldName</em>(String value)</code>
     * с соответствующим аргументом.
     *
     * @return если true, использовать метод parseFieldName() для загрузки значения из ReadOnlyProperties
     * @see AutoConfiguration#afterLoad(LogDome, ReadOnlyProperties, Map, UnusedKeyAction)
     * @deprecated слишком неочевидный ход; следует объявлять текстовое загружаемое поле и компилировать его в {@link AutoConfiguration#afterLoad(LogDome, ReadOnlyProperties, Map, UnusedKeyAction)}.
     */
    @Deprecated boolean viaParse() default false;

    /**
     * Для экспорта значения в проперти будет вызыван метод <code>public String compile<em>FieldName</em>()</code>,
     * результат его деятельности и будет использован.
     *
     * @return если true, использовать метод compileFieldName() для экспорта значения в ReadOnlyProperties
     * @see AutoConfiguration#afterLoad(LogDome, ReadOnlyProperties, Map, UnusedKeyAction)
     * @deprecated слишком неочевидный ход; следует объявлять текстовое загружаемое поле и компилировать его в {@link AutoConfiguration#afterLoad(LogDome, ReadOnlyProperties, Map, UnusedKeyAction)}.
     */
    @Deprecated boolean viaCompile() default false;

    /**
     * То же, что {@link #viaParse()} и {@link #viaCompile()} одновременно, два в одном.
     *
     * @return если true, использовать методы parseFieldName() и compileFieldName() для загрузки из и экспорта значения в ReadOnlyProperties
     * @see AutoConfiguration#afterLoad(LogDome, ReadOnlyProperties, Map, UnusedKeyAction)
     * @deprecated слишком неочевидный ход; следует объявлять текстовое загружаемое поле и компилировать его в {@link AutoConfiguration#afterLoad(LogDome, ReadOnlyProperties, Map, UnusedKeyAction)}.
     */
    @Deprecated boolean viaParseCompile() default false;

	/**
	 * Шаблон для полей, требующих некоторого шаблона для экспорта и импорта.
	 * <p>
	 * На данный момент этот шаблон требуют поля типа Date, для которых нужно указать паттерн для SimpleDateFormat.
	 *
	 * @return шаблон для экспорта и импорта значения поля
	 */
	String pattern() default "";

    /** @return если true, описываемый параметр не должен быть виден человекам (в toString() и подобных вещах), иначе false */
    boolean hidden() default false;

    /** @return если true, значение описываемого параметра не должно быть видно в toString(), иначе false */
    boolean password() default false;

    /**
     * @return способ преобразования значения для печати/вывода в проперти, если нужно.
     * @see #viaParse()
     */
    ValueTransformation transf() default ValueTransformation.NONE;

    /** @return описание поля для вывода на печать конф-принтером */
    String desc() default "";

    /** @return если true, то значение по умолчанию описываемого параметра будет спрятано (имеет смысл для конф-принтера) */
    boolean hideDefValue() default false;

    /** @return названия дополнительных кючей */
    String[] aka() default {};

    /** @return названия дополнительных кючей, значения которых будут считаны, но в лог будет выдана ругань на устаревший ключ с советом переименовать его */
    String[] deprecatedKeys() default {};

}
