package tk.bolovsrol.utils;

import java.util.Random;

/** Всякие связанные со случайностями утилитки. */
public final class RandomizeUtils {

    private RandomizeUtils() {}

    /**
     * Генерирует прикольный 6-значный пароль, состоящий из латинских букв и цифр.
     *
     * @return пароль
     */
    public static String generatePassword() {
        // загадочные числа -- 36-ричные zzzzzz и 100000 соответственно.
        return Long.toString((long) (random.nextDouble() * 2116316159.0) + 60466176L, 36);
    }

    private static Random random = new Random(System.currentTimeMillis());

    /**
     * Возвращает случайное целое число в интервале [0; to).
     *
     * @param to
     * @return случайное число
     */
    public static int getInt(int to) {
        return random.nextInt(to);
    }

    /**
     * Возвращает случайное целое число в интервале [from; to).
     *
     * @param to
     * @return случайное число
     */
    public static int getInt(int from, int to) {
        return random.nextInt(to - from) + from;
    }

    //----- random chars

    public static final char[] DECIMAL_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    public static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    public static final char[] ALPHADIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    /**
     * Возвращает случайный символ из переданного набора символов.
     *
     * @param variants
     * @return символ
     * @see #DECIMAL_DIGITS
     * @see #HEX_DIGITS
     * @see #ALPHADIGITS
     * @see #getRandomChars(char[], int)
     */
    public static char getRandomChar(char[] variants) {
        return variants[getInt(variants.length)];
    }

    /**
     * Возвращает последовательность случаынйх символов из переданного набора символов.
     *
     * @param variants
     * @return символ
     * @see #DECIMAL_DIGITS
     * @see #HEX_DIGITS
     * @see #ALPHADIGITS
     * @see #getCharCombination(char[], int)
     * @see #getRandomChar(char[])
     */
    public static char[] getRandomChars(char[] variants, int length) {
        char[] result = new char[length];
        for (int i = 0; i < result.length; i++) {
            result[i] = variants[getInt(variants.length)];
        }
        return result;
    }

    /**
     * Возвращает случайным образом составленную комбинацию, составленную из символов словаря.
     * Каждый символ из словаря используется не более одного раза.
     * <p/>
     * Если в словаре нет одинаковых символов, то в комбинации повторяющихся символов не будет.
     *
     * @param dictionary словарег возможных символов
     * @param length     необходимая длина строки; она не может быть больше длины словаря
     * @return строка случайных символов
     * @see #DECIMAL_DIGITS
     * @see #HEX_DIGITS
     * @see #ALPHADIGITS
     * @see #getRandomChars(char[], int)
     * @see #getIntCombination(int[], int)
     */
    public static char[] getCharCombination(char[] dictionary, int length) {
        if (dictionary.length < length) {
            throw new IllegalArgumentException("Dictionary is too short");
        }

        char[] buf = new char[dictionary.length];
        System.arraycopy(dictionary, 0, buf, 0, dictionary.length);
        int buflen = buf.length;
        char[] result = new char[length];
        for (int i = 0; i < result.length; i++) {
            int pos = getInt(buflen--);
            result[i] = buf[pos];
            if (pos < buflen) {
                System.arraycopy(buf, pos + 1, buf, pos, buflen - pos);
            }
        }
        return result;
    }

    /**
     * Возвращает случайным образом составленную комбинацию, составленную из чисел словаря.
     * Каждое число из словаря используется не более одного раза.
     * <p/>
     * Если в словаре нет одинаковых чисел, то в комбинации повторяющихся чисел не будет.
     *
     * @param dictionary словарег возможных чисел
     * @param length     необходимая длина строки; она не может быть больше длины словаря
     * @return массив случайных чисел
     * @see #DECIMAL_DIGITS
     * @see #HEX_DIGITS
     * @see #ALPHADIGITS
     * @see #getCharCombination(char[], int)
     */
    public static int[] getIntCombination(int[] dictionary, int length) {
        if (dictionary.length < length) {
            throw new IllegalArgumentException("Dictionary is too short");
        }

        int[] buf = new int[dictionary.length];
        System.arraycopy(dictionary, 0, buf, 0, dictionary.length);
        int buflen = buf.length;
        int[] result = new int[length];
        for (int i = 0; i < result.length; i++) {
            int pos = getInt(buflen--);
            result[i] = buf[pos];
            if (pos < buflen) {
                System.arraycopy(buf, pos + 1, buf, pos, buflen - pos);
            }
        }
        return result;
    }

    /**
     * Из переданного массива элементов выбирает один случайным образом.
     * <p/>
     * Вероятность выбора пропорциональна отношению значения
     * элемента массива к сумме значений всех элементов массива.
     *
     * @param probabilities массив пропорциональных вероятностей
     * @return индекс выбранного элемента
     */
    public static int pickItem(int[] probabilities) {
        int sum = 0;
        for (int probability : probabilities) {
            sum += probability;
        }

        int pick = getInt(sum);

        int i = 0;
        while (true) {
            pick -= probabilities[i];
            if (pick < 0) {
                return i;
            }
            i++;
        }
    }

}
