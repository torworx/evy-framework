package evyframework.container.factory;

public class FactoryInitializationException extends FactoryException {

	private static final long serialVersionUID = 1L;

	public FactoryInitializationException(String errorContext, String errorCode, String errorMessage, Throwable cause) {
		super(errorContext, errorCode, errorMessage, cause);
	}

	public FactoryInitializationException(String errorContext, String errorCode, String errorMessage) {
		super(errorContext, errorCode, errorMessage);
	}

}
