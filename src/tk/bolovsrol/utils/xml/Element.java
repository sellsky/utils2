package tk.bolovsrol.utils.xml;

import tk.bolovsrol.utils.NumberUtils;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.caseinsensitive.CaseInsensitiveLinkedHashMap;
import tk.bolovsrol.utils.properties.PlainProperties;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Lightweight XML element.
 * <p/>
 * Умеет хранить в себе атрибуты и прицеплять детишек. Ну и отцеплять.
 */
public class Element {

    private Element parent;

    private final ArrayList<Element> childElements = new ArrayList<>();

    private final CaseInsensitiveLinkedHashMap<String> attributesMap = new CaseInsensitiveLinkedHashMap<>();
    private final PlainProperties attributes = new PlainProperties(attributesMap);

    private final String name;

	public static final String DATE_VALUE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	public static final Element[] EMPTY_ELEMENT_ARRAY = new Element[0];

    /**
     * Создаёт элемент. Пустой.
     * <p/>
     * Элемент не привязывается ни к какому иному элементу.
     * Можно создать привязанный пустой элемент, вызвав метод
     * {@link #newChild(String)} родительского элемента.
     *
     * @param name имя (tag) элемента
     * @see #newChild(String)
     */
    public Element(String name) {
        this.name = name;
    }

    /**
     * Возвращает атрибуты элемента в виде пропертей.
     * <p/>
     * Изменения в пропертях будут отражены в элементе.
     * <p/>
     * Можно воспользоваться методом {@link #a()},
     * он идентичен данному, только название у него короткое.
     * <p/>
     * Можно получить карту атрибутов ({@link #attributesMap()}
     * или копию карты {@link #getAttributesMap()}.
     *
     * @return атрибуты элемента
     * @see #attributesMap()
     * @see #getAttributesMap()
     * @see #a()
     */
    public PlainProperties attributes() {
        return attributes;
    }

    /**
     * Метод идентичен методу {@link #attributes()}, но с сокращённым именем
     * для удобства частого обращения.
     *
     * @return атрибуты элемента
     * @see #attributes()
     */
    public PlainProperties a() {
        return attributes;
    }

    /**
     * Устанавливает атрибут элемента.
     * <p/>
     * Если value == null, то атрибут из элемента удаляется.
     *
     * @param name
     * @param value
     */
    public Element setAttribute(String name, String value) {
        if (value == null) {
            dropAttribute(name);
        } else {
            attributesMap.put(name, value);
        }
        return this;
    }

    /**
     * Устанавливает атрибут элемента.
     * <p/>
     * Если value == null, то атрибут из элемента удаляется.
     *
     * @param name
     * @param value
     */
    public Element setAttribute(String name, Enum<?> value) {
        if (value == null) {
            dropAttribute(name);
        } else {
            attributesMap.put(name, value.name());
        }
        return this;
    }


    /**
     * Устанавливает атрибут элемента в виде десятичной записи числа.
     *
     * @param name
     * @param value
     */
    public Element setAttribute(String name, int value) {
        attributesMap.put(name, String.valueOf(value));
        return this;
    }

    /**
     * Устанавливает атрибут элемента в виде десятичной записи числа.
     *
     * @param name
     * @param value
     */
    public Element setAttribute(String name, long value) {
        attributesMap.put(name, String.valueOf(value));
        return this;
    }

    /**
     * Устанавливает атрибут элемента в виде десятичной записи числа.
     *
     * @param name
     * @param value
     */
    public Element setAttribute(String name, double value) {
        attributesMap.put(name, String.valueOf(value));
        return this;
    }

    @SuppressWarnings({"ObjectToString"})
    public Element setAttribute(String name, Number value) {
        if (value == null) {
            dropAttribute(name);
        } else {
            attributesMap.put(name, value.toString());
        }
        return this;
    }

    protected final DateFormat valueDateFormat = new SimpleDateFormat(DATE_VALUE_FORMAT_PATTERN);

    /**
     * Устанавливает атрибут элемента в виде форматированной записи даты.
     *
     * @param name
     * @param value
     */
    public Element setAttribute(String name, Date value) {
        if (value == null) {
            dropAttribute(name);
        } else {
            attributesMap.put(name, valueDateFormat.format(value));
        }
        return this;
    }

