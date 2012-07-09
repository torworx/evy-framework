package evyframework.oxm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Exception thrown on marshalling validation failure.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ValidationFailureException extends XmlMappingException {

	private static final long serialVersionUID = 1L;
	
	private List _errors;

	/**
	 * Constructs an ValidationFailureException from a message.
	 */
	public ValidationFailureException(String m) {
		super(m);
	}

	/**
	 * Constructs an ValidationFailureException from a message and a cause.
	 */
	public ValidationFailureException(String m, Throwable t) {
		super(m, t);
	}

	/**
	 * Constructs an ValidationFailureException from a cause.
	 */
	public ValidationFailureException(Throwable t) {
		super(t);
	}

	/**
	 * Constructs an ValidationFailureException from an {@link ConfigError}.
	 */
	public ValidationFailureException(ConfigError error) {
		this(error.toString(), null, error);
	}

	/**
	 * Constructs an ValidationFailureException from a message, a cause, and an
	 * {@link ConfigError}.
	 */
	public ValidationFailureException(String m, Throwable t, ConfigError error) {
		this(m, t, Collections.singletonList(error));
	}

	/**
	 * Constructs an ValidationFailureException from a message, a cause, and a collection of
	 * {@link ConfigError ConfigErrors}.
	 */
	public ValidationFailureException(String m, Throwable t, Collection errors) {
		super(m, t);

		if (errors != null)
			_errors = Collections.unmodifiableList(new ArrayList(errors));
	}

	/**
	 * Returns the first {@link ConfigError ConfigErrors} that caused the exception,
	 * if any.
	 */
	public ConfigError getError() {
		if (_errors == null || _errors.size() == 0)
			return null;

		return (ConfigError) _errors.get(0);
	}

	/**
	 * Returns the collection of {@link ConfigError ConfigErrors} that caused the
	 * exception, if any.
	 */
	public Collection getErrors() {
		return _errors;
	}

	
}
