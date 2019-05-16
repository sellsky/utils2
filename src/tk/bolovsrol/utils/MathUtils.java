package tk.bolovsrol.utils;

/**
 * Несколько утилиток для выполнения пяти арифметических действий
 * над Numeric-объектами.
 */
public final class MathUtils {
    private MathUtils() {
    }

    public static Integer inc(Integer a) {
        return Integer.valueOf(a.intValue() + 1);
    }

    public static Long inc(Long a) {
        return Long.valueOf(a.longValue() + 1L);
    }

    public static Float inc(Float a) {
        return new Float(a.floatValue() + 1.0F);
    }

    public static Double inc(Double a) {
        return new Double(a.doubleValue() + 1.0);
    }

    public static Integer dec(Integer a) {
        return Integer.valueOf(a.intValue() - 1);
    }

    public static Long dec(Long a) {
        return Long.valueOf(a.longValue() - 1L);
    }

    public static Float dec(Float a) {
        return new Float(a.floatValue() - 1.0F);
    }

    public static Double dec(Double a) {
        return new Double(a.doubleValue() - 1.0);
    }


    public static Integer add(Integer a, Number b) {
        return Integer.valueOf(a.intValue() + b.intValue());
    }

    public static Long add(Long a, Number b) {
        return Long.valueOf(a.longValue() + b.longValue());
    }

    public static Float add(Float a, Number b) {
        return new Float(a.floatValue() + b.floatValue());
    }

    public static Double add(Double a, Number b) {
        return new Double(a.doubleValue() + b.doubleValue());
    }

    public static Integer sub(Integer a, Number b) {
        return Integer.valueOf(a.intValue() - b.intValue());
    }

    public static Long sub(Long a, Number b) {
        return Long.valueOf(a.longValue() - b.longValue());
    }

    public static Float sub(Float a, Number b) {
        return new Float(a.floatValue() - b.floatValue());
    }

    public static Double sub(Double a, Number b) {
        return new Double(a.doubleValue() - b.doubleValue());
    }


    public static Integer mul(Integer a, Number b) {
        return Integer.valueOf(a.intValue() * b.intValue());
    }

    public static Long mul(Long a, Number b) {
        return Long.valueOf(a.longValue() * b.longValue());
    }

    public static Float mul(Float a, Number b) {
        return new Float(a.floatValue() * b.floatValue());
    }

    public static Double mul(Double a, Number b) {
        return new Double(a.doubleValue() * b.doubleValue());
    }


    public static Integer div(Integer a, Number b) {
        return Integer.valueOf(a.intValue() / b.intValue());
    }

    public static Long div(Long a, Number b) {
        return Long.valueOf(a.longValue() / b.longValue());
    }

    public static Float div(Float a, Number b) {
        return new Float(a.floatValue() / b.floatValue());
    }

    public static Double div(Double a, Number b) {
        return new Double(a.doubleValue() / b.doubleValue());
    }


    public static Integer mod(Integer a, Number b) {
        return Integer.valueOf(a.intValue() % b.intValue());
    }

    public static Long mod(Long a, Number b) {
        return Long.valueOf(a.longValue() % b.longValue());
    }

    public static Float mod(Float a, Number b) {
        return new Float(a.floatValue() % b.floatValue());
    }

    public static Double mod(Double a, Number b) {
        return new Double(a.doubleValue() % b.doubleValue());
    }

    public static Integer add(Integer a, int b) {
        return Integer.valueOf(a.intValue() + b);
    }

    public static Long add(Long a, long b) {
        return Long.valueOf(a.longValue() + b);
    }

    public static Float add(Float a, float b) {
        return new Float(a.floatValue() + b);
    }

    public static Double add(Double a, double b) {
        return new Double(a.doubleValue() + b);
    }

    public static Integer sub(Integer a, int b) {
        return Integer.valueOf(a.intValue() - b);
    }

    public static Long sub(Long a, long b) {
        return Long.valueOf(a.longValue() - b);
    }

    public static Float sub(Float a, float b) {
        return new Float(a.floatValue() - b);
    }

    public static Double sub(Double a, double b) {
        return new Double(a.doubleValue() - b);
    }


    public static Integer mul(Integer a, int b) {
        return Integer.valueOf(a.intValue() * b);
    }

    public static Long mul(Long a, long b) {
        return Long.valueOf(a.longValue() * b);
    }

    public static Float mul(Float a, float b) {
        return new Float(a.floatValue() * b);
    }

    public static Double mul(Double a, double b) {
        return new Double(a.doubleValue() * b);
    }


    public static Integer div(Integer a, int b) {
        return Integer.valueOf(a.intValue() / b);
    }