    /**
     * Устанавливает атрибут элемента в виде форматированной записи даты.
     *
     * @param name
     * @param value
     */
    public Element setAttribute(String name, DateFormat dateFormat, Date value) {
        if (value == null) {
            dropAttribute(name);
        } else {
            attributesMap.put(name, dateFormat.format(value));
        }
        return this;
    }

    /**
     * Удаляет атрибут из элемента.
     *
     * @param name имя атрибута, который надо удалить.
     * @return значение удалённого атрибута или null, если атрибута не было.
     */
    public String dropAttribute(String name) {
        return attributesMap.remove(name);
    }

    /**
     * Проверяет наличие атрибута в элементе.
     *
     * @param name имя атрибута
     * @return true, если есть
     */
    public boolean hasAttribute(String name) {
        return attributesMap.containsKey(name);
    }

    public boolean hasAttributes() {
        return !attributesMap.isEmpty();
    }

    /**
     * Отдаёт значение атрибута по имени.
     *
     * @param name имя атрибута
     * @return значение атрибута
     */
    public String getAttribute(String name) {
        return attributesMap.get(name);
    }

    public String getAttributeOrDie(String name) throws UnexpectedBehaviourException {
        if (!hasAttribute(name)) {
            throw new UnexpectedBehaviourException("Attribute " + Spell.get(name) + " isn't set ");
        }
        return getAttribute(name);
    }

	protected static Date parseDate(String value, DateFormat valueDateFormat) throws UnexpectedBehaviourException {
		try {
			return value == null ? null : valueDateFormat.parse(value);
        } catch (ParseException e) {
            throw new UnexpectedBehaviourException("Cannot parse attribute value " + Spell.get(value) + " as date.", e);
        }
    }

    /**
     * Возвращает копию карты атрибутов элемента.
     * <p/>
     * Копия в том смысле, что возвращаемая карта никак не связана
     * с атрибутами элемента. Для того, чтобы использовать карту
     * элемента, можно воспользоваться методом {@link #attributesMap()}.
     * <p/>
     * А ещё можно получить атрибуты в виде пропертей методом {@link #attributes()}.
     *
     * @return копия карты атрибутов
     * @see #attributesMap()
     * @see #attributes()
     */
    public CaseInsensitiveLinkedHashMap<String> getAttributesMap() {
        return new CaseInsensitiveLinkedHashMap<>(attributesMap);
    }

    /**
     * Возвращает карту атрибутов элемента.
     * <p/>
     * Изменения в карте будут отражены в атрибутах элемента.
     * Для того, чтобы получить копию карты атрибутов
     * элемента, можно воспользоваться методом {@link #getAttributesMap()}.
     * <p/>
     * А ещё можно получить атрибуты в виде пропертей методом {@link #attributes()}.
     *
     * @return карта атрибутов
     * @see #getAttributesMap()
     * @see #attributes()
     */
    public CaseInsensitiveLinkedHashMap<String> attributesMap() {
        return attributesMap;
    }

    /**
     * Устанавливает атрибуты элементу.
     * Если у элемента до этого были какие-то атрибуты, то они теряются.
     *
     * @param attributesMap новые атрибуты.
     */
    public void setAttributesMap(Map<String, String> attributesMap) {
        this.attributesMap.clear();
        this.attributesMap.putAll(attributesMap);
    }

    public void addTextData(String value, TextData.Type type) {
        if (value != null) {
            this.addChild(new TextData(value, type));
        }
    }

    public void addTextData(int value, TextData.Type type) {
        this.addChild(new TextData(String.valueOf(value), type));
    }

    public void addTextData(long value, TextData.Type type) {
        this.addChild(new TextData(String.valueOf(value), type));
    }

    public void addTextData(Long value, TextData.Type type) {
        if (value != null) {
            this.addChild(new TextData(value.toString(), type));
        }
    }

    public void addTextData(Integer value, TextData.Type type) {
        if (value != null) {
            this.addChild(new TextData(value.toString(), type));
        }
    }

    public void addTextData(Date date, DateFormat dateFormat, TextData.Type type) {
        if (date != null) {
            this.addChild(new TextData(dateFormat.format(date), type));
        }
    }

