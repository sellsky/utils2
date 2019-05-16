package tk.bolovsrol.utils.properties.files;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.io.LineInputStream;
import tk.bolovsrol.utils.properties.PropertyIdentityValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Построчно разбирает конфигурационный файл.
 * <p/>
 * Пустые строки и строки, начинающиеся с ; или #, игорируются.
 * <pre>
 * # comment
 * ; this line too</pre>
 * <p/>
 * Общий синтаксис ключ=значение. Пробелы в начале и в конце слов игнорируются.
 * <pre>
 * aaa=bbb
 * foo = barco rolla </pre>
 * <p/>
 * Чтобы записать значение с пробелами в начале или в конце, нужно взять значение в кавычки.
 * Также значение нужно брать в кавычки, если строка заканчивается квадратной или фигурной скобкой,
 * иначе такая строка будет принята за начала шаблона или секции (см. ниже)..
 * Внутри строки инвалидировать ничего не нужно.
 * <pre>
 * xyz="  History of so-called "Time Travel"!  "</pre>
 * <p/>
 * Дублирующиеся ключи заменяют существующие.
 * Чтобы значения добавлялись, вместо = нужно указать +=.
 * <pre>
 * spell=one,two,three,four,five,six,seven,eight,nine,ten,
 * spell+=eleven,twelve,thirteen,fourteen,fiveteen,sixteen,seventeen,
 * spell+=eighteen,nineteen,twenty</pre>
 * <p/>
 * Вместо директивы
 * <pre>
 * .include имя_файла</pre>
 * будет вставлено содержимое указанного файла.
 * <p/>
 * Можно группировать значения в секциях. Имя секции добавляется через точку ко всем ключам секции.
 * <pre>
 * section {
 *   foo=bar
 *   abc=xyz
 * }</pre> - то же самое, что
 * <pre>
 * section.foo=bar
 * section.abc=xyz</pre>
 * <p/>
 * Ещё можно использовать шаблоны. С их помощью можно добавлять в несколько секций
 * одни и те же параметры. Описание шаблона аналогично секции, но скобки квадратные.
 * <pre>
 * tpl [
 *   aaa=bbb
 *   .include secret.cfg
 * ]
 * section1 {
 *   .template tpl
 *   foo=bar
 * }
 * section2 {
 *   kaka=buka
 *   .template tpl
 * }</pre> -- то же самое, что
 * <pre>
 * section1 {
 *   aaa=bbb
 *   .include secret.cfg
 *   foo=bar
 * }
 * section2 {
 *   kaka=buka
 *   aaa=bbb
 *   .include secret.cfg
 * }</pre>
 */
public class ConfFileParser {

    /** Контекст секции. */
    private static class Section {
        public final Section parent;
        public final String prefix;
        public final String name;

        private Section(Section parent, String prefix, String name) {
            this.parent = parent;
            this.prefix = prefix;
            this.name = name;
        }

        public Section newChild(String name) {
            return new Section(
                    this,
                    this.prefix.length() == 0 ? name + '.' : this.prefix + name + '.',
                    name
            );
        }

        public Section getParent() {
            return parent;
        }
    }

    /** Аккумулятор полезных данных. */
    private final Map<String, PropertyIdentityValue> data = new LinkedHashMap<String, PropertyIdentityValue>();

    /** Стек файлов для отслеживания циклических ссылок. */
    private final Deque<File> fileStack = new ArrayDeque<File>();

    /** Стек шаблонов для отслеживания циклических ссылок. */
    private final Deque<String> templateStack = new ArrayDeque<String>();

    /** Стек для хранения координат строки. */
    private final Deque<LineIdentity> identityStack = new ArrayDeque<LineIdentity>();

    /** Актуальная секция. */
    private Section section;

    /**
     * Режим заполнения шаблона -- в таком режиме данные не разбираются,
     * а аккумулируются в шаблоне для позднейшего разбора.
     */
    private boolean templateMode;

    /** Считаем вложенность шаблонов. */
    private int templateLevel;

    /** Название актуального шаблона. */
    private String templateName;
    /** Данные актуального шаблона. */
    private List<String> template;

    /** Считанные шаблоны построчно. */
    private final Map<String, List<String>> templates = new TreeMap<String, List<String>>();

    /** Считанные файлы построчно. */
    private final Map<File, List<String>> files = new TreeMap<File, List<String>>();

    /** Даты изменения считанного файла. */
    private final SortedMap<File, Long> fileModificationDates = new TreeMap<File, Long>();

    public ConfFileParser() {
    }

