package tk.bolovsrol.utils;

import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Преобразование IP-адреса и прочие штуки. */
public final class InternetUtils {

    private InternetUtils() { }

    /** Регулярное выражение: (перед адресом пробельный или пунктуационный символ
     * исключая точку)(возможно цифра 1 или 2)(одна или две цифры)(точка){так
     * повторяется 4 раза кроме последней точки}(после адреса снова пробельный
     * или пунктуационный символ исключая точку). Точку я исключаю, чтобы не захватить
     * часть чисел из множества перечисленного через точку. Можно в будущем добавить
     * проверку, что после точки нет больше цифр, пока необходимости не вижу. */
    private static final Pattern ip4 = Pattern.compile(
        "(?<=^|[\\p{Punct}\\p{Space}&&[^.]])([12]?\\d{1,2})\\.([12]?\\d{1,2})\\.([12]?\\d{1,2})\\.([12]?\\d{1,2})(?=$|[\\p{Punct}\\p{Space}&&[^.]])");

    /** Находит в тексте первое вхождение похожее на IP адрес, разбирает его и возвращает
     * представленным в виде 32-битного числа. Если адрес найти не получается, то
     * выкидывает исключение. */
    public static int searchIp4AndConvertToIntOrDie(String textWithAddress) throws InvalidIpAddressException {
        if (textWithAddress == null) {
            throw new InvalidIpAddressException("Can't search IP format in NULL string");
        }
        Matcher matcher = ip4.matcher(textWithAddress);
        int[] bytes = new int[4];
        searchAddress: while (matcher.find()) { // Пока удаётся найти очередное вхождение адреса
            for (int index = 0; index < 4; index++) {
                bytes[index] = Integer.valueOf(matcher.group(index+1));
                if (bytes[index] > 255) {
                    continue searchAddress;
                }
            }
            return bytes[0] << 24 | bytes[1] << 16 | bytes[2] << 8 | bytes[3];
        }
        throw new InvalidIpAddressException("Can't search IP format in " + Spell.get(textWithAddress));
    }

    /**
     * Преобразует IP-адресс, записанный как "123.45.67.89", в 32-битное число.
     * <p/>
     * Проверяет формат адреса, в случае неудачи выкидывает исключение..
     *
     * @param address
     * @return число, соответствующее ip-адресу.
     * @throws InvalidIpAddressException ошибка парсинга адреса
     */
    public static int ip4toInt(String address) throws InvalidIpAddressException {
        int[] ipBytes = parseAndValidateIp4String(address);
        return (ipBytes[0] & 0xff) << 24 |
                (ipBytes[1] & 0xff) << 16 |
                (ipBytes[2] & 0xff) << 8 |
                (ipBytes[3] & 0xff);
    }

    /**
     * Преобразует IP-адресс, записанный как массив 4 байтов, в 32-битное число.
     * <p/>
     * Проверяет длину массива, она должна быть равна 4.
     * Из каждого элемента массива используются 8 младших битов.
     *
     * @param array
     * @return число, соответствующее ip-адресу.
     * @throws InvalidIpAddressException ошибка парсинга адреса
     */
    public static int ip4toInt(int[] array) throws InvalidIpAddressException {
        if (array.length != 4) {
            throw new InvalidIpAddressException("Invalid IP address array length " + array.length + ": " + Spell.get(array));
        }
        return (array[0] & 0xff) << 24 |
                (array[1] & 0xff) << 16 |
                (array[2] & 0xff) << 8 |
                (array[3] & 0xff);
    }

    /**
     * Преобразует IP-адресс, записанный как массив 4 байтов, в 32-битное число.
     * <p/>
     * Проверяет длину массива, она должна быть равна 4.
     *
     * @param array
     * @return число, соответствующее ip-адресу.
     * @throws InvalidIpAddressException ошибка парсинга адреса
     */
    public static int ip4toInt(byte[] array) throws InvalidIpAddressException {
        if (array.length != 4) {
            throw new InvalidIpAddressException("Invalid IP address array length " + array.length + ": " + Spell.get(array));
        }
        return (array[0] & 0xff) << 24 |
                (array[1] & 0xff) << 16 |
                (array[2] & 0xff) << 8 |
                (array[3] & 0xff);
    }

    /**
     * Преобразует IP-адресс, записанный как "123.45.67.89", в массив 4 байтов.
     *
     * @param address
     * @return число, соответствующее ip-адресу.
     * @throws InvalidIpAddressException ошибка парсинга адреса
     */
    public static int[] ip4toArray(String address) throws InvalidIpAddressException {
        return parseAndValidateIp4String(address);
    }

