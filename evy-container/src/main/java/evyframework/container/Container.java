package evyframework.container;

import evyframework.container.context.ListableBeanContext;
import evyframework.container.factory.GlobalFactory;

import java.util.Map;

/**
 * The factory manager can keep track of the factories in the application. You
 * can register factories, unregister factories and other stuff.
 */
public interface Container extends ListableBeanContext {

	/**
	 * Adds a factory to the container using the given name.
	 * 
	 * @param name
	 *            A name to identify the factory by.
	 * @param factory
	 *            A factory producing some component.
	 */
	void addFactory(String name, GlobalFactory<?> factory);

	/**
	 * Adds a value factory to the container using the given name. A value
	 * factory just returns the value passed in the value parameter. Thus the
	 * value object becomes a singleton. Value factories can be used to add
	 * constants or configuration parameters to the container, though these can
	 * also be added in scripts.
	 * 
	 * @param name
	 *            The name to identify the factory by.
	 * @param value
	 *            The value the value factory is to return (as a singleton).
	 */
	void addValueFactory(String name, Object value);

	/**
	 * Replaces the existing factory with the given name, with the new factory
	 * passed as parameter. All factories referencing the old factory will
	 * hereafter reference the new factory.
	 * 
	 * @param name
	 *            The name of the factory to replace.
	 * @param newFactory
	 *            The new factory that is to replace the old.
	 * @return The old factory - the one that was replaced.
	 */
	<T> GlobalFactory<T> replaceFactory(String name, GlobalFactory<?> newFactory);

	/**
	 * Removes the factory identified by the given name from the container.
	 * 
	 * @param name
	 *            The name identifying the factory to remove.
	 */
	void removeFactory(String name);

	/**
	 * Returns the factory identified by the given name.
	 * 
	 * @param name
	 *            The name identifying the factory to return.
	 * @return The factory identified by the given name.
	 */
	<T> GlobalFactory<T> getFactory(String name);

	/**
	 * Returns a Map containing all the factories in this container.
	 * 
	 * @return A Map containing all the factories in this container.
	 */
	Map<String, GlobalFactory<?>> getFactories();
	
	/**
	 * Initializes the container. Currently this means creating all singletons
	 * and other cached instances.
	 */
	void init();

	/**
	 * Executes the given life cycle phase on all factories in the container.
	 * 
	 * @param phase
	 *            The name of the life cycle phase to execute ("config",
	 *            "dipose" etc.)
	 */
	void execPhase(String phase);

	/**
	 * Executes the given life cycle phase on the factory identified by the
	 * given name.
	 * 
	 * @param phase
	 *            The name of the life cycle phase to execute ("config",
	 *            "dispose" etc.)
	 * @param name
	 *            The name of the factory to execute the life cycle phase on.
	 */
	void execPhase(String phase, String name);

	/**
	 * Executes the "dispose" life cycle phase on all factories in the
	 * container.
	 */
	void dispose();

}
