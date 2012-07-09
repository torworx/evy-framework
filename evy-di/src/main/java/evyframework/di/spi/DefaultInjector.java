/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/
package evyframework.di.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import evyframework.di.ConfigurationException;
import evyframework.di.Injector;
import evyframework.di.Key;
import evyframework.di.Module;
import evyframework.di.Provider;
import evyframework.di.Scope;

/**
 * A default implementations of a DI injector.
 * 
 * @since initial
 */
public class DefaultInjector implements Injector {

	private DefaultScope singletonScope;
	private Scope noScope;

	private Map<Key<?>, Binding<?>> bindings;
	private InjectionStack injectionStack;
	private Scope defaultScope;

	@SuppressWarnings("unchecked")
	public DefaultInjector(Module... modules) throws ConfigurationException {

		this.singletonScope = new DefaultScope();
		this.noScope = NoScope.INSTANCE;

		// this is intentionally hardcoded and is not configurable
		this.defaultScope = singletonScope;

		this.bindings = new HashMap<Key<?>, Binding<?>>();
		this.injectionStack = new InjectionStack();

		DefaultBinder binder = new DefaultBinder(this);

		// bind self for injector injection...
		binder.bind(Injector.class).toInstance(this);

		// bind modules
		if (modules != null && modules.length > 0) {

			for (Module module : modules) {
				module.configure(binder);
			}
		}
	}

	InjectionStack getInjectionStack() {
		return injectionStack;
	}

	@SuppressWarnings("unchecked")
	<T> Binding<T> getBinding(Key<T> key) throws ConfigurationException {

		if (key == null) {
			throw new NullPointerException("Null key");
		}

		// may return null - this is intentionally allowed in this non-public method
		return (Binding<T>) bindings.get(key);
	}

	<T> void putBinding(Key<T> bindingKey, Provider<T> provider) {
		// TODO: andrus 11/15/2009 - report overriding existing binding??
		bindings.put(bindingKey, new Binding<T>(provider, defaultScope));
	}

	<T> void changeBindingScope(Key<T> bindingKey, Scope scope) {
		if (scope == null) {
			scope = noScope;
		}

		Binding<?> binding = bindings.get(bindingKey);
		if (binding == null) {
			throw new ConfigurationException("No existing binding for key " + bindingKey);
		}

		binding.changeScope(scope);
	}

	@Override
	public Collection<Key<?>> getKeys() {
		return Collections.unmodifiableSet(bindings.keySet());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Collection<Key<T>> getKeysByType(Class<T> type) {
		if (type == null) {
			throw new IllegalArgumentException("'type' must not be null");
		}
		List<Key<T>> answer = new ArrayList<Key<T>>();
		for (Key<?> key : bindings.keySet()) {
			if (type.isAssignableFrom(key.getType())) {
				answer.add((Key<T>) key);
			}
		}
		return Collections.unmodifiableList(answer);
	}

	public <T> T getInstance(Class<T> type) throws ConfigurationException {
		return getProvider(type).get();
	}

	public <T> T getInstance(Key<T> key) throws ConfigurationException {
		return getProvider(key).get();
	}

	public <T> Provider<T> getProvider(Class<T> type) throws ConfigurationException {
		return getProvider(Key.get(type));
	}

	@SuppressWarnings("unchecked")
	public <T> Provider<T> getProvider(Key<T> key) throws ConfigurationException {

		if (key == null) {
			throw new NullPointerException("Null key");
		}

		Binding<T> binding = (Binding<T>) bindings.get(key);

		if (binding == null) {
			throw new ConfigurationException("DI container has no binding for key %s", key);
		}

		return binding.getScoped();
	}

	public void injectMembers(Object object) {
		Provider<Object> provider0 = new InstanceProvider<Object>(object);
		Provider<Object> provider1 = new FieldInjectingProvider<Object>(provider0, this);
		provider1.get();
	}

	public void shutdown() {
		singletonScope.shutdown();
	}

	DefaultScope getSingletonScope() {
		return singletonScope;
	}

	Scope getNoScope() {
		return noScope;
	}

}
