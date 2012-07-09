package evyframework.container.factory.support;

import evyframework.common.io.Resource;
import evyframework.common.io.ResourceLoader;
import evyframework.container.factory.FactoryDefinitionStoreException;
import evyframework.container.factory.registry.FactoryDefinitionRegistry;

public interface FactoryDefinitionParser {
	
	FactoryDefinitionRegistry getRegistry();
	
	ResourceLoader getResourceLoader();
	
	ClassLoader getFactoryClassLoader();

	/**
	 * Load Factory definitions from the specified resource.
	 * @param resource the resource descriptor
	 * @return the number of Factory definitions found
	 * @throws FactoryDefinitionStoreException in case of loading or parsing errors
	 */
	int loadFactoryDefinitions(Resource resource) throws FactoryDefinitionStoreException;

	/**
	 * Load Factory definitions from the specified resources.
	 * @param resources the resource descriptors
	 * @return the number of Factory definitions found
	 * @throws FactoryDefinitionStoreException in case of loading or parsing errors
	 */
	int loadFactoryDefinitions(Resource... resources) throws FactoryDefinitionStoreException;

	/**
	 * Load Factory definitions from the specified resource location.
	 * <p>The location can also be a location pattern, provided that the
	 * ResourceLoader of this Factory definition reader is a ResourcePatternResolver.
	 * @param location the resource location, to be loaded with the ResourceLoader
	 * (or ResourcePatternResolver) of this Factory definition reader
	 * @return the number of Factory definitions found
	 * @throws FactoryDefinitionStoreException in case of loading or parsing errors
	 * @see #getResourceLoader()
	 * @see #loadFactoryDefinitions(evyframework.common.io.Resource)
	 * @see #loadFactoryDefinitions(evyframework.common.io.Resource[])
	 */
	int loadFactoryDefinitions(String location) throws FactoryDefinitionStoreException;

	/**
	 * Load Factory definitions from the specified resource locations.
	 * @param locations the resource locations, to be loaded with the ResourceLoader
	 * (or ResourcePatternResolver) of this Factory definition reader
	 * @return the number of Factory definitions found
	 * @throws FactoryDefinitionStoreException in case of loading or parsing errors
	 */
	int loadFactoryDefinitions(String... locations) throws FactoryDefinitionStoreException;


}
