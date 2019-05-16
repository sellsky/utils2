package tk.bolovsrol.utils.http;

public class HttpRequestHashException extends HttpRequestProcessingException {
	private final String modelHash, targetHash, target;

	/**
	 * @param modelHash — модель, ожидаемый хеш.
	 * @param targetHash — цель, полученный хеш.
	 * @param target — целевой текст, по которому вычисляется хеш. */
	public HttpRequestHashException(String modelHash, String targetHash, String target) {
		super(null, null, HttpStatus._403_FORBIDDEN, "Signature hash missing");

		this. modelHash =  modelHash;
		this.targetHash = targetHash;
		this.target     = target;
	}

	@Override public String getMessage() {
		return "Hash mismatch, provided [" + modelHash + "], expected md5('" + target + "')=[" + targetHash + "]";
	}
}


