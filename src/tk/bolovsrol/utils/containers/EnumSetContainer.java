package tk.bolovsrol.utils.containers;

import java.util.Collection;
import java.util.EnumSet;

public interface EnumSetContainer<E extends Enum<E>> extends ValueContainer<EnumSet<E>> {

    void setValue(Collection<E> items);

    Class<E> getElementType();
//
//    void addItem(E item);
//
//    void addItems(Collection<E> items);
//
//    void removeItem(E item);
//
//    void removeItems(Collection<E> items);

}
