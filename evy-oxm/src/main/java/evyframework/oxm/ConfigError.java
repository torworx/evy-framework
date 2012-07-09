package evyframework.oxm;

public abstract class ConfigError {
	
	private String message;

	public ConfigError(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		return message;
	}
}