    /**
     * Считывает и разбирает указанный файл.
     *
     * @param file файл
     * @return конфигурация
     * @throws ConfFileParsingException возникла проблема
     */
    public Map<String, PropertyIdentityValue> parse(File file) throws ConfFileParsingException {
        data.clear();
        fileStack.clear();
        templateStack.clear();
        section = new Section(null, "", null);
        templateMode = false;
        templates.clear();
        identityStack.clear();

        try {
            appendFile(file);
        } catch (ConfFileParsingException e) {
            throw e;
        } catch (Exception e) {
            throw ConfFileParsingException.getForFile(file, e);
        }

        return data;
    }

    public SortedMap<File, Long> getFileModificationDates() {
        return Collections.unmodifiableSortedMap(fileModificationDates);
    }

    private List<String> getTemplate(String name) throws UnexpectedBehaviourException {
        List<String> lines = templates.get(name);
        if (lines == null) {
            throw new UnexpectedBehaviourException("Template " + Spell.get(name) + " not found");
        }
        return lines;
    }

    private List<String> getFile(File file) throws UnexpectedBehaviourException {
        List<String> lines = files.get(file);
        if (lines == null) {
            fileModificationDates.put(file, file.lastModified());
            lines = readFile(file);
            files.put(file, lines);
        }
        return lines;
    }

    private static List<String> readFile(File file) throws UnexpectedBehaviourException {
        List<String> lines = new ArrayList<String>();
        try {
            LineInputStream lis = new LineInputStream(new FileInputStream(file));
            try {
                String line;
                while ((line = lis.readLine()) != null) {
                    lines.add(line.trim());
                }
            } finally {
                lis.close();
            }
        } catch (IOException e) {
            throw new UnexpectedBehaviourException("Error reading file " + Spell.get(file), e);
        }
        return lines;
    }

