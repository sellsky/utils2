package tk.bolovsrol.utils.textformatter.compiling.modifiers.containers;

import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

/**
 *
 */
public class EnumContainer<E extends Enum<E>> {
    private E value;
    private final Class<E> clas;

    public EnumContainer(Class<E> clas) {
        this.clas = clas;
    }

    public E get(Section section, boolean strict) throws IllegalArgumentException, EvaluationFailedException {
        if (value != null) {
            return value;
        }
        E value = Enum.valueOf(clas, section.evaluate(strict).toUpperCase());
        if (section.isConstant()) {
            this.value = value;
        }
        return value;
    }
}
