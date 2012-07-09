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

import java.util.List;
import java.util.Map;

import evyframework.di.Binder;
import evyframework.di.BindingBuilder;
import evyframework.di.Key;
import evyframework.di.ListBuilder;
import evyframework.di.MapBuilder;

/**
 * @since initial
 */
class DefaultBinder implements Binder {

	private DefaultInjector injector;

	DefaultBinder(DefaultInjector injector) {
		this.injector = injector;
	}

	public <T> BindingBuilder<T> bind(Class<T> interfaceType) {
		return new DefaultBindingBuilder<T>(Key.get(interfaceType), injector);
	}

	public <T> BindingBuilder<T> bind(Key<T> key) {
		return new DefaultBindingBuilder<T>(key, injector);
	}

	@SuppressWarnings("unchecked")
	public <T> ListBuilder<T> bindList(String bindingName) {
		Class<?> listClass = List.class;
		return new DefaultListBuilder<T>(Key.get((Class<List<?>>) listClass, bindingName), injector);
	}

	@SuppressWarnings("unchecked")
	public <T> MapBuilder<T> bindMap(String bindingName) {
		Class<?> mapClass = Map.class;
		return new DefaultMapBuilder<T>(Key.get((Class<Map<String, ?>>) mapClass, bindingName), injector);
	}

}