    private void appendFile(File file) throws ConfFileParsingException {
        List<String> lines;
        try {
            lines = getFile(file);
        } catch (UnexpectedBehaviourException e) {
            throw ConfFileParsingException.getForFile(file, e);
        }

        fileStack.push(file);
        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            identityStack.push(new LineIdentity(file.getPath(),lineNumber));
            try {
                processLine(line);
            } catch (UnexpectedBehaviourException e) {
                throw ConfFileParsingException.getForFile(file, lineNumber, line, e);
            }
            identityStack.pop();
        }
        fileStack.pop();
    }

    private void appendTemplate(String name) throws ConfFileParsingException {
        List<String> lines;
        try {
            lines = getTemplate(name);
        } catch (UnexpectedBehaviourException e) {
            throw ConfFileParsingException.getForTemplate(name, e);
        }

        templateStack.push(name);
        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            try {
                processLine(line);
            } catch (UnexpectedBehaviourException e) {
                throw ConfFileParsingException.getForTemplate(name, lineNumber, line, e);
            }
        }
        templateStack.pop();
    }

    private void processLine(String line) throws UnexpectedBehaviourException {
        if (templateMode) {
            if (line.length() > 0) {
                if (line.charAt(0) == ']') {
                    if (templateLevel == 0) {
                        processTemplateEnd();
                        return;
                    } else {
                        templateLevel--;
                    }
                } else if (line.charAt(line.length() - 1) == '[') {
                    templateLevel++;
                }
            }
            appendToTemplate(line);
            return;
        }

        if (line.length() == 0 || line.charAt(0) == ';' || line.charAt(0) == '#') {
            return;
        }

        char firstChar = line.charAt(0);
        if (firstChar == '.') {
            processService(line);
            return;
        }

        if (firstChar == '}') {
            processSectionEnd();
            return;
        }

        char lastChar = line.charAt(line.length() - 1);
        if (lastChar == '[') {
            processTemplateStart(line);
            return;
        }

        if (lastChar == '{') {
            processSectionStart(line);
            return;
        }

        processKeyValue(line);
    }

    private void appendToTemplate(String line) {
        template.add(line);
    }

    private void processService(String line) throws UnexpectedBehaviourException {
        if (line.startsWith(".i") || line.startsWith(".f") || line.startsWith(".+")) {
            processServiceIncludeFile(line);
        } else if (line.startsWith(".t") || line.startsWith(".:")) {
            processServiceIncludeTemplate(line);
        } else {
            throw new UnexpectedBehaviourException("unexpected directive");
        }
    }

    private void processServiceIncludeFile(String line) throws UnexpectedBehaviourException {
        String includeFilename = StringUtils.subWords(line, 1);
        File includeFile = new File(includeFilename);
        if (!includeFile.isAbsolute()) {
            includeFile = new File(fileStack.peek().getParentFile(), includeFilename);
        }
        if (fileStack.contains(includeFile)) {
            throw new UnexpectedBehaviourException("include loop detected");
        }
        appendFile(includeFile);
    }

    private void processServiceIncludeTemplate(String line) throws UnexpectedBehaviourException {
        String templateName = StringUtils.subWords(line, 1);
        if (templateStack.contains(templateName)) {
            throw new UnexpectedBehaviourException("template loop detected");
        }
        appendTemplate(templateName);
    }

    private void processTemplateStart(String line) {
        templateName = line.substring(0, line.length() - 1).trim();
        template = new ArrayList<String>();
        templateMode = true;
        templateLevel = 0;
    }

    private void processTemplateEnd() throws UnexpectedBehaviourException {
        if (!templateMode) {
            throw new UnexpectedBehaviourException("closing template while out of template definition");
        }
        templates.put(templateName, template);
        templateMode = false;
    }

    private void processSectionEnd() throws UnexpectedBehaviourException {
        if (section.parent == null) {
            throw new UnexpectedBehaviourException("closing section while out of section");
        }
        section = section.parent;
    }

    private void processSectionStart(String line) {
        String sectionName = line.substring(0, line.length() - 1).trim();
        section = section.newChild(sectionName);
    }

    private void processKeyValue(String line) throws UnexpectedBehaviourException {
        int equalsPos = line.indexOf('=');
        boolean appendMode;
        String key, value;
        if (equalsPos == -1) {
            appendMode = false;
            key = section.prefix + line;
            value = null;
        } else if (equalsPos == 0) {
            throw new UnexpectedBehaviourException("key missing");
        } else {
            value = line.substring(equalsPos + 1).trim();
            if (line.charAt(equalsPos - 1) == '+') {
                appendMode = true;
                key = section.prefix + line.substring(0, equalsPos - 1).trim();
            } else {
                appendMode = false;
                key = section.prefix + line.substring(0, equalsPos).trim();
            }

            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
        }
        String currentIdentity = getCurrentIdentity();
        PropertyIdentityValue oldValue = data.remove(key);
        if (appendMode && oldValue != null) {
            value = oldValue.getValue() + value;
            currentIdentity = mergeAppendIdentity(oldValue.getIdentity());
        }
        data.put(key, new PropertyIdentityValue(value,currentIdentity));
    }

    /**
     * Совмещаеет координаты для добавляемых значений
     * должна уметь порождать координаты типа:
     * /tmp/test.conf:85
     * /tmp/test.conf:85+89 если приходит к предыдущей /tmp/test.conf:89
     * /tmp/test.conf:85+89 + /tmp/test.conf:90->/tmp/included.conf:15 если приходит к предыдущей /tmp/test.conf:90->/tmp/included.conf:15
     * /tmp/test.conf:85+89+91 + /tmp/test.conf:90->/tmp/included.conf:15 если приходит к предыдущей /tmp/test.conf:91
     * и т.д.
     * @param oldIdentity
     * @return
     */
    private String mergeAppendIdentity(String oldIdentity) {
        LineIdentity currentIdentity = identityStack.peek();
        StringBuilder sb =  new StringBuilder(128);
        if(oldIdentity.contains(currentIdentity.getPath())){
            int index = oldIdentity.indexOf(' ',oldIdentity.indexOf(currentIdentity.getPath())+currentIdentity.getPath().length());
            if(index != -1){
            sb.append(oldIdentity.substring(0,index))
                    .append('+')
                    .append(currentIdentity.getLine())
                    .append(oldIdentity.substring(oldIdentity.indexOf(' ',oldIdentity.indexOf(currentIdentity.getPath())+currentIdentity.getPath().length())));
            }
            else {
                sb.append(oldIdentity)
                        .append('+')
                        .append(currentIdentity.getLine());
            }
        }
        else{
            sb.append(oldIdentity)
                    .append(" + ")
                    .append(getCurrentIdentity());
        }
        return sb.toString();
    }

    /** Получение текущей координаты из identityStack
     * @return Текущая коордианта
     */
    private String getCurrentIdentity(){
        Iterator<LineIdentity> i = identityStack.descendingIterator();
        StringBuilder sb =  new StringBuilder(128);
        sb.append(i.next().toString());
        while(i.hasNext()){
            sb.append("->")
                .append(i.next().toString());
        }
        return sb.toString();
    }

    private static class LineIdentity{
        private final String path;
        private final int line;

        public LineIdentity(String path, int line) {
            this.path = path;
            this.line = line;
        }

        public String getPath(){
            return path;
        }

        public int getLine(){
            return line;
        }

        @Override
        public String toString() {
            return new StringBuilder(128)
                .append(path)
                .append(':')
                .append(line)
                .toString();
        }
    }
}
