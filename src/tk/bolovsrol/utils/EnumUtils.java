package tk.bolovsrol.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Created by andrew.cherepivsky */
public final class EnumUtils {

    private EnumUtils() {
    }

    /**
     * Проверяет, что одна из строк массива <code>haystack</code>
     * совпадает с паттерном <code>needle</code>.
     * <p/>
     * Тупо перебирает, так что лучше передавать небольшой список.
     * А для больших лучше пользоваться методом contains() какой-нить
     * специально обученной коллекции.
     *
     * @param needle эту иголку ищем
     * @param haystack в этом стогу сена
     * @return true, если таки да.
     */
    @SafeVarargs public static <E extends Enum<E>> boolean oneOf(E needle, E... haystack) {
        if (haystack == null || haystack.length == 0) {
            return false;
        }

        if (needle == null) {
            for (E value : haystack) {
                if (value == null) {
                    return true;
                }
            }
        } else {
            for (E value : haystack) {
                if (needle == value) {
                    return true;
                }
            }
        }
        return false;
    }

    public static <E extends Enum<E>> E pickByName(Class<E> cl, String name) throws UnexpectedBehaviourException {
        if (name == null) {
            return null;
        }
        try {
            return Enum.valueOf(cl, name);
        } catch (IllegalArgumentException e) {
            throw new UnexpectedBehaviourException("No enum item named " + Spell.get(name) + " is found in enum " + cl.getSimpleName(), e);
        }
    }

    @SuppressWarnings({"StringEquality"})
    public static <E extends Enum<E>> E pickByNameOrToString(Class<E> cl, String name) throws UnexpectedBehaviourException {
        if (name == null) {
            return null;
        }
        try {
            return Enum.valueOf(cl, name);
        } catch (IllegalArgumentException e) {
            // небольшой твик. Если простого соответствия нет, пошароёбимся по тому, что выдают енумы в toString().
            for (E item : cl.getEnumConstants()) {
                if (item.toString() != item.name() && item.toString().equals(name)) {
                    return item;
                }
            }
        }
        throw new UnexpectedBehaviourException("No enum item named " + Spell.get(name) + " is found in enum " + cl.getSimpleName());
    }

    public static <E extends Enum<E>> List<E> parse(Class<E> clas, Collection<String> strings) throws IllegalArgumentException {
        if (strings == null) {
            return null;
        }
        List<E> result = new ArrayList<>(strings.size());
        for (String s : strings) {
            result.add(Enum.valueOf(clas, s));
        }
        return result;
    }


}
