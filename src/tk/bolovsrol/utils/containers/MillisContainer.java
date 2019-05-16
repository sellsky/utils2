package tk.bolovsrol.utils.containers;

/** Контейнер содержит миллисекунды. */
public interface MillisContainer<V> extends ValueContainer<V> {

	void setValue(Long millis);

	Long getValueMillis();
}
