package evyframework.container.factory;

import evyframework.common.StringUtils;

public class NoSuchFactoryDefinitionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/** Name of the missing factory */
	private String factoryName;

	/** Required factory type */
	private Class<?> factoryType;


	/**
	 * Create a new NoSuchFactoryDefinitionException.
	 * @param name the name of the missing factory
	 */
	public NoSuchFactoryDefinitionException(String name) {
		super("No factory named '" + name + "' is defined");
		this.factoryName = name;
	}

	/**
	 * Create a new NoSuchFactoryDefinitionException.
	 * @param name the name of the missing factory
	 * @param message detailed message describing the problem
	 */
	public NoSuchFactoryDefinitionException(String name, String message) {
		super("No factory named '" + name + "' is defined: " + message);
		this.factoryName = name;
	}

	/**
	 * Create a new NoSuchFactoryDefinitionException.
	 * @param type required type of factory
	 */
	public NoSuchFactoryDefinitionException(Class<?> type) {
		super("No unique factory of type [" + type.getName() + "] is defined");
		this.factoryType = type;
	}

	/**
	 * Create a new NoSuchFactoryDefinitionException.
	 * @param type required type of factory
	 * @param message detailed message describing the problem
	 */
	public NoSuchFactoryDefinitionException(Class<?> type, String message) {
		super("No unique factory of type [" + type.getName() + "] is defined: " + message);
		this.factoryType = type;
	}
	
	/**
	 * Create a new NoSuchFactoryDefinitionException.
	 * @param type required type of factory
	 * @param dependencyDescription a description of the originating dependency
	 * @param message detailed message describing the problem
	 */
	public NoSuchFactoryDefinitionException(Class<?> type, String dependencyDescription, String message) {
		super("No matching factory of type [" + type.getName() + "] found for dependency" +
				(StringUtils.hasLength(dependencyDescription) ? " [" + dependencyDescription + "]" : "") +
				": " + message);
		this.factoryType = type;
	}


	/**
	 * Return the name of the missing factory,
	 * if it was a lookup by name that failed.
	 */
	public String getFactoryName() {
		return this.factoryName;
	}

	/**
	 * Return the required type of factory,
	 * if it was a lookup by type that failed.
	 */
	public Class<?> getFactoryType() {
		return this.factoryType;
	}

}