    public void addTextData(Date date, TextData.Type type) {
        if (date != null) {
            this.addChild(new TextData(valueDateFormat.format(date), type));
        }
    }

    public void addTextData(String value) {
        addTextData(value, TextData.Type.TEXT);
    }

    public void addTextData(int value) {
        addTextData(value, TextData.Type.TEXT);
    }

    public void addTextData(long value) {
        addTextData(value, TextData.Type.TEXT);
    }

    public void addTextData(Long value) {
        addTextData(value, TextData.Type.TEXT);
    }

    public void addTextData(Integer value) {
        addTextData(value, TextData.Type.TEXT);
    }

    public void addTextData(Date date, DateFormat dateFormat) {
        addTextData(date, dateFormat, TextData.Type.TEXT);
    }

    public void addTextData(Date date) {
        addTextData(date, TextData.Type.TEXT);
    }

    /**
     * Возвращает родительский элемент.
     *
     * @return родитель
     */
    public Element getParent() {
        return parent;
    }

    /**
     * Проверяет, является ли элемент корневым (т.е. без родителя).
     *
     * @return true -- да.
     */
    public boolean hasParent() {
        return parent != null;
    }

    /**
     * Добавляет дитячий элемент после уже существующих.
     * <p/>
     * Добавляемый элемент должен быть сиротой.
     *
     * @param child добавляемый элемент
     * @throws IllegalStateException элемент уже имеет родителя
     * @see #newChild(String)
     * @see #addChild(int, Element)
     */
    public void addChild(Element child) {
        if (child.hasParent()) {
            throw new IllegalStateException("Element has parent already");
        }
        childElements.add(child);
        child.parent = this;
    }

    /**
     * Добавляет дитячие элементы после уже существующих.
     * <p/>
     * Добавляемые элементы должны быть сиротами.
     *
     * @param children добавляемые элементы
     * @throws IllegalStateException элемент уже имеет родителя
     * @see #newChild(String)
     * @see #addChild(int, Element)
     */
    public void addChildren(Element[] children) {
        addChildren(Arrays.asList(children));
    }

    /**
     * Добавляет дитячие элементы после уже существующих.
     * <p/>
     * Добавляемые элементы должны быть сиротами.
     *
     * @param children добавляемые элементы
     * @throws IllegalStateException элемент уже имеет родителя
     * @see #addChild(Element)
     * @see #addChild(int, Element)
     */
    public void addChildren(Collection<? extends Element> children) {
        for (Element child : children) {
            if (child.hasParent()) {
                throw new IllegalStateException("Child element has parent already");
            }
        }
        childElements.addAll(children);
        for (Element child : children) {
            child.parent = this;
        }
    }

    /**
     * Добавляет дитячий элемент в указанную позицию.
     * <p/>
     * Добавляемый элемент должен быть сиротой.
     *
     * @param pos позиция
     * @param child добавляемый элемент
     * @throws IllegalStateException элемент уже имеет родителя
     * @throws IndexOutOfBoundsException позиция нелепая
     * @see #newChild(String)
     * @see #addChild(Element)
     */
    public void addChild(int pos, Element child) {
        if (child.hasParent()) {
            throw new IllegalStateException("Element has parent already");
        }
        childElements.add(pos, child);
        child.parent = this;
    }

    /**
     * Добавляет дитячие элементы в указанную позицию.
     * <p/>
     * Добавляемые элементы должны быть сиротами.
     *
     * @param pos позиция, в которую добавлять
     * @param children добавляемые элементы
     * @throws IllegalStateException элемент уже имеет родителя
     * @see #newChild(String)
     * @see #addChild(int, Element)
     */
    public void addChildren(int pos, Element[] children) {
        addChildren(pos, Arrays.asList(children));
    }

    /**
     * Добавляет дитячие элементы в указанную позицию.
     * <p/>
     * Добавляемые элементы должны быть сиротами.
     *
     * @param pos позиция, в которую добавлять
     * @param children добавляемые элементы
     * @throws IllegalStateException элемент уже имеет родителя
     * @see #addChild(Element)
     * @see #addChild(int, Element)
     */
    public void addChildren(int pos, Collection<? extends Element> children) {
        for (Element child : children) {
            if (child.hasParent()) {
                throw new IllegalStateException("Child element has parent already");
            }
        }
        childElements.addAll(pos, children);
        for (Element child : children) {
            child.parent = this;
        }
    }

