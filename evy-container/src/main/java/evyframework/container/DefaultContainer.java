/*
    Copyright 2007-2010 Jenkov Development

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package evyframework.container;

import evyframework.container.factory.GlobalFactory;
import evyframework.container.factory.config.BeanContextPostProcessor;
import evyframework.container.factory.impl.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultContainer implements MutableContainer {

	private static final Object[] EMPTY_PARAMS = new Object[] {};
	
	private final List<BeanContextPostProcessor> beanContextPostProcessors = new ArrayList<BeanContextPostProcessor>();

	protected Map<String, GlobalFactory<?>> factories = null;

	public DefaultContainer() {
		this.factories = new ConcurrentHashMap<String, GlobalFactory<?>>();
	}

	public DefaultContainer(Map<String, GlobalFactory<?>> factories) {
		this.factories = factories;
	}

	public void addFactory(String name, GlobalFactory<?> factory) {
		if (this.factories.containsKey(name))
			throw new ContainerException("Container", "FACTORY_ALREADY_EXISTS",
					"Container already contains a factory with this name: " + name);
		this.factories.put(name, new GlobalFactoryProxy(factory));
	}

	public void addValueFactory(String id, Object value) {
		GlobalFactoryBase factory = new GlobalNewInstanceFactory();
		factory.setLocalInstantiationFactory(new ValueFactory(value));
		this.factories.put(id, new GlobalFactoryProxy(factory));
	}

	@SuppressWarnings("unchecked")
	public <T> GlobalFactory<T> replaceFactory(String name, GlobalFactory<?> newFactory) {
		GlobalFactoryProxy factoryProxy = (GlobalFactoryProxy) this.factories.get(name);
		if (factoryProxy == null) {
			addFactory(name, newFactory);
			return null;
		} else {
			return (GlobalFactory<T>) factoryProxy.setDelegateFactory(newFactory);
		}
	}

	public void removeFactory(String id) {
		this.factories.remove(id);
	}

	@SuppressWarnings("unchecked")
	public <T> GlobalFactory<T> getFactory(String id) {
		GlobalFactory<?> factory = this.factories.get(id);
		// if(factory == null) throw new ContainerException("Unknown Factory: "
		// + id);
		return (GlobalFactory<T>) factory;
	}

	public Map<String, GlobalFactory<?>> getFactories() {
		return this.factories;
	}

	@Override
	public void addBeanContextPostProcessor(BeanContextPostProcessor beanContextPostProcessor) {
		beanContextPostProcessors.add(beanContextPostProcessor);
	}

	@Override
	public List<BeanContextPostProcessor> getBeanContextPostProcessors() {
		return beanContextPostProcessors;
	}

	@Override
	public <T> T getInstance(String name) {
		return getInstance(name, EMPTY_PARAMS);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getInstance(String name, Class<T> requiredType) {
		GlobalFactory<T> factory = (GlobalFactory<T>) this.factories.get(name);
		if (factory == null) {
			throw new ContainerException("Container", "UNKNOWN_FACTORY", "Unknown Factory: " + name);
		}
		if (requiredType.isAssignableFrom(factory.getReturnType())) {
			return (T) factory.instance(EMPTY_PARAMS);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getInstance(Class<T> requiredType) {
		for (GlobalFactory<?> factory : this.factories.values()) {
			if (requiredType.isAssignableFrom(factory.getReturnType())) {
				return (T) factory.instance(EMPTY_PARAMS);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T getInstance(String name, Object... parameters) {
		GlobalFactory<T> factory = (GlobalFactory<T>) this.factories.get(name);
		if (factory == null)
			throw new ContainerException("Container", "UNKNOWN_FACTORY", "Unknown Factory: " + name);
		return (T) factory.instance(parameters);
	}

	@Override
	public String[] getInstanceNamesForType(Class<?> type) {
		return getInstanceNamesForType(type, true);
	}

	@Override
	public String[] getInstanceNamesForType(Class<?> type, boolean includeNonSingletons) {
		List<String> result = new ArrayList<String>();
		for (Entry<String, GlobalFactory<?>> entry : this.factories.entrySet()) {
			GlobalFactory<?> factory = entry.getValue();
			if ((factory instanceof GlobalSingletonFactory) || (includeNonSingletons)) {
				if (type.isAssignableFrom(factory.getReturnType())) {
					result.add(entry.getKey());
				}
			}
		}
		return result.toArray(new String[result.size()]);
	}

	@Override
	public <T> Map<String, T> getInstancesOfType(Class<T> type) {
		return getInstancesOfType(type, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Map<String, T> getInstancesOfType(Class<T> type, boolean includeNonSingletons) {
		Map<String, T> result = new HashMap<String, T>();
		for (Entry<String, GlobalFactory<?>> entry : this.factories.entrySet()) {
			GlobalFactory<?> factory = entry.getValue();
			if ((factory instanceof GlobalSingletonFactory) || (includeNonSingletons)) {
				if (type.isAssignableFrom(factory.getReturnType())) {
					result.put(entry.getKey(), (T) factory.instance(EMPTY_PARAMS));
				}
			}
		}
		return result;
	}

	public void init() {
		for (String key : this.factories.keySet()) {
			Object factory = this.factories.get(key);

			if (factory instanceof GlobalFactoryProxy) {
				factory = ((GlobalFactoryProxy) factory).getDelegateFactory();
				if (factory instanceof GlobalSingletonFactory) {
					((GlobalSingletonFactory) factory).instance();
				}
			}
		}
	}

	public void dispose() {
		execPhase("dispose");
	}

	public void execPhase(String phase) {
		for (String key : this.factories.keySet()) {
			execPhase(phase, key);
		}
	}

	public void execPhase(String phase, String factoryName) {
		Object factory = this.factories.get(factoryName);
		if (factory instanceof GlobalFactoryProxy) {
			((GlobalFactoryProxy) factory).execPhase(phase);
		}
	}

}
