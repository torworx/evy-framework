package evyframework.container.context;

public interface BeanContext {

	<T> T getInstance(String name);

	<T> T getInstance(String name, Class<T> requiredType);

	<T> T getInstance(Class<T> requiredType);

	/**
	 * Returns instance of whatever component the factory identified by the
	 * given name produces.
	 * 
	 * @param name
	 *            The name of the factory to obtain an instance from.
	 * @param parameters
	 *            Any parameters needed by the factory to produce the component
	 *            instance.
	 * @return An instance of the component the factory identified by the given
	 *         name produces.
	 */
	<T> T getInstance(String name, Object... parameters);

}
