package tk.bolovsrol.utils.textformatter.compiling;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.textformatter.compiling.evaluators.ProxyKeywordEvaluator;
import tk.bolovsrol.utils.textformatter.compiling.sections.ConstSection;
import tk.bolovsrol.utils.textformatter.compiling.sections.MacroSection;
import tk.bolovsrol.utils.textformatter.compiling.sections.ModifierSection;
import tk.bolovsrol.utils.textformatter.compiling.sections.MultiSection;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Конпелятор шаблона.
 * <p>
 * Шаблон:
 * <code>[текст][{макрос}]...</code>
 * <p>
 * Макрос:
 * <code>ключ[ модификатор[ параметр]...][| модификатор[ параметр]...]...</code>
 * <p>
 * где:     <br/>
 * ключ — название переменной для вычислятеля {@link KeywordEvaluator};<br/>
 * модификатор — преобразователь значения, возвращённого ключом или предыдущим модификатором;<br/>
 * параметр — шаблон, результат вычисления которого модификатор получит в качестве параметра.
 * <p>
 * Модификаторы по умолчаню определены в {@link CommonModifierFactories}. Компилятору можно указывать
 * дополнительные фабрики модификаторов.
 */
public class TextFormatCompiler {

    public static final char DEFAULT_MACRO_OPEN_CHAR = '{';
    public static final char DEFAULT_MACRO_CLOSE_CHAR = '}';
    public static final char DEFAULT_MODIFIER_DELIMITER_CHAR = '|';
    public static final char DEFAULT_MASK_CHAR = '\\';
    public static final char[] DEFAULT_QUOTE_CHARS = {'\"', '\''};

    private static final Section[] EMPTY_SECTIONS = new Section[0];

    /**
     * Изначально используем прокси-вычислятель,
     * а он при форматировании укажет на реальный.
     */
    private ProxyKeywordEvaluator proxyKeywordEvaluator;

    /** Кэш созданных секций для повторного использования. */
    private final Map<Section, Section> madeSections = new HashMap<>();

    /** Фабрика модификаторов. */
    private Map<String, ValueModifierFactory> valueModifierFactoryMap = CommonModifierFactories.MODIFIER_FACTORIES;

    /**
     * Фабрика по умолчанию общая для всех, так что для дополнения нужно заводить копию.
     * Этот флажок тогда сбрасываем.
     */
    private boolean defaultVmfm = true;

    /** Исходный шаблон. */
    private String globalSource;

    private char macroOpenChar = DEFAULT_MACRO_OPEN_CHAR;
    private char macroCloseChar = DEFAULT_MACRO_CLOSE_CHAR;
    private char modifierDelimiterChar = DEFAULT_MODIFIER_DELIMITER_CHAR;
    private char maskChar = DEFAULT_MASK_CHAR;
    private char[] quoteCharsOrNull = DEFAULT_QUOTE_CHARS;

    /**
     * Создаёт форматирователь со стандартными разделителями
     * (фигурные скобки
     * {@link #DEFAULT_MACRO_OPEN_CHAR} и
     * {@link #DEFAULT_MACRO_CLOSE_CHAR}
     * выделяют макрос,
     * палка {@link #DEFAULT_MODIFIER_DELIMITER_CHAR} сцепляет модификаторы,
     * бэкслэш {@link #DEFAULT_MASK_CHAR} маскирует символы и
     * двойные и одинарные кавычки {@link #DEFAULT_QUOTE_CHARS}
     * маскируют строки).
     *
     * @see #DEFAULT_MODIFIER_DELIMITER_CHAR
     * @see #DEFAULT_MACRO_CLOSE_CHAR
     * @see #DEFAULT_MACRO_OPEN_CHAR
     * @see #DEFAULT_MASK_CHAR
     * @see #DEFAULT_QUOTE_CHARS
     */
    public TextFormatCompiler() {
    }

    /**
     * Компилирует шаблон.
     * <p>
     * Если переданный шаблон нул, то вернёт нул.
     *
     * @param template шаблон.
     * @return скомпилированный шаблон или нул
     * @throws InvalidTemplateException шаблон кривой, компиляция не удалась.
     */
    public ProxyingCompiledFormatter compile(String template) throws InvalidTemplateException {
        if (template == null) {
            return null;
        }
        proxyKeywordEvaluator = new ProxyKeywordEvaluator();
        madeSections.clear();
        globalSource = template;
        return new ProxyingCompiledFormatter(template, compileSection(template, 0), proxyKeywordEvaluator);
    }