    /**
     * Создаёт пустой дитячий элемент, привязанный к данному после существующих.
     *
     * @param childName имя дитёныша
     * @return новый элемент
     * @see #addChild(Element)
     */
    public Element newChild(String childName) {
        Element child = new Element(childName);
        addChild(child);
        return child;
    }

    /**
     * Создаёт пустой дитячий элемент, привязанный к данному в указанной позиции.
     *
     * @param pos позиция, в которую привязывать
     * @param childName имя дитёныша
     * @return новый элемент
     * @see #addChild(int, Element)
     */
    public Element newChild(int pos, String childName) {
        Element child = new Element(childName);
        addChild(pos, child);
        return child;
    }

//    /**
//     * Возвращает xml-представление элемента (и всех его детей).
//     *
//     * @return текст-xml
//     */
//    public String toXmlString() {
//        StringBuffer sb = new StringBuffer(256);
//        appendXmlTo(sb);
//        return sb.toString();
//    }

//    /**
//     * Добавляет xml-представление элемента (и всех его детей) к стрингбуфферу.
//     *
//     * @param sb
//     */
//    public void appendXmlTo(StringBuffer sb) {
//        sb.append('<').append(name);
//        {
//            for (Map.Entry<String, String> attr : attributes.entrySet()) {
//                sb.append(' ').append(attr.getKey()).append("=\"").append(XmlUtils.xmlInvalidate(attr.getValue())).append('"');
//            }
//        }
//        {
//            if (childElements.isEmpty()) {
//                sb.append("/>");
//            } else {
//                sb.append('>');
//                for (Element childElement : childElements) {
//                    childElement.appendXmlTo(sb);
//                }
//                sb.append("</").append(name).append('>');
//            }
//        }
//    }

    public String getName() {
        return name;
    }

    /** Костыль чтобы хоть как-то научить структуру работать с xml namespace. */
    public String getLocalName() {
		if (name == null) { return null; }
		int position = name.indexOf(':');
		return position >= 0 ? name.substring(position + 1) : name;
    }

    /** Сравнение имени элемента с переданным эталоном с возможностью игнорировать
     * хранимое в имени элемента пространство имён ("namespace_prefix:element_name").
	 */
	public boolean equalsName(String name) {
		if (name == null) { return this.name == null; }
		if (this.name == null) { return false; }
		if (name.contains(":")) { return name.equals(this.name); }
		int pisition = this.name.indexOf(':');
		return name.equals(pisition >= 0 ? this.name.substring(pisition + 1) : this.name);
    }

	public ArrayList<Element> childElements() {
		return childElements;
	}

	public Element[] getChildren() {
		return childElements.toArray(new Element[childElements.size()]);
	}

    public Element getFirstChild() {
        return getChild(0);
    }

    public Element getChild(int index) {
        if (childElements == null || childElements.size() <= index) {
            return null;
        }
        return childElements.get(index);
    }

    public String getFirstTextData() {
        return getTextData(0);
    }

    public Long getFirstTextDataAsLong() {
        return NumberUtils.parseLong(getFirstTextData());
    }

    public Integer getFirstTextDataAsInteger() {
        return NumberUtils.parseInteger(getFirstTextData());
    }

    public Integer getFirstTextDataAsInteger(int radix) {
        return NumberUtils.parseInteger(getFirstTextData(), radix);
    }

    public Date getFirstTextDataAsDate() throws UnexpectedBehaviourException {
        return getFirstTextDataAsDate(valueDateFormat);
    }

    public Date getFirstTextDataAsDate(DateFormat dateFormat) throws UnexpectedBehaviourException {
        return parseDate(getFirstTextData(), dateFormat);
    }

    public String getTextData(int index) {
        if (childElements == null || childElements.size() <= index) {
            return null;
        }

        for (Element o : childElements) {
            if (o instanceof TextData) {
                if (index == 0) {
                    return ((TextData) o).getValue();
                }
                --index;
            }
        }
        return null;
    }

    public Date getTextDataAsDate(int index) throws UnexpectedBehaviourException {
        return getTextDataAsDate(index, valueDateFormat);
    }

