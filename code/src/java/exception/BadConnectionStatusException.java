package exception;

public class BadConnectionStatusException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6515392384402717206L;

	public BadConnectionStatusException() {
	}

	public BadConnectionStatusException(String message) {
		super(message);
	}

	public BadConnectionStatusException(Throwable cause) {
		super(cause);
	}

	public BadConnectionStatusException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadConnectionStatusException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