    /**
     * Компилирует секцию.
     *
     * @param source шаблон секции
     * @param globalPos позиция начала секции в исходном шаблоне
     * @return скомпилированная секция
     * @throws InvalidTemplateException шаблон кривой, компиляця не удалась
     */
    private Section compileSection(String source, int globalPos) throws InvalidTemplateException {
        int sourceLen = source.length();
        if (sourceLen == 0) {
            return registerSection(ConstSection.EMPTY_CONST_SECTION);
        }
        List<Section> result = new ArrayList<>();
        int i = 0;
        while (true) {
            if (i >= sourceLen) {
                return result.size() > 1 ? registerSection(new MultiSection(result)) : result.get(0);
            }
            int macroOpenPos = StringUtils.getClosingPosition(source, macroOpenChar, macroOpenChar, i, maskChar, quoteCharsOrNull);
            if (macroOpenPos < 0) {
                if (i < sourceLen) {
                    checkNoMoreClosingSequences(source, i, globalPos);
                    result.add(registerSection(new ConstSection(StringUtils.unmask(source.substring(i), maskChar, quoteCharsOrNull))));
                }
                return result.size() > 1 ? registerSection(new MultiSection(result)) : result.get(0);
            }
            if (i < macroOpenPos) {
                String text = source.substring(i, macroOpenPos);
                result.add(registerSection(new ConstSection(StringUtils.unmask(text, maskChar, quoteCharsOrNull))));
                i += text.length();
            }
            int macroStartPos = macroOpenPos + 1;
            int macroEndPos = StringUtils.getClosingPosition(source, macroOpenChar, macroCloseChar, macroStartPos, maskChar, quoteCharsOrNull);
            if (macroEndPos < 0) {
                throw InvalidTemplateException.forSource(globalSource, globalPos, source, macroOpenPos, "Sequence opened and not closed");
            }

            String macro = source.substring(macroStartPos, macroEndPos);
            result.add(processMacro(macro, macroStartPos));

            i += macro.length() + 2;
        }
    }

    /**
     * Разбирает шаблон с макросом.
     *
     * @param macro шаблон секции с макросом
     * @param globalPos позиция начала секции в исходном шаблоне
     * @return скомпилированная секция
     * @throws InvalidTemplateException шаблон кривой, компиляця не удалась
     */
    private Section processMacro(String macro, int globalPos) throws InvalidTemplateException {
        String trimmedMacro = macro.trim();
        int spacePos = trimmedMacro.indexOf(' ');
        if (spacePos < 0) {
            // простой случай: это всего лишь макрос.
            return registerSection(new MacroSection(trimmedMacro, proxyKeywordEvaluator));
        } else {
            // сложный случай: макрос с модификаторами
            return processComplexMacro(macro, globalPos);
        }
    }

    /**
     * Разбирает макрос с модификаторами {key modifier[ modifier_arg]... [| modifier[ modifier_arg]...]...}
     *
     * @param macro шаблон секции с макросом и модификаторами
     * @param globalPos позиция начала секции в исходном шаблоне
     * @return скомпилированная секция
     * @throws InvalidTemplateException шаблон кривой, компиляця не удалась
     */
    private Section processComplexMacro(String macro, int globalPos) throws InvalidTemplateException {
        StringParser keySc = new StringParser(macro, ' ', maskChar, quoteCharsOrNull);
        keySc.next();
        int pos = keySc.getTo();

        Section section = registerSection(new MacroSection(keySc.getWord(), proxyKeywordEvaluator));

        while (true) {
            int delimPos = StringUtils.getClosingPosition(macro, '\0', modifierDelimiterChar, pos, maskChar, quoteCharsOrNull);
            String modifierString = delimPos < 0 ? macro.substring(pos) : macro.substring(pos, delimPos);
            StringParser modifierSc = new StringParser(modifierString, ' ', maskChar, quoteCharsOrNull);
            if (!modifierSc.next()) {
                // все модификаторы закончились
                return section;
            }
            String modifierName = modifierSc.getWord();
            int modifierPos = modifierSc.getFrom();
            ValueModifierFactory valueModifierFactory = valueModifierFactoryMap.get(modifierName);
            if (valueModifierFactory == null) {
                throw InvalidTemplateException.forSource(globalSource, globalPos, modifierString, pos + modifierSc.getFrom(), "Unknown macro modifier name " + Spell.get(modifierName));
            }

            Section[] parameterSections;
            if (!modifierSc.next()) {
                // модификатор без параметров
                parameterSections = EMPTY_SECTIONS;
            } else {
                // модификатор с параметрами
                List<Section> parameterSectionsList = new ArrayList<>();
                do {
                    parameterSectionsList.add(compileSection(modifierSc.getWord(), globalPos + pos + modifierSc.getFrom()));
                } while (modifierSc.next());
                parameterSections = parameterSectionsList.toArray(new Section[parameterSectionsList.size()]);
            }
            try {
                section = registerSection(new ModifierSection(section, valueModifierFactory.newModifier(), parameterSections));
            } catch (Exception e) {
                throw InvalidTemplateException.forSource(globalSource, globalPos, modifierString, pos + modifierPos, e);
            }

            if (delimPos < 0) {
                return section;
            }
            pos = delimPos + 1;
        }
    }

