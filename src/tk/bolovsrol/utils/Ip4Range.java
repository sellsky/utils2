package tk.bolovsrol.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Диапазон: два ip4-адреса.
 */
public class Ip4Range implements Comparable<Ip4Range> {

    private final int ipFloor;
    private final int ipCeiling;

    public Ip4Range(int ipFloor, int ipCeiling) {
        this.ipFloor = ipFloor;
        this.ipCeiling = ipCeiling;
    }

    @Override public int compareTo(Ip4Range that) {
        int result = Integer.compare(this.ipCeiling, that.ipCeiling);
        if (result == 0) { result = Integer.compare(this.ipFloor, that.ipFloor); }
        return result;
    }

    /**
     * Проверяет, содержится ли указанный ip в диапазоне
     *
     * @param ip интересующий ip
     * @return true, если содержится, иначе false
     */
    public boolean contains(int ip) {
        return ipFloor <= ip && ipCeiling >= ip;
    }

    @Override public String toString() {
        return ipFloor == ipCeiling ?
            InternetUtils.ip4toString(ipFloor) :
            InternetUtils.ip4toString(ipFloor) + "~" + InternetUtils.ip4toString(ipCeiling);
    }

    /**
     * Выковыривает из переданной строки все возможные интервалы.
     * Разделителем считаются все символы кроме пробела, точки, дефиса и слэша.
     * Точка разделяет циферки в адресе, дефис разделяет два адреса-диапазона, слэш отделяет маску.
     *
     * @param source
     * @return
     */
    public static List<Ip4Range> parseBunch(String source) throws InvalidIpAddressException {
        if (source == null) { return null; }

        // вытащим всё похожее на адреса
        List<String> strings = new ArrayList<>();
        StringBuilder sb = new StringBuilder(32);
        int len = source.length();
        for (int po = 0; po < len; po++) {
            char ch = source.charAt(po);
            switch (ch) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '-':
            case '/':
            case '.':
                sb.append(ch);
                break;

            case ' ':
                break;

            default:
                if (sb.length() > 0) {
                    strings.add(sb.toString());
                    sb.setLength(0);
                }
            }
        }
        if (sb.length() > 0) {
            strings.add(sb.toString());
        }

        // преобразуем вытащенное
        return CollectionUtils.map(strings, Ip4Range::parse, new ArrayList<>(strings.size()));
    }

    /**
     * Преобразует один диапазон из строки в объект.
     *
     * @param ipString
     * @return
     * @throws InvalidIpAddressException
     * @see #parseBunch(String)
     */
    public static Ip4Range parse(String ipString) throws InvalidIpAddressException {
        if (ipString == null) { return null; }
        int slashPos = ipString.indexOf('/');
        if (slashPos > 0) {
            int ip = InternetUtils.ip4toInt(ipString.substring(0, slashPos));
            int maskDigits;
            try {
                maskDigits = Integer.valueOf(ipString.substring(slashPos + 1));
            } catch (NumberFormatException e) {
                throw new InvalidIpAddressException("Impossible ip mask " + Spell.get(ipString.substring(slashPos + 1)) + " in " + Spell.get(ipString));
            }
            if (maskDigits > 32) { throw new InvalidIpAddressException("Impossible ip mask in " + Spell.get(ipString)); }
            return new Ip4Range(
                ip & 0xffffffff << (32 - maskDigits),
                ip | 0xffffffff >>> (maskDigits)
            );
        }

        int hyphenPos = ipString.indexOf('-');
        if (hyphenPos > 0) {
            int ip1 = InternetUtils.ip4toInt(ipString.substring(0, hyphenPos));
            int ip2 = InternetUtils.ip4toInt(ipString.substring(hyphenPos + 1));
            return new Ip4Range(Math.min(ip1, ip2), Math.max(ip1, ip2));
        }

        int ip = InternetUtils.ip4toInt(ipString);
        return new Ip4Range(ip, ip);
    }

    /**
     * Ищет среди переданных интервалов содержащий указанный адрес и возвращает его.
     * Если адрес нигде не содержится, возвращает нул.
     *
     * @param ranges диапазоны адресов
     * @param ip интересующий адрес
     * @return диапазон, хранящий адрес, или нул
     */
    public static Ip4Range pickRange(Collection<Ip4Range> ranges, int ip) {
        for (Ip4Range range : ranges) {
            if (range.contains(ip)) {
                return range;
            }
        }
        return null;
    }

    /**
     * Ищет среди переданных интервалов содержащий указанный адрес и возвращает его.
     * Если адрес нигде не содержится, возвращает нул.
     *
     * @param ranges диапазоны адресов
     * @param ip интересующий адрес
     * @return диапазон, хранящий адрес, или нул
     */
    public static Ip4Range pickRange(Ip4Range[] ranges, int ip) {
        for (Ip4Range range : ranges) {
            if (range.contains(ip)) {
                return range;
            }
        }
        return null;
    }
}