    /**
     * Преобразует IP-адресс, 32-битное число, в массив 4 байтов.
     *
     * @param address
     * @return массив 4 байтов (в каждом элементе массива значащие 8 младших битов)
     */
    public static int[] ip4toArray(int address) {
        int[] result = new int[4];
        result[0] = address >> 24 & 0xff;
        result[1] = address >> 16 & 0xff;
        result[2] = address >> 8 & 0xff;
        result[3] = address & 0xff;
        return result;
    }

    /**
     * Преобразует IP-адресс, записанный как 32-битное число, в строку типа "123.45.67.89".
     *
     * @param address
     * @return число, соответствующее ip-адресу.
     */
    public static String ip4toString(int address) {
        return String.valueOf(address >> 24 & 0xff) + '.' +
              (address >> 16 & 0xff) + '.' +
              (address >> 8 & 0xff) + '.' +
              (address & 0xff);
    }

    /**
     * Преобразует IP-адресс, записанный как массив 4 байтов, в строку типа "123.45.67.89".
     * <p/>
     * Из каждого элемента массива используются 8 младших битов.
     *
     * @param array
     * @return число, соответствующее ip-адресу.
     */
    public static String ip4toString(int[] array) {
        return String.valueOf(array[0] & 0xff) + '.' +
              (array[1] & 0xff) + '.' +
              (array[2] & 0xff) + '.' +
              (array[3] & 0xff);
    }

    /**
     * Декодирует численную маску адреса в битовую.
     * <p/>
     * Например, из маски 24 будет сделана маска 4294967040, т.е. в двоичном счислении
     * 11111111.11111111.11111111.00000000.
     *
     * @param valueBits численная маска
     * @return битовая маска
     */
    public static int decodeIp4Mask(int valueBits) {
        return 0xffffffff << (32 - valueBits);
    }

    /**
     * Проверяет, что переданная строка является записью IPv4-адреса.
     * Если что не так, выкидывает исключение.
     *
     * @param ip проверяемая строка с адресом
     * @throws InvalidIpAddressException проверка не пройдена
     */
    public static void validateIp4String(String ip) throws InvalidIpAddressException {
        parseAndValidateIp4String(ip);
    }

    public static int[] parseAndValidateIp4String(String ip) throws InvalidIpAddressException {
        String[] strings = StringUtils.parseDelimited(ip, ".");

        if (strings.length != 4) {
            throw new InvalidIpAddressException("Invalid IP string format " + Spell.get(ip));
        }

        int[] ipBytes = new int[4];
        for (int i = 0; i < strings.length; i++) {
            int val;
            try {
                val = Integer.parseInt(strings[i].trim());
            } catch (NumberFormatException ignored) {
                throw new InvalidIpAddressException("Error parsing numeric value " + Spell.get(strings[i]) + " in group " + (i + 1) + " from IP string " + Spell.get(ip));
            }
            if (val < 0 || val > 255) {
                throw new InvalidIpAddressException("Impossible numeric value " + Spell.get(strings[i]) + " in group" + (i + 1) + " in IP string " + Spell.get(ip));
            }
            ipBytes[i] = val;
        }
        return ipBytes;
    }

    /**
     * Парсит массив адресов.
     *
     * @param addressStrings
     * @return
     * @throws UnexpectedBehaviourException
     * @see #parseInetSocketAddress(String, Integer)
     */
    public static InetSocketAddress[] parseInetSocketAddresses(String[] addressStrings, Integer defaultPortOrNull) throws UnexpectedBehaviourException {
        InetSocketAddress[] isas = new InetSocketAddress[addressStrings.length];
        for (int i = 0; i < addressStrings.length; i++) {
            isas[i] = parseInetSocketAddress(addressStrings[i], defaultPortOrNull);
        }
        return isas;
    }

    /**
     * Парсит строку с сокет-адресом формата <code>[host][:port]</code>.
     * <p/>
     * Если хост не указан, подразумеваются все хосты.
     * Если порт не указан, используется переданный порт по умолчанию,
     * а если он нул, то выкидывается исключение.
     *
     * @param addressString
     * @param defaultPortOrNull
     * @return
     * @throws UnexpectedBehaviourException
     */
    public static InetSocketAddress parseInetSocketAddress(String addressString, Integer defaultPortOrNull) throws UnexpectedBehaviourException {
        String[] parts = StringUtils.parseDelimited(addressString, ':');
        int port;
        switch (parts.length) {
        case 1:
            if (defaultPortOrNull == null) {
                throw new UnexpectedBehaviourException("Expected port number is missing: " + Spell.get(addressString));
            }
            port = defaultPortOrNull;
            break;
        case 2:
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                throw new UnexpectedBehaviourException("Unparseable port nuber " + Spell.get(addressString), e);
            }
            break;
        default:
            throw new UnexpectedBehaviourException("Unparseable address " + Spell.get(addressString));
        }
        if (parts[0].isEmpty()) {
            return new InetSocketAddress(port);
        } else {
            return new InetSocketAddress(parts[0], port);
        }
    }

}
