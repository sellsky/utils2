package tk.bolovsrol.utils.store;

import tk.bolovsrol.utils.io.LineInputStream;
import tk.bolovsrol.utils.io.LineOutputStream;

/** Объект, который можно сохранять. */
public interface Storeable {

	/**
	 * Записывает образ объекта в хранилище.
	 * <p>
	 * Этот метод {@link StoreManager} вызывает обычно один раз — при завершении жизненного цикла объекта.
	 * Однако возможен и промежуточный вызов для бекапа состояния.
	 *
	 * @param los писатель в хранилище
	 */
	void store(LineOutputStream los) throws Exception;

	/**
	 * Восстанавливает образ объекта из хранилища.
	 * <p>
	 * Этот метод {@link StoreManager} вызывает единственный раз в начале жизненного цикла объекта.
	 * Предполагается (можно рассчитывать), что это первый метод, который вызывают у объекта после его создания.
	 *
	 * @param lis читатель хранилища
	 */
	void restore(LineInputStream lis) throws Exception;

}