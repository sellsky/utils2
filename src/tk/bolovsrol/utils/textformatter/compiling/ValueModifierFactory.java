package tk.bolovsrol.utils.textformatter.compiling;

/** Генератор модификаторов. */
@FunctionalInterface
public interface ValueModifierFactory {

    /** @return новый модификатор. */
    ValueModifier newModifier();

}