    public static Long div(Long a, long b) {
        return Long.valueOf(a.longValue() / b);
    }

    public static Float div(Float a, float b) {
        return new Float(a.floatValue() / b);
    }

    public static Double div(Double a, double b) {
        return new Double(a.doubleValue() / b);
    }


    public static Integer mod(Integer a, int b) {
        return Integer.valueOf(a.intValue() % b);
    }

    public static Long mod(Long a, long b) {
        return Long.valueOf(a.longValue() % b);
    }

    public static Float mod(Float a, float b) {
        return new Float(a.floatValue() % b);
    }

    public static Double mod(Double a, double b) {
        return new Double(a.doubleValue() % b);
    }

    public static int pow(int base, int exponent) {
        int result = 1;
        for (int i = exponent; i > 0; i--) {
            result *= base;
        }
        return result;
    }

    public static long pow(long base, int exponent) {
        long result = 1L;
        for (int i = exponent; i > 0; i--) {
            result *= base;
        }
        return result;
    }

    public static double pow(double base, int exponent) {
        double result = 1.0;
        for (int i = exponent; i > 0; i--) {
            result *= base;
        }
        return result;
    }

    public static Integer pow(Integer base, int exponent) {
        return Integer.valueOf(pow(base.intValue(), exponent));
    }

    public static Long pow(Long base, int exponent) {
        return Long.valueOf(pow(base.longValue(), exponent));
    }

    public static Double pow(Double base, int exponent) {
        return new Double(pow(base.doubleValue(), exponent));
    }

    // ------------- сравнения
    public static Integer min(Integer... integers) {
        Integer result = null;
        for (Integer integer : integers) {
            if (integer != null && (result == null || result.compareTo(integer) > 0)) {
                result = integer;
            }
        }
        return result;
    }

    public static Integer max(Integer... integers) {
        Integer result = null;
        for (Integer integer : integers) {
            if (integer != null && (result == null || result.compareTo(integer) < 0)) {
                result = integer;
            }
        }
        return result;
    }

    /**
     * Наибольший общий делитель последовательности чисел.
     * <p/>
     * Используется последовательное нахождение НОДа
     * для каждой пары чисел в последовательности.
     *
     * @param numbers
     * @return НОД последовательности
     * @see #gcd(int, int)
     */
    public static int gcd(int[] numbers) {
        if (numbers.length == 0) {
            throw new IllegalArgumentException("Attempt to compute GCD over empty array");
        }
        int i = numbers.length - 1;
        int result = numbers[i];
        while (i > 0) {
            i--;
            result = gcd(result, numbers[i]);
        }
        return result;
    }

    /**
     * Находит наибольший общий делитель пары чисел.
     * <p/>
     * Алгоритм слизан из статьи педивикии
     * «<a href="http://en.wikipedia.org/wiki/Binary_GCD_algorithm">Binary GCD algorithm</a>».
     *
     * @param c
     * @param d
     * @return НОД c и d
     * @see #gcd(int[])
     */
    public static int gcd(int c, int d) {
        if (c == 0 || d == 0) {
            return c | d;
        }
        int u = Integer.numberOfTrailingZeros(c);
        int v = Integer.numberOfTrailingZeros(d);
        int s = (u < v) ? u : v;
        u = c >> u;
        v = d >> v;
        while (u != v) {
            if (u > v) {
                u -= v;
                u >>= Integer.numberOfTrailingZeros(u);
            } else {
                v -= u;
                v >>= Integer.numberOfTrailingZeros(v);
            }
        }
        return u << s;
    }


    /**
     * Вычисляет разность a - b с учётом возможного переполнения.
     * <p/>
     * Переполнение определяется сопоставлением расстояний а...b и
     * b...{@link Integer#MAX_VALUE}+{@link Integer#MIN_VALUE}...a.
     *
     * @param a
     * @param b
     * @return
     */
    public static int subWithOverflow(int a, int b) {
        return subWithOverflow(a, b, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Вычисляет разность a - b с учётом возможного переполнения.
     * <p/>
     * Переполнение определяется сопоставлением расстояний а...b и b...max+min...a.
     *
     * @param a
     * @param b
     * @param min
     * @param max
     * @return
     */
    public static int subWithOverflow(int a, int b, int min, int max) {
        // x > y
        long x, y;
        boolean inv;
        if (a > b) {
            x = a;
            y = b;
            inv = false;
        } else {
            x = b;
            y = a;
            inv = true;
        }

        long prim = x - y;
        long sec = 1L + max - x + y - min;

        if (prim <= sec) {
            return inv ? (int) (0L - prim) : (int) prim;
        } else {
            return inv ? (int) sec : (int) (0L - sec);
        }
    }
}