    public Date getTextDataAsDate(int index, DateFormat dateFormat) throws UnexpectedBehaviourException {
        return parseDate(getTextData(index), dateFormat);
    }

    public String[] getTextDatas() {
        if (childElements == null || childElements.isEmpty()) {
            return StringUtils.EMPTY_STRING_ARRAY;
        }

        ArrayList<String> textDataValues = new ArrayList<>();
        for (Element o : childElements) {
            if (o instanceof TextData) {
                textDataValues.add(((TextData) o).getValue());
            }
        }
        return textDataValues.toArray(new String[textDataValues.size()]);
    }

    public boolean hasChildren() {
        return !childElements.isEmpty();
    }

    public Element[] getChildren(String name) {
        if (childElements.isEmpty()) {
            return EMPTY_ELEMENT_ARRAY;
        }

        ArrayList<Element> children = new ArrayList<>();
        for (Element el : childElements) {
            if (el.equalsName(name)) {
                children.add(el);
            }
        }
        return children.toArray(new Element[children.size()]);
    }

    public Element getFirstChild(String name) {
        return getChild(name, 0);
    }

    public Element getFirstChildOrDie(String name) throws UnexpectedBehaviourException {
        Element child = getFirstChild(name);
        if (child == null) {
            throw new UnexpectedBehaviourException("No child element is found by tag " + Spell.get(name));
        }
        return child;
    }

    public Element getOrSpawnFirstChild(String name) {
        Element child = getFirstChild(name);
        if (child == null) {
            child = newChild(name);
        }
        return child;
    }

    public Element getChild(String name, int index) {
        if (childElements.isEmpty() || childElements.size() <= index) {
            return null;
        }

        for (Element el : childElements) {
            if (el.equalsName(name)) {
                if (index == 0) {
                    return el;
                }
                --index;
            }
        }
        return null;
    }

    public Element getChildOrDie(String name, int index) throws UnexpectedBehaviourException {
        Element child = getChild(name, index);
        if (child == null) {
            throw new UnexpectedBehaviourException("No child element is found by tag " + Spell.get(name) + " and index " + index);
        }
        return child;
    }

    public boolean removeChild(Element child) {
        if (child.hasParent() && child.parent == this) {
            child.parent = null;
            childElements.remove(child);
            return true;
        } else {
            return false;
        }
    }

    public void removeAllChildren() {
        for (Element child : childElements) {
            child.parent = null;
        }
        childElements.clear();
    }

    public void removeChildren(Element[] children) {
        for (Element child : children) {
            removeChild(child);
        }
    }

    public void removeChildren(String name) {
        removeChildren(getChildren(name));
    }

    public int getChildrenCount() {
        return childElements.size();
    }

    public int getChildrenCount(String name) {
        if (childElements.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Element el : childElements) {
            if (el.equalsName(name)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256)
                .append(Spell.get(name));
        if (!attributesMap.isEmpty()) {
            sb.append(" attrs: ").append(Spell.get(attributesMap));
        }
        if (!childElements.isEmpty()) {
            sb.append(" children: ").append(Spell.get(childElements));
        }
        return sb.toString();
    }

    /**
     * Создаёт полную копию эелемента и всей его требухи.
     * <p/>
     * Так, после создания копии изменения в одном из элементов
     * не будут отражаться на другом элементе.
     *
     * @return элемент-копия
     */
    public Element copy() {
        Element copy = new Element(name);
        copy.attributesMap.putAll(this.attributesMap);
        for (Element element : childElements) {
            copy.addChild(element.copy());
        }
        return copy;
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof Element && this.equals((Element) that);
    }

    /**
     * Проверяет равенство элементов. Они равны, если у обоих одинаковое имя,
     * одинаковый набор атрибутов, значения атрибутов такие же,
     * одинаковое количество детей и все дети упорядоченно равны.
     *
     * @param that
     * @return true, если и только если элемент такой же
     */
    public boolean equals(Element that) {
        return this == that ||
                (this.name.equals(that.name)
                        && this.attributesMap.equals(that.attributesMap)
                        && this.childElements.equals(that.childElements)
                );
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() ^ this.attributesMap.hashCode() ^ this.childElements.hashCode();
    }
}