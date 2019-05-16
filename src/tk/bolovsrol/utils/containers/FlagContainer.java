package tk.bolovsrol.utils.containers;

import tk.bolovsrol.utils.Flag;

/**
 * Забавный контейнер, который может принимать значение YES или NO.
 * <p/>
 * А также это однозначно мапится на булевые значения
 */
public interface FlagContainer extends ValueContainer<Flag> {

    void setValue(boolean value);

    boolean booleanValue();

}