package tk.bolovsrol.utils.mail;

import tk.bolovsrol.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Почтовый адрес.
 * <p/>
 * Класс также содержит статические хелперы для парсинга строки адреса (адресов).
 */
public class MailAddress {

    /**
     * Символы, которыми разделяются почтовые адреса в строке. Используется при парсинге.
     *
     * @see #parseMulti(String)
     */
    public static final char[] ADDRESS_DELIMITERS = {',', ';'};

    private String alias;
    private String name;
    private String domain;

    public MailAddress() {
    }

    public MailAddress(String name, String domain) {
        this.name = name;
        this.domain = domain;
    }

    /**
     * “alias &lt;name@domain&gt;”
     *
     * @param alias
     * @param name
     * @param domain
     */
    public MailAddress(String alias, String name, String domain) {
        this.alias = alias;
        this.name = name;
        this.domain = domain;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getRawAddress() {
        StringBuilder sb = new StringBuilder(32);
//        sb.append('<');
        sb.append(name);
        if (domain != null) {
            sb.append('@');
            sb.append(domain);
        }
//        sb.append('>');
        return sb.toString();
    }

    @Override public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof MailAddress)) { return false; }
        MailAddress that = (MailAddress) o;
        return Objects.equals(alias, that.alias) &&
            Objects.equals(name, that.name) &&
            Objects.equals(domain, that.domain);
    }

    @Override public int hashCode() {
        return Objects.hash(alias, name, domain);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        if (alias != null) {
            sb.append(alias);
            sb.append(" <");
        }
        sb.append(name);
        if (domain != null) {
            sb.append('@');
            sb.append(domain);
        }
        if (alias != null) {
            sb.append('>');
        }
        return sb.toString();
    }

    public static List<MailAddress> parseMulti(String source) {
        String[] items = StringUtils.parseDelimited(source, ADDRESS_DELIMITERS);
        if (items.length == 1) {
            return Collections.singletonList(parse(source));
        } else {
            List<MailAddress> result = new ArrayList<MailAddress>(items.length);
            for (String item : items) {
                result.add(MailAddress.parse(item));
            }
            return result;
        }
    }

    public static MailAddress parse(String source) {
        if (source == null) {
            return null;
        }

        int domainEnd = source.lastIndexOf((int) '>');
        if (domainEnd < 0) {
            domainEnd = source.length();
        }
        int domainBegin = source.lastIndexOf((int) '@', domainEnd);
        String domain;
        int nameEnd;
        if (domainBegin < 0) {
            domain = null;
            nameEnd = domainEnd;
        } else {
            domain = source.substring(domainBegin + 1, domainEnd);
            nameEnd = domainBegin;
        }

        int nameBegin = source.lastIndexOf((int) '<', nameEnd);
        String name;
        String alias;
        if (nameBegin < 0) {
            name = source.substring(0, nameEnd);
            alias = null;
        } else {
            name = source.substring(nameBegin + 1, nameEnd);
            alias = source.substring(0, nameBegin).trim();
        }
        return new MailAddress(alias, name, domain);
    }
}
