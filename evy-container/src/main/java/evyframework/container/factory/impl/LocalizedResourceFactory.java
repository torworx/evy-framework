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

package evyframework.container.factory.impl;

import evyframework.container.factory.GlobalFactory;
import evyframework.container.factory.LocalFactory;

import java.util.Map;
import java.util.Locale;

/**
 * @author Jakob Jenkov - Copyright 2004-2006 Jenkov Development
 */
public class LocalizedResourceFactory extends LocalFactoryBase implements LocalFactory {

	protected LocalFactory resourceMapFactory = null;
	protected GlobalFactory<?> localeFactory = null;

	public LocalizedResourceFactory(LocalFactory resourceMapFactory, GlobalFactory<?> localeFactory) {
		this.resourceMapFactory = resourceMapFactory;
		this.localeFactory = localeFactory;
	}

	public Class<?> getReturnType() {
		return Object.class;
	}

	@SuppressWarnings("unchecked")
	public Object instance(Object[] parameters, Object[] localProducts) {
		Map<Object, LocalFactory> resourceMap = (Map<Object, LocalFactory>) this.resourceMapFactory.instance(parameters, localProducts);
		Locale locale = (Locale) this.localeFactory.instance(parameters, localProducts);
		if (locale == null) {
			throw new NullPointerException(
					"The 'locale' factory returned null. It must always return a valid Locale instance.");
		}
		// return resourceMap.get(locale);
		return resourceMap.get(locale).instance(parameters, localProducts);
	}

}