    /**
     * Регистрирует секцию, и возвращает секцию для использования.
     * <p>
     * Если такая же секция уже собрана, возвращает её, а переданную теряет.
     *
     * @param section секция для регистрации
     * @param <S> класс секции
     * @return зарегистрированная секция
     */
    @SuppressWarnings({"unchecked"})
    private <S extends Section> S registerSection(S section) {
        S candidate = (S) madeSections.get(section);
        if (candidate == null) {
            candidate = section;
            madeSections.put(section, section);
        }
        return candidate;
    }

    private void checkNoMoreClosingSequences(String source, int from, int golbalPos) throws InvalidTemplateException {
        int macroClosePos = StringUtils.getClosingPosition(source, macroCloseChar, macroCloseChar, from, maskChar, quoteCharsOrNull);
        if (macroClosePos >= 0) {
            throw InvalidTemplateException.forSource(globalSource, golbalPos, source, from, "Too many closing sequences");
        }
    }

    public char getMacroOpenChar() {
        return macroOpenChar;
    }

    public char getMacroCloseChar() {
        return macroCloseChar;
    }

    public char getModifierDelimiterChar() {
        return modifierDelimiterChar;
    }

    public char getMaskChar() {
        return maskChar;
    }

    public char[] getQuoteCharsOrNull() {
        return quoteCharsOrNull;
    }

    public TextFormatCompiler setMacroOpenChar(char macroOpenChar) {
        this.macroOpenChar = macroOpenChar;
        return this;
    }

    public TextFormatCompiler setMacroCloseChar(char macroCloseChar) {
        this.macroCloseChar = macroCloseChar;
        return this;
    }

    public TextFormatCompiler setModifierDelimiterChar(char modifierDelimiterChar) {
        this.modifierDelimiterChar = modifierDelimiterChar;
        return this;
    }

    public TextFormatCompiler setMaskChar(char maskChar) {
        this.maskChar = maskChar;
        return this;
    }

    public TextFormatCompiler setQuoteChars(char[] quoteCharsOrNull) {
        this.quoteCharsOrNull = quoteCharsOrNull;
        return this;
    }

    public TextFormatCompiler registerValueModifierFactory(String tag, ValueModifierFactory factory) {
        if (defaultVmfm) {
            valueModifierFactoryMap = new TreeMap<>(valueModifierFactoryMap);
            defaultVmfm = false;
        }
        valueModifierFactoryMap.put(tag, factory);
        valueModifierFactoryMap.put(tag.toUpperCase(), factory);
        valueModifierFactoryMap.put(tag.toLowerCase(), factory);
        return this;
    }

    public TextFormatCompiler registerValueModifierFactory(String[] tags, ValueModifierFactory factory) {
        for (String tag : tags) {
            registerValueModifierFactory(tag, factory);
        }
        return this;
    }

    public ValueModifierFactory unregisterValueModifierFactory(String tag) {
        return defaultVmfm ? null : valueModifierFactoryMap.remove(tag);
    }

    /**
     * Сокращённый метод для простых случаев: компилирует шаблон и решает его переданным эвалюатором.
     *
     * @param template
     * @param evaluator
     * @return решённый шаблон
     * @throws InvalidTemplateException
     */
    public static String format(String template, KeywordEvaluator evaluator) throws InvalidTemplateException {
        return new TextFormatCompiler().compile(template).format(evaluator);
    }
}
