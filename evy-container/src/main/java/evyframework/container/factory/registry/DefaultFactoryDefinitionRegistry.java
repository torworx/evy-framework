package evyframework.container.factory.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evyframework.common.Assert;
import evyframework.common.StringUtils;
import evyframework.container.factory.FactoryDefinitionStoreException;
import evyframework.container.factory.NoSuchFactoryDefinitionException;
import evyframework.container.factory.config.FactoryDefinition;

public class DefaultFactoryDefinitionRegistry implements FactoryDefinitionRegistry {

	private static final Logger log = LoggerFactory.getLogger(DefaultFactoryDefinitionRegistry.class);

	/**
	 * Whether to allow re-registration of a different definition with the same
	 * name
	 */
	private boolean allowFactoryDefinitionOverriding = true;

	/** Map of factory definition objects, keyed by factory name */
	private final Map<String, FactoryDefinition> factoryDefinitionMap = new ConcurrentHashMap<String, FactoryDefinition>();

	/** List of factory definition names, in registration order */
	private final List<String> factoryDefinitionNames = new ArrayList<String>();

	/** Whether factory definition metadata may be cached for all factorys */
	private boolean configurationFrozen = false;

	/** Cached array of factory definition names in case of frozen configuration */
	private String[] frozenFactoryDefinitionNames;

	/**
	 * Set whether it should be allowed to override factory definitions by
	 * registering a different definition with the same name, automatically
	 * replacing the former. If not, an exception will be thrown. This also
	 * applies to overriding aliases.
	 * <p>
	 * Default is "true".
	 * 
	 * @see #registerFactoryDefinition
	 */
	public void setAllowFactoryDefinitionOverriding(boolean allowFactoryDefinitionOverriding) {
		this.allowFactoryDefinitionOverriding = allowFactoryDefinitionOverriding;
	}

	public void registerFactoryDefinitions(FactoryDefinitionRegistry registry) {
		synchronized (this.factoryDefinitionMap) {
			for (String name : registry.getFactoryDefinitionNames()) {
				registerFactoryDefinition(name, registry.getFactoryDefinition(name));
			}
		}
	}

	@Override
	public void registerFactoryDefinition(String factoryName, FactoryDefinition factoryDefinition) {
		Assert.hasText(factoryName, "Factory name must not be empty");
		Assert.notNull(factoryDefinition, "FactoryDefinition must not be null");

		synchronized (factoryDefinitionMap) {
			Object oldFactoryDefinition = this.factoryDefinitionMap.get(factoryName);
			if (oldFactoryDefinition != null) {
				if (!this.allowFactoryDefinitionOverriding) {
					throw new FactoryDefinitionStoreException(factoryDefinition.getResourceDescription(), factoryName,
							"Cannot register factory definition [" + factoryDefinition + "] for factory '"
									+ factoryName + "': There is already [" + oldFactoryDefinition + "] bound.");
				} else {
					if (log.isInfoEnabled()) {
						log.info("Overriding factory definition for factory '" + factoryName + "': replacing ["
								+ oldFactoryDefinition + "] with [" + factoryDefinition + "]");
					}
				}
			} else {
				this.factoryDefinitionNames.add(factoryName);
				this.frozenFactoryDefinitionNames = null;
			}
			this.factoryDefinitionMap.put(factoryName, factoryDefinition);
		}
	}

	@Override
	public void removeFactoryDefinition(String factoryName) {
		Assert.hasText(factoryName, "'factoryName' must not be empty");

		synchronized (this.factoryDefinitionMap) {
			FactoryDefinition fd = this.factoryDefinitionMap.remove(factoryName);
			if (fd == null) {
				if (log.isTraceEnabled()) {
					log.trace("No factory named '" + factoryName + "' found in " + this);
				}
				throw new NoSuchFactoryDefinitionException(factoryName);
			}
			this.factoryDefinitionNames.remove(factoryName);
			this.frozenFactoryDefinitionNames = null;
		}
	}

	public void clearFactoryDefinitions() {
		synchronized (this.factoryDefinitionMap) {
			this.factoryDefinitionMap.clear();
			this.factoryDefinitionNames.clear();
			this.frozenFactoryDefinitionNames = null;
		}
	}

	@Override
	public FactoryDefinition getFactoryDefinition(String factoryName) {
		FactoryDefinition fd = this.factoryDefinitionMap.get(factoryName);
		if (fd == null) {
			if (log.isTraceEnabled()) {
				log.trace("No factory named '" + factoryName + "' found in " + this);
			}
			throw new NoSuchFactoryDefinitionException(factoryName);
		}
		return fd;
	}

	@Override
	public boolean containsFactoryDefinition(String factoryName) {
		Assert.hasText(factoryName, "'name' must not be empty");
		return factoryDefinitionMap.containsKey(factoryName);
	}

	@Override
	public String[] getFactoryDefinitionNames() {
		synchronized (this.factoryDefinitionMap) {
			if (this.frozenFactoryDefinitionNames != null) {
				return this.frozenFactoryDefinitionNames;
			} else {
				return StringUtils.toStringArray(this.factoryDefinitionNames);
			}
		}
	}

	@Override
	public int getFactoryDefinitionCount() {
		return this.factoryDefinitionMap.size();
	}

	public void freezeConfiguration() {
		this.configurationFrozen = true;
		synchronized (this.factoryDefinitionMap) {
			this.frozenFactoryDefinitionNames = StringUtils.toStringArray(this.factoryDefinitionNames);
		}
	}

	public boolean isConfigurationFrozen() {
		return this.configurationFrozen;
	}

	protected boolean isAllowFactoryDefinitionOverriding() {
		return this.allowFactoryDefinitionOverriding;
	}
}
