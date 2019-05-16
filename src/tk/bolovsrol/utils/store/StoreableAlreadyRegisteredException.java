package tk.bolovsrol.utils.store;

public class StoreableAlreadyRegisteredException extends RestoreException {
	public StoreableAlreadyRegisteredException(String message) {
		super(message);
	}

	public StoreableAlreadyRegisteredException(Throwable cause) {
		super(cause);
	}

	public StoreableAlreadyRegisteredException(String message, Throwable cause) {
		super(message, cause);
	}
}
