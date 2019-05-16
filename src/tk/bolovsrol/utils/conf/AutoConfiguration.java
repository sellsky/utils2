package tk.bolovsrol.utils.conf;

import tk.bolovsrol.utils.BooleanUtils;
import tk.bolovsrol.utils.Flag;
import tk.bolovsrol.utils.HostPort;
import tk.bolovsrol.utils.Ip4Range;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.NumberUtils;
import tk.bolovsrol.utils.RegexUtils;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.Uri;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.mail.MailAddress;
import tk.bolovsrol.utils.properties.ReadOnlyProperties;
import tk.bolovsrol.utils.properties.sources.ReadOnlySource;
import tk.bolovsrol.utils.properties.sources.SourceUnavailableException;
import tk.bolovsrol.utils.textformatter.compiling.CompiledFormatter;
import tk.bolovsrol.utils.textformatter.compiling.TextFormatCompiler;
import tk.bolovsrol.utils.time.Duration;
import tk.bolovsrol.utils.time.TwofacedTime;
import tk.bolovsrol.utils.xml.Element;
import tk.bolovsrol.utils.xml.ElementUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * «Автоматическая» конфигурация.
 * <p>
 * Предполагается, что наследник этого класса состоит из публичных нефинальных полей
 * с аннотацией {@link Param}. Это и есть поля конфигурации. Также наследник может содержать
 * вложенные конфигурации, публичные финальные поля с аннотацией {@link Include}.
 * <p>
 * Самая мякотка — возможность автоматически вычитать конфигурацию
 * из пропертей {@link ReadOnlyProperties} методом {@link #load(LogDome, ReadOnlyProperties)}.
 * <p>
 * И наоборот: конфигурация имплементит {@link ReadOnlySource}.
 * <p>
 * Для сложных полей следует объявить текстовое поле, которое будет загружено, а после загрузки скомпилировать значение этого поля.
 * <p>
 * Загрузка происходит в три этапа.
 * <ol><li>Перед загрузкой конфигурации вызывается метод {@link #loadDefaults(LogDome)},
 * он должен инициализировать поля конфигурации значениями по умолчанию.
 * Так как конструктор не может выкидывать исключения в силу контракта, сложная инициализация полей проводится в этом методе.
 * <li>Собственно загрузка методом {@link #load(LogDome, ReadOnlyProperties)}.
 * <li>После загрузки вызывается метод {@link #afterLoad(LogDome, ReadOnlyProperties, Map, UnusedKeyAction)},
 * в котором нужно инициализировать компилируемые переменные на основании заполя.
 * </ol>
 * Для печати конфига для документации следует создать конфиг и вызвать {@link #loadDefaults(LogDome)}.
 */
public class AutoConfiguration implements Cloneable, ReadOnlySource {

    /** Поля конфигурации: если значение нул, то это поле со значением, иначе это автоконф с инклюдом. */
    private Map<Field, AutoConfiguration> confFields = null;

    public AutoConfiguration() {
    }

    /**
     * Вычитывает значения из переданных пропертей и через рефлекшн насаждает их полям конфигурации.
     * <p>
     * Для каждого поля, аннотированного {@link Param}, читатель ищет параметр в пропертях
     * и преобразует его определённым способом. Если установлен {@link Param#viaParse()} или {@link Param#viaParseCompile()},
     * то для создания значения вызывается метод parseFieldName(String value). Иначе значения разбираются
     * некоторым очевидным образом внутренним механизмом и корректируются в соответствии с указанным
     * {@link Param#transf()}.
     * <p>
     * У каждой вложенной конфигурации, аннотированной {@link Include}, вызывается аналогичный метод.
     * <p>
     * Если поля в пропертях не окажется, значение поля изменено не будет
     * (можно использовать значения по умолчанию).
     * <p>
     * Ключи пропертей, не соответствующие полям конфигурации, никакой роли не играют.
     * <p>
     * Перед загрузкой вызывается {@link #loadDefaults(LogDome)},
     * после загрузки — {@link #afterLoad(LogDome, ReadOnlyProperties, Map, UnusedKeyAction)}, реализации по умолчанию ничего не делают.
     * <p>
     * Не даётся никаких гарантий относительно порядка загрузки полей.
     *
     * @param log
     * @param cfg
     * @see #loadDefaults(LogDome)
     * @see #afterLoad(LogDome, ReadOnlyProperties, Map, UnusedKeyAction)
     * @see #putStringValue(Field, Param, String)
     */
    public final void load(LogDome log, ReadOnlyProperties cfg) throws InvalidConfigurationException {
        load(log, cfg, UnusedKeyAction.IGNORE);
    }

    /**
     * Вычитывает значения из переданных пропертей и через рефлекшн насаждает их полям конфигурации.
     * <p>
     * Для каждого поля, аннотированного {@link Param}, читатель ищет параметр в пропертях
     * и преобразует его определённым способом. Если установлен {@link Param#viaParse()} или {@link Param#viaParseCompile()},
     * то для создания значения вызывается метод parseFieldName(String value). Иначе значения разбираются
     * некоторым очевидным образом внутренним механизмом и корректируются в соответствии с указанным
     * {@link Param#transf()}.
     * <p>
     * У каждой вложенной конфигурации, аннотированной {@link Include}, вызывается аналогичный метод.
     * <p>
     * Если поля в пропертях не окажется, значение поля изменено не будет
     * (можно использовать значения по умолчанию).
     * <p>
     * Перед загрузкой вызывается {@link #loadDefaults(LogDome)},
     * реализация по умолчанию ничего не делает.
     * <p>
     * Ключи пропертей, не соответствующие полям конфигурации, обрабатываются переданным {@link UnusedKeyAction} в методе
     * {@link #afterLoad(LogDome, ReadOnlyProperties, Map, UnusedKeyAction)}, который вызывается после загрузки.
     * <p>
     * Не даётся никаких гарантий относительно порядка загрузки полей.
     *
     * @param log
     * @param cfg
     * @see #loadDefaults(LogDome)
     * @see #afterLoad(LogDome, ReadOnlyProperties, Map, UnusedKeyAction)
     * @see #putStringValue(Field, Param, String)
     */
    public final void load(LogDome log, ReadOnlyProperties cfg, UnusedKeyAction unusedKeyAction) throws InvalidConfigurationException {
        loadDefaults(log);
        Set<String> usedKeys = new HashSet<>();
        loadInternal(log, cfg, usedKeys);
        Map<String, String> unusedKeys = new LinkedHashMap<>(cfg.dump());
        unusedKeys.keySet().removeAll(usedKeys);
        afterLoad(log, cfg, unusedKeys, unusedKeyAction);
    }

    /**
     * Собственно процедура загрузки.
     *
     * @param log
     * @param cfg
     * @param usedKeys
     */
    protected void loadInternal(LogDome log, ReadOnlyProperties cfg, Set<String> usedKeys) throws InvalidConfigurationException {
        for (Map.Entry<Field, AutoConfiguration> entry : confFields().entrySet()) {
            Field f = entry.getKey();
            Param c = f.getAnnotation(Param.class);
            if (c != null) {
                loadLocalField(log, cfg, f, c, usedKeys);
            } else {
                // либо парам, либо инклюд
                processIncludeField(log, cfg, f, entry.getValue(), usedKeys);
            }
        }
    }

    /**
     * Метод вызывается перед загрузкой {@link #load(tk.bolovsrol.utils.log.LogDome, tk.bolovsrol.utils.properties.ReadOnlyProperties)}.
     * Он должен загрузить значениями по умолчанию те поля, которые нельзя инициализировать без контекста
     * либо которые в процессе инициализации могут выкинуть исключение, которое следует обработать.
     * Так как конструктор не может выкидывать исключения в силу контракта, инициализация проводится этим методом.
     * <p>
     * Реализация по умолчанию ничего не делает.
     *
     * @param log
     * @see #load(tk.bolovsrol.utils.log.LogDome, tk.bolovsrol.utils.properties.ReadOnlyProperties)
     */
    public void loadDefaults(LogDome log) throws InvalidConfigurationException {
        // noop
    }

    /**
     * Метод вызывается после загрузки {@link #load(tk.bolovsrol.utils.log.LogDome, tk.bolovsrol.utils.properties.ReadOnlyProperties)}.
     * Он должен загрузить значениями вычисляемые на основании загруженной конфигурации поля.
     * <p>
     * Метод должен удалить из карты <code>unusedKeys</code> все используемые им ключи.
     * <p>
     * Реализация по умолчанию вызывает {@link UnusedKeyAction#processUnusedKeys(LogDome, AutoConfiguration, Map)},
     * если карта unusedKeys не пуста.
     *
     * @param log
     * @param cfg
     * @param unusedKeys
     * @param unusedKeyAction
     * @see #load(tk.bolovsrol.utils.log.LogDome, tk.bolovsrol.utils.properties.ReadOnlyProperties)
     */
    public void afterLoad(LogDome log, ReadOnlyProperties cfg, Map<String, String> unusedKeys, UnusedKeyAction unusedKeyAction) throws InvalidConfigurationException {
        if (unusedKeyAction != null && !unusedKeys.isEmpty()) {
            unusedKeyAction.processUnusedKeys(log, this, unusedKeys);
        }
    }

    /**
     * Загружает значение в локальное поле, учитывая требования аннотации.
     *
     * @param log
     * @param cfg
     * @param f
     * @param c
     * @param usedKeys
     * @throws MandatoryFieldMissingException
     * @throws InvalidFieldValueException
     */
    @SuppressWarnings("deprecation")
    private void loadLocalField(LogDome log, ReadOnlyProperties cfg, Field f, Param c, Set<String> usedKeys) throws MandatoryFieldMissingException, InvalidFieldValueException {
        String key = getParamName(f, c);

//
//        if (c.deprecatedKeys().length > 0) {
//            usedKeys.addAll(Arrays.asList(c.deprecatedKeys()));
//        }
//        if (c.aka().length > 0) {
//            usedKeys.keySet().removeAll(Arrays.asList(c.aka()));
//        }

        if (c.custom()) {
            return;
        }

        String val = cfg.get(key);
        for (String deprecatedKey : c.deprecatedKeys()) {
            String deprecatedVal = cfg.get(deprecatedKey);
            if (deprecatedVal != null) {
                usedKeys.add(deprecatedKey);
                if (val == null) {
                    log.warning("Key " + Spell.get(cfg.expand(deprecatedKey)) + " is deprecated. Should be renamed to " + Spell.get(cfg.expand(key)));
                    val = deprecatedVal;
                } else {
                    log.warning("Key " + Spell.get(cfg.expand(deprecatedKey)) + " is deprecated and should be removed. Key " + Spell.get(cfg.expand(key)) + " (which is defined) used instead.");
                }
            }
        }
        if (val == null) {
            for (String akaKey : c.aka()) {
                val = cfg.get(akaKey);
                if (val != null) {
                    usedKeys.add(akaKey);
                    break;
                }
            }
        } else {
            usedKeys.add(key);
        }
        if (val == null) {
            if (c.mandatory()) {
                String fieldName = cfg.expand(key);
                throw new MandatoryFieldMissingException("No value for mandatory field " + Spell.get(fieldName) + " is specified", fieldName);
            }
        } else {
            try {
                if (c.viaParse() || c.viaParseCompile()) {
                    Class<?> cl = this.getClass();
                    Method setter = cl.getMethod("parse" + Character.toUpperCase(f.getName().charAt(0)) + f.getName().substring(1), String.class);
                    setter.setAccessible(true);
                    setter.invoke(this, val);
                } else {
                    putStringValue(f, c, c.transf().transformConf(val));
                }
            } catch (Exception e) {
                String fieldName = cfg.expand(key);
                throw new InvalidFieldValueException("Error loading field " + Spell.get(fieldName) + " with value " + Spell.get(val), e, fieldName, val);
            }
        }
    }

    private static String getParamName(Field f, Param c) {
        return c.keyPrefix() + (c.key().isEmpty() ? f.getName() : c.key());
    }

    /**
     * Проставляет полю значение, вычитанное из соответствующего ключа пропертей..
     * <p>
     * В стандартной реализации предусмотрена загрузка примитивов, енумов, строк и
     * нескольких прочих легко стандартизируемых популярных объектов.
     * <p>
     * Ограниченно реализована загрузка массивов.
     * <p>
     * Предполагается, что этот метод будут дорабатывать по мере необходимости
     * или даже оверрайдить.
     *
     * @param field поле, которому надо проставить значение
     * @param param параметры поля
     * @param value текстовое представление значения  @throws Exception
     * @see #load(tk.bolovsrol.utils.log.LogDome, tk.bolovsrol.utils.properties.ReadOnlyProperties)
     */
    @SuppressWarnings("rawtypes")
    protected void putStringValue(Field field, Param param, String value) throws Exception {
        Class<?> fieldClass = field.getType();
        if (fieldClass.isPrimitive()) {
            if (int.class.isAssignableFrom(fieldClass)) {
                field.setInt(this, Integer.decode(value).intValue());
            } else if (long.class.isAssignableFrom(fieldClass)) {
                field.setLong(this, Long.decode(value).longValue());
            } else if (char.class.isAssignableFrom(fieldClass)) {
                field.setChar(this, value.charAt(0));
            } else if (byte.class.isAssignableFrom(fieldClass)) {
                field.setByte(this, Byte.decode(value).byteValue());
            } else if (boolean.class.isAssignableFrom(fieldClass)) {
                field.setBoolean(this, BooleanUtils.parse(value).booleanValue());
            } else if (float.class.isAssignableFrom(fieldClass)) {
                field.setFloat(this, Float.parseFloat(value));
            } else if (double.class.isAssignableFrom(fieldClass)) {
                field.setDouble(this, Double.parseDouble(value));
            } else if (short.class.isAssignableFrom(fieldClass)) {
                field.setShort(this, Short.parseShort(value));
            } else {
                throw new UnsupportedOperationException("Don't know how to deserialize privitive " + Spell.get(field));
            }
        } else if (fieldClass.isEnum()) {
//            field.set(target, Enum.valueOf((Class<? extends Enum<?>>) fieldClass, value));
            //noinspection unchecked
            field.set(this, Enum.valueOf((Class) fieldClass, value.toUpperCase()));
        } else if (String.class.isAssignableFrom(fieldClass)) {
            field.set(this, value);
        } else if (Long.class.isAssignableFrom(fieldClass)) {
            field.set(this, Long.decode(value));
        } else if (Integer.class.isAssignableFrom(fieldClass)) {
            field.set(this, Integer.decode(value));
        } else if (BigDecimal.class.isAssignableFrom(fieldClass)) {
            field.set(this, new BigDecimal(value));
        } else if (Uri.class.isAssignableFrom(fieldClass)) {
            field.set(this, Uri.parseUri(value));
        } else if (HostPort.class.isAssignableFrom(fieldClass)) {
            field.set(this, HostPort.parse(value));
        } else if (File.class.isAssignableFrom(fieldClass)) {
            field.set(this, new File(value));
        } else if (Path.class.isAssignableFrom(fieldClass)) {
            field.set(this, Paths.get(value));
        } else if (Duration.class.isAssignableFrom(fieldClass)) {
            field.set(this, new Duration(value));
        } else if (TwofacedTime.class.isAssignableFrom(fieldClass)) {
            field.set(this, TwofacedTime.parseHumanReadable(value));
        } else if (SimpleDateFormat.class.isAssignableFrom(fieldClass)) {
            field.set(this, new SimpleDateFormat(value));
        } else if (Charset.class.isAssignableFrom(fieldClass)) {
            field.set(this, Charset.forName(value));
        } else if (Boolean.class.isAssignableFrom(fieldClass)) {
            field.set(this, BooleanUtils.parse(value));
        } else if (CompiledFormatter.class.isAssignableFrom(fieldClass)) {
            field.set(this, new TextFormatCompiler().compile(value));
        } else if (Pattern.class.isAssignableFrom(fieldClass)) {
            field.set(this, RegexUtils.compilePattern(value));
        } else if (MailAddress.class.isAssignableFrom(fieldClass)) {
            field.set(this, MailAddress.parse(value));
        } else if (LocalTime.class.isAssignableFrom(fieldClass)) {
            field.set(this, param.pattern().isEmpty() ? LocalTime.parse(value) : LocalTime.parse(value, DateTimeFormatter.ofPattern(param.pattern())));
        } else if (Date.class.isAssignableFrom(fieldClass)) {
            field.set(this, new SimpleDateFormat(param.pattern()).parse(value));
        } else if (Json.class.isAssignableFrom(fieldClass)) {
            field.set(this, Json.parse(value));
        } else if (Ip4Range.class.isAssignableFrom(fieldClass)) {
            field.set(this, Ip4Range.parse(value));
        } else if (fieldClass.isArray()) {
            if (int.class.isAssignableFrom(fieldClass.getComponentType())) {
                field.set(this, NumberUtils.parseIntValues(StringUtils.parseDelimited(value)));
            } else if (String.class.isAssignableFrom(fieldClass.getComponentType())) {
                field.set(this, StringUtils.parseDelimited(value));
            } else if (Long.class.isAssignableFrom(fieldClass.getComponentType())) {
                field.set(this, NumberUtils.parseLongs(StringUtils.parseDelimited(value)));
            } else if (BigDecimal.class.isAssignableFrom(fieldClass.getComponentType())) {
                field.set(this, NumberUtils.parseBigDecimals(StringUtils.parseDelimited(value)));
            } else if (Uri.class.isAssignableFrom(fieldClass.getComponentType())) {
                field.set(this, Uri.parseUris(StringUtils.parseDelimited(value)));
            } else if (HostPort.class.isAssignableFrom(fieldClass.getComponentType())) {
                field.set(this, HostPort.parse(StringUtils.parseDelimited(value)));
            } else if (Duration.class.isAssignableFrom(fieldClass.getComponentType())) {
                field.set(this, Duration.parse(StringUtils.parseDelimited(value)));
            } else if (Ip4Range.class.isAssignableFrom(fieldClass.getComponentType())) {
                List<Ip4Range> ip4Ranges = Ip4Range.parseBunch(value);
                field.set(this, ip4Ranges.toArray(new Ip4Range[ip4Ranges.size()]));
            } else {
                throw new UnsupportedOperationException("Don't know how to deserialize array " + Spell.get(field));
            }
        } else {
            throw new UnsupportedOperationException("Don't know how to deserialize " + Spell.get(field));
        }
    }

    private static void processIncludeField(LogDome log, ReadOnlyProperties cfg, Field field, AutoConfiguration includeConf, Set<String> usedKeys) throws InvalidConfigurationException {
        Include i = field.getAnnotation(Include.class);
        if (!i.prefix().isEmpty()) {
            Set<String> prefixedUsedKeys = new HashSet<>();
            includeConf.loadInternal(log, cfg.getBranch(i.prefix()), prefixedUsedKeys);
            prefixedUsedKeys.stream().map(s -> i.prefix() + s).collect(Collectors.toCollection(() -> usedKeys));
        } else {
            includeConf.loadInternal(log, cfg, usedKeys);
        }
    }

    /**
     * Возвращает индексированное представление конфигурации
     * в виде карты «ключ → вложенная конфигурация».
     * <p>
     * Для простых параметров значение карты нул, у инклюдов —
     * вложенная конфигурация.
     * <p>
     * Возвращаемая карта — используемая конфигурацией для своих нужд,
     * все изменения будут отражены.
     *
     * @return карта конфигурации
     */
    public Map<Field, AutoConfiguration> confFields() {
        if (confFields == null) {
            confFields = gatherFields();
        }
        return confFields;
    }

    private Map<Field, AutoConfiguration> gatherFields() {
        Set<String> overridenNames = new TreeSet<>();
        Map<Field, AutoConfiguration> fields = Collections.emptyMap();
        Class<?> cl = this.getClass();
        while (true) {
            Field[] declaredFields = cl.getDeclaredFields();
            Map<Field, AutoConfiguration> localFields = new LinkedHashMap<>(fields.size() + declaredFields.length);
            for (Field f : declaredFields) {
                f.setAccessible(true);
                boolean annotation = false;
                if ((f.isAnnotationPresent(Param.class) || (annotation = f.isAnnotationPresent(Include.class))) && overridenNames.add(f.getName())) {
                    if (annotation) {
                        AutoConfiguration includeConf = getIncludeConf(f);
                        localFields.put(f, includeConf);
                    } else {
                        localFields.put(f, null);
                    }
                }
            }
            localFields.putAll(fields);
            if (cl == AutoConfiguration.class) {
                return localFields;
            }
            fields = localFields;
            cl = cl.getSuperclass();
        }
    }

    private AutoConfiguration getIncludeConf(Field field) {
        Class<?> fieldClass = field.getType();
        if (!AutoConfiguration.class.isAssignableFrom(fieldClass)) {
            throw new IllegalArgumentException(
                "Include field " + Spell.get(field) + " in class " + Spell.get(getClass())
                    + " has unexpected type " + Spell.get(fieldClass)
            );
        }
        AutoConfiguration includeConf;
        try {
            includeConf = (AutoConfiguration) field.get(this);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot reach instance of include field " + Spell.get(field) + " in class " + Spell.get(getClass()), e);
        }
        if (includeConf == null) {
            throw new IllegalArgumentException(
                "Include field " + Spell.get(field) + " in class " + Spell.get(getClass())
                    + " is not initialized"
            );
        }
        return includeConf;
    }

    // ------------ toString suite -------------

    /**
     * Формирует строку, в которой перечислены все печатаемые значения конфигурации.
     * <p>
     * Выводом поля можно управлять параметрами аннотаций {@link Param} и {@link Include}.
     *
     * @return
     */
    @Override
    public String toString() {
        StringDumpBuilder sdb = new StringDumpBuilder();
        appendToString(sdb, "");
        return sdb.toString();
    }

    private void appendToString(StringDumpBuilder sdb, String prefix) {
        for (Map.Entry<Field, AutoConfiguration> entry : confFields().entrySet()) {
            Field f = entry.getKey();
            try {
                Param c = f.getAnnotation(Param.class);
                if (c != null) {
                    appendToStringField(sdb, prefix, f, c);
                } else {
                    appendToStringInclude(sdb, prefix, f, entry.getValue());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Illegal access at configuration dumping for class " + getClass(), e);
            }
        }
    }

    private void appendToStringField(StringDumpBuilder sdb, String prefix, Field f, Param c) throws IllegalAccessException {
        if (!c.hidden()) {
            Object value = f.get(this);
            String valueDump = c.password() ? value == null ? "null" : "*hidden*" : Spell.get(value);
            sdb.append(prefix + (getParamName(f, c)) + '=' + valueDump);
        }
    }

    private static void appendToStringInclude(StringDumpBuilder sdb, String prefix, Field f, AutoConfiguration includeConf) {
        Include i = f.getAnnotation(Include.class);
        if (!i.doNotPrint()) {
            includeConf.appendToString(sdb, prefix + i.prefix());
        }
    }


    // ------------ ReadOnlySource suite -------------
    private String compileReadOnlySourceValue(Field f, Param c) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (c.viaCompile() || c.viaParseCompile()) {
            Class<?> cl = this.getClass();
            Method compiler = cl.getMethod("compile" + Character.toUpperCase(f.getName().charAt(0)) + f.getName().substring(1));
            compiler.setAccessible(true);
            return (String) compiler.invoke(this);
        } else {
            Object value = f.get(this);
            if (value == null) {
                return null;
            }
            Class<?> fieldClass = f.getType();
            // некоторые очевидные преобразования сделаем явно
            if (BigDecimal.class.isAssignableFrom(fieldClass)) {
                return ((BigDecimal) value).toPlainString();
            } else if (SimpleDateFormat.class.isAssignableFrom(fieldClass)) {
                return ((SimpleDateFormat) value).toPattern();
            } else if (Date.class.isAssignableFrom(fieldClass)) {
                return new SimpleDateFormat(c.pattern()).format(((Date) value));
            } else {
                return c.transf().transformPrintable(value);
            }
        }
    }

    /**
     * Возвращает текстовое представление всех значений конфигурации и инклюдов на момент вызова метода.
     *
     * @return карта-снапшот конфигурации
     */
    @Override public Map<String, String> dump() {
        Map<String, String> result = new LinkedHashMap<>();
        dumpInternal(result, "");
        return result;
    }

    private void dumpInternal(Map<String, String> target, String prefix) {
        for (Map.Entry<Field, AutoConfiguration> entry : confFields().entrySet()) {
            Field f = entry.getKey();
            try {
                Param c = f.getAnnotation(Param.class);
                if (c != null) {
                    String value = compileReadOnlySourceValue(f, c);
                    if (value != null) {
                        target.put(prefix + (getParamName(f, c)), value);
                    }
                } else {
                    entry.getValue().dumpInternal(target, prefix + f.getAnnotation(Include.class).prefix());
                }
            } catch (Exception e) {
                throw new RuntimeException("Illegal access at configuration dumping for class " + getClass(), e);
            }
        }
    }

    /**
     * Выясняет значение поля по его ключу.
     * <p>
     * Перебирает все поля, пока не найдёт подходящее.
     *
     * @param key ключ.
     * @return значение проперти либо null, если проперти нету.
     */
    @Override public String get(String key) throws SourceUnavailableException {
        try {
            return getInternal(key, "");
        } catch (Exception e) {
            throw new SourceUnavailableException("Error reading key " + Spell.get(key), e);
        }
    }

    private String getInternal(String key, String prefix) {
        try {
            for (Map.Entry<Field, AutoConfiguration> entry : confFields().entrySet()) {
                Field f = entry.getKey();
                Param c = f.getAnnotation(Param.class);
                if (c != null) {
                    String thisKey = prefix + (getParamName(f, c));
                    if (key.equals(thisKey)) {
                        return compileReadOnlySourceValue(f, c);
                    }
                } else {
                    String result = entry.getValue().getInternal(key, prefix + f.getAnnotation(Include.class).prefix());
                    if (result != null) {
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed retrieving key " + Spell.get(key) + " from configuration class " + getClass(), e);
        }
        return null;
    }

    /**
     * Проверяет наличие проперти.
     * Фактически — получение значения {@link #get(String)} и проверка, нул вернулся или нет.
     *
     * @param key название ключа
     * @return true, если такому ключу назначено значение.
     */
    @Override public boolean has(String key) throws SourceUnavailableException {
        return get(key) != null;
    }

    // ----------- утилиты

    /**
     * Ограниченная реализация возвращения джсон-представления конфигурации.
     * <p>
     * Правила трансляции вполне очевидные.
     * Точка в имени ключа обозначает вложенные ключи. Например, конфигурация
     * <pre>
     *     &#64;Param(key="foo.bar") public String foobar = "value";
     *     &#64;Param(key="foo.kuka") public int fookuka = 123;
     * </pre>
     * будет отображена джсоном <code>{foo:{bar:"value",kuka=123}}</code>.
     * <p>
     * Числа кладутся как числа, строки как строки, SimpleDateFormat как #toPattern(), остальное как #toString();
     *
     * @return Json
     * @see Json#parse(String)
     */
    public Json toJson() {
        Json root = new Json();
        try {
            appendJsonTo(root);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error exporting conf to JSON", e);
        }
        return root;
    }

    protected void appendJsonTo(Json root) throws IllegalAccessException {
        for (Map.Entry<Field, AutoConfiguration> entry : confFields().entrySet()) {
            Field f = entry.getKey();
            if (entry.getValue() != null) {
                entry.getValue().appendJsonTo(root.getOrSpawnObjectItem(f.getAnnotation(Include.class).prefix()));
                continue;
            }
            Param c = f.getAnnotation(Param.class);
            String name = getParamName(f, c);
            Json node = root.getOrSpawnObjectItem(name);

            Class<?> fieldClass = f.getType();
            Object value = f.get(this);
            if (value == null) {
                node.drop();
            } else if (fieldClass.isPrimitive()) {
                if (int.class.isAssignableFrom(fieldClass)) {
                    node.set((Integer) value);
                } else if (long.class.isAssignableFrom(fieldClass)) {
                    node.set((Long) value);
                } else if (char.class.isAssignableFrom(fieldClass)) {
                    node.set(value.toString());
                } else if (byte.class.isAssignableFrom(fieldClass)) {
                    node.set(Integer.valueOf(((Byte) value).intValue()));
                } else if (boolean.class.isAssignableFrom(fieldClass)) {
                    node.set((Boolean) value);
                } else if (float.class.isAssignableFrom(fieldClass) || double.class.isAssignableFrom(fieldClass)) {
                    node.set(new BigDecimal(value.toString()));
                } else if (short.class.isAssignableFrom(fieldClass)) {
                    node.set(Integer.valueOf(((Short) value).intValue()));
                } else {
                    throw new UnsupportedOperationException("Don't know how to export privitive " + Spell.get(f) + " to Json");
                }
            } else if (fieldClass.isArray()) {
                throw new UnsupportedOperationException("Array export to Json not yet implemented (failed on field " + Spell.get(f) + ')');
            } else if (String.class.isAssignableFrom(fieldClass)) {
                node.set((String) value);
            } else if (Flag.class.isAssignableFrom(fieldClass)) {
                node.set((Flag) value);
            } else if (Enum.class.isAssignableFrom(fieldClass)) {
                node.set((Enum) value);
            } else if (BigDecimal.class.isAssignableFrom(fieldClass)) {
                node.set((BigDecimal) value);
            } else if (Integer.class.isAssignableFrom(fieldClass)) {
                node.set((Integer) value);
            } else if (Long.class.isAssignableFrom(fieldClass)) {
                node.set((Long) value);
            } else if (Instant.class.isAssignableFrom(fieldClass)) {
                node.set((Instant) value);
            } else if (Date.class.isAssignableFrom(fieldClass)) {
                node.set((Date) value);
            } else if (Boolean.class.isAssignableFrom(fieldClass)) {
                node.set((Boolean) value);
            } else if (SimpleDateFormat.class.isAssignableFrom(fieldClass)) {
                node.set(((SimpleDateFormat) value).toPattern());
            } else {
                // generic approach
                node.set(value.toString());
            }
        }
    }

    /**
     * Ограниченная реализация возвращения xml-представления конфигурации,
     * каждый ключ становится тэгом, значения — текстовые данные.
     * Конфигурация оборачивается в корневой элемент с именем класса.
     * <p>
     * Это конвиниенс-метод, он преобразует проперти, которые отдаёт метод {@link #dump()},
     * в соответствующую XML-структуру посредством {@link ElementUtils#fromMap(Element, Map)}.
     * <p>
     * Правила трансляции достаточно очевидны.
     * Точка в имени ключа обозначает вложенные элементы. Например, конфигурация
     * <pre>
     *     public class FooConf extends AutoConfiguration {
     *       &#64;Param(key="foo.bar") public String foobar = "value";
     *       &#64;Param(key="foo.kuka") public int fookuka = 123;
     *     }
     * </pre>
     * будет отображена так:
     * <pre>
     *     &lt;FooConf&gt;
     *       &lt;foo&gt;
     *         &lt;bar&gt;value&lt;/bar&gt;
     *         &lt;kuka&gt;123&lt;/kuka&gt;
     *       &lt;/foo&gt;
     *     &lt;/FooConf&gt;
     * </pre>
     *
     * @return корневой элемент
     * @see #dump()
     * @see ElementUtils#fromMap(Element, Map)
     * @see ElementUtils#toMap(Element)
     */
    public Element toXml() {
        Element root = new Element(getClass().getSimpleName());
        ElementUtils.fromMap(root, dump());
        return root;
    }

    /**
     * Отдаёт квери-представление конфигурации.
     * <p>
     * Это конвиниенс-метод, он преобразует проперти, которые отдаёт метод {@link #dump()},
     * в соответствующую квери-строку посредством {@link Uri#compileQuery(Map)}.
     * <p>
     * Например, конфигурация
     * <pre>
     *       &#64;Param(key="foo.bar") public String foobar = "value";
     *       &#64;Param(key="foo.kuka") public int fookuka = 123;
     * </pre>
     * будет возвращена как
     * <pre>
     * foo.bar=value&foo.kuka=123
     * </pre>
     *
     * @return квери-строка
     * @see #dump()
     * @see Uri#compileQuery(Map)
     */
    public String toQuery() {
        return Uri.compileQuery(this);
    }
}