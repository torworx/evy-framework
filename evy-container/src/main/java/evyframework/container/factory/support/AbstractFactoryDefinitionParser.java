package evyframework.container.factory.support;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evyframework.common.Assert;
import evyframework.common.io.Resource;
import evyframework.common.io.ResourceLoader;
import evyframework.common.io.support.ResourcePatternResolver;
import evyframework.common.io.support.ResourcePatternUtils;
import evyframework.container.factory.FactoryDefinitionStoreException;
import evyframework.container.factory.registry.FactoryDefinitionRegistry;

public abstract class AbstractFactoryDefinitionParser implements FactoryDefinitionParser {
	
	/** Logger available to subclasses */
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final FactoryDefinitionRegistry registry;
	
	private ResourceLoader resourceLoader;
	
	private ClassLoader factoryClassLoader;

	public AbstractFactoryDefinitionParser(FactoryDefinitionRegistry registry) {
		Assert.notNull(registry, "FactoryDefinitionRegistry must not be null");
		this.registry = registry;

		// Determine ResourceLoader to use.
		if (this.registry instanceof ResourceLoader) {
			this.resourceLoader = (ResourceLoader) this.registry;
		}
		else {
			this.resourceLoader = ResourcePatternUtils.getFileAsDefaultResourcePatternResolver();
		}

	}

	@Override
	public final FactoryDefinitionRegistry getRegistry() {
		return this.registry;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public ClassLoader getFactoryClassLoader() {
		return factoryClassLoader;
	}

	public void setFactoryClassLoader(ClassLoader factoryClassLoader) {
		this.factoryClassLoader = factoryClassLoader;
	}

	@Override
	public int loadFactoryDefinitions(Resource... resources) throws FactoryDefinitionStoreException {
		Assert.notNull(resources, "Resource array must not be null");
		int counter = 0;
		for (Resource resource : resources) {
			counter += loadFactoryDefinitions(resource);
		}
		return counter;
	}

	@Override
	public int loadFactoryDefinitions(String location) throws FactoryDefinitionStoreException {
		return loadFactoryDefinitions(location, null);
	}

	@Override
	public int loadFactoryDefinitions(String... locations) throws FactoryDefinitionStoreException {
		Assert.notNull(locations, "Location array must not be null");
		int counter = 0;
		for (String location : locations) {
			counter += loadFactoryDefinitions(location);
		}
		return counter;
	}

	/**
	 * Load Factory definitions from the specified resource location.
	 * <p>The location can also be a location pattern, provided that the
	 * ResourceLoader of this Factory definition reader is a ResourcePatternResolver.
	 * @param location the resource location, to be loaded with the ResourceLoader
	 * (or ResourcePatternResolver) of this Factory definition reader
	 * @param actualResources a Set to be filled with the actual Resource objects
	 * that have been resolved during the loading process. May be <code>null</code>
	 * to indicate that the caller is not interested in those Resource objects.
	 * @return the number of Factory definitions found
	 * @throws FactoryDefinitionStoreException in case of loading or parsing errors
	 * @see #getResourceLoader()
	 * @see #loadFactoryDefinitions(org.springframework.core.io.Resource)
	 * @see #loadFactoryDefinitions(org.springframework.core.io.Resource[])
	 */
	public int loadFactoryDefinitions(String location, Set<Resource> actualResources) throws FactoryDefinitionStoreException {
		ResourceLoader resourceLoader = getResourceLoader();
		if (resourceLoader == null) {
			throw new FactoryDefinitionStoreException(
					"Cannot import Factory definitions from location [" + location + "]: no ResourceLoader available");
		}

		if (resourceLoader instanceof ResourcePatternResolver) {
			// Resource pattern matching available.
			try {
				Resource[] resources = ((ResourcePatternResolver) resourceLoader).getResources(location);
				int loadCount = loadFactoryDefinitions(resources);
				if (actualResources != null) {
					for (Resource resource : resources) {
						actualResources.add(resource);
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Loaded " + loadCount + " Factory definitions from location pattern [" + location + "]");
				}
				return loadCount;
			}
			catch (IOException ex) {
				throw new FactoryDefinitionStoreException(
						"Could not resolve Factory definition resource pattern [" + location + "]", ex);
			}
		}
		else {
			// Can only load single resources by absolute URL.
			Resource resource = resourceLoader.getResource(location);
			int loadCount = loadFactoryDefinitions(resource);
			if (actualResources != null) {
				actualResources.add(resource);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Loaded " + loadCount + " Factory definitions from location [" + location + "]");
			}
			return loadCount;
		}
	}
}
