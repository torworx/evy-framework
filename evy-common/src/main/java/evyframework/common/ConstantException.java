package evyframework.common;

/**
 * Exception thrown when the {@link Constants} class is asked for an invalid
 * constant name.
 * 
 * @author Rod Johnson
 * @since 28.04.2003
 * @see org.springframework.core.Constants
 */
public class ConstantException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	/**
	 * Thrown when an invalid constant name is requested.
	 * 
	 * @param className
	 *            name of the class containing the constant definitions
	 * @param field
	 *            invalid constant name
	 * @param message
	 *            description of the problem
	 */
	public ConstantException(String className, String field, String message) {
		super("Field '" + field + "' " + message + " in class [" + className + "]");
	}

	/**
	 * Thrown when an invalid constant value is looked up.
	 * 
	 * @param className
	 *            name of the class containing the constant definitions
	 * @param namePrefix
	 *            prefix of the searched constant names
	 * @param value
	 *            the looked up constant value
	 */
	public ConstantException(String className, String namePrefix, Object value) {
		super("No '" + namePrefix + "' field with value '" + value + "' found in class [" + className + "]");
	}

}