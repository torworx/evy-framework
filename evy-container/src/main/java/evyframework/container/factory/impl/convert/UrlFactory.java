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

package evyframework.container.factory.impl.convert;

import evyframework.container.factory.FactoryException;
import evyframework.container.factory.LocalFactory;
import evyframework.container.factory.impl.LocalFactoryBase;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public class UrlFactory extends LocalFactoryBase implements LocalFactory {

	protected LocalFactory sourceFactory = null;

	public UrlFactory(LocalFactory sourceFactory) {
		this.sourceFactory = sourceFactory;
	}

	public Class<?> getReturnType() {
		return URL.class;
	}

	public Object instance(Object[] parameters, Object[] localProducts) {
		Object urlSource = null;
		try {
			urlSource = this.sourceFactory.instance(parameters, localProducts);
			return new URL(urlSource.toString());
		} catch (MalformedURLException e) {
			throw new FactoryException("UrlFactory", "INVALID_URL", "Error creating URL from object: " + urlSource, e);
		}
	}
}
