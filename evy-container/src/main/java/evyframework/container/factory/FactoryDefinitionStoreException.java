package evyframework.container.factory;

public class FactoryDefinitionStoreException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String resourceDescription;

	private String factoryName;


	/**
	 * Create a new FactoryDefinitionStoreException.
	 * @param msg the detail message (used as exception message as-is)
	 */
	public FactoryDefinitionStoreException(String msg) {
		super(msg);
	}

	/**
	 * Create a new FactoryDefinitionStoreException.
	 * @param msg the detail message (used as exception message as-is)
	 * @param cause the root cause (may be <code>null</code>)
	 */
	public FactoryDefinitionStoreException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Create a new FactoryDefinitionStoreException.
	 * @param resourceDescription description of the resource that the factory definition came from
	 * @param msg the detail message (used as exception message as-is)
	 */
	public FactoryDefinitionStoreException(String resourceDescription, String msg) {
		super(msg);
		this.resourceDescription = resourceDescription;
	}

	/**
	 * Create a new FactoryDefinitionStoreException.
	 * @param resourceDescription description of the resource that the factory definition came from
	 * @param msg the detail message (used as exception message as-is)
	 * @param cause the root cause (may be <code>null</code>)
	 */
	public FactoryDefinitionStoreException(String resourceDescription, String msg, Throwable cause) {
		super(msg, cause);
		this.resourceDescription = resourceDescription;
	}

	/**
	 * Create a new FactoryDefinitionStoreException.
	 * @param resourceDescription description of the resource that the factory definition came from
	 * @param factoryName the name of the factory requested
	 * @param msg the detail message (appended to an introductory message that indicates
	 * the resource and the name of the factory)
	 */
	public FactoryDefinitionStoreException(String resourceDescription, String factoryName, String msg) {
		this(resourceDescription, factoryName, msg, null);
	}

	/**
	 * Create a new FactoryDefinitionStoreException.
	 * @param resourceDescription description of the resource that the factory definition came from
	 * @param factoryName the name of the factory requested
	 * @param msg the detail message (appended to an introductory message that indicates
	 * the resource and the name of the factory)
	 * @param cause the root cause (may be <code>null</code>)
	 */
	public FactoryDefinitionStoreException(String resourceDescription, String factoryName, String msg, Throwable cause) {
		super("Invalid factory definition with name '" + factoryName + "' defined in " + resourceDescription + ": " + msg, cause);
		this.resourceDescription = resourceDescription;
		this.factoryName = factoryName;
	}


	/**
	 * Return the description of the resource that the factory
	 * definition came from, if any.
	 */
	public String getResourceDescription() {
		return this.resourceDescription;
	}

	/**
	 * Return the name of the factory requested, if any.
	 */
	public String getFactoryName() {
		return this.factoryName;
	}

}
