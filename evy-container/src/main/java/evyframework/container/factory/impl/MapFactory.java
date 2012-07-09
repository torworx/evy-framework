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

import evyframework.container.factory.LocalFactory;

import java.util.*;

/**

 */
public class MapFactory extends LocalFactoryBase implements LocalFactory {

	protected List<LocalFactory> keyFactories = null;
	protected List<LocalFactory> valueFactories = null;

	/** If true, produces a map of the product factories instead of the products. */
	protected boolean isFactoryMap = false;

	public MapFactory(List<LocalFactory> keyFactories, List<LocalFactory> valueFactories) {
		this.keyFactories = keyFactories;
		this.valueFactories = valueFactories;
	}

	public Class<?> getReturnType() {
		return Map.class;
	}

	public synchronized boolean isFactoryMap() {
		return isFactoryMap;
	}

	public synchronized void setFactoryMap(boolean factoryMap) {
		isFactoryMap = factoryMap;
	}

	public Object instance(Object[] parameters, Object[] localProducts) {
		Map<Object, Object> map = new HashMap<Object, Object>();

		Iterator<LocalFactory> keyFactoryIterator = keyFactories.iterator();
		Iterator<LocalFactory> valueFactoryIterator = valueFactories.iterator();
		if (!isFactoryMap()) {
			while (keyFactoryIterator.hasNext()) {
				LocalFactory keyFactory = keyFactoryIterator.next();
				LocalFactory valueFactory = valueFactoryIterator.next();
				map.put(keyFactory.instance(parameters, localProducts),
						valueFactory.instance(parameters, localProducts));
			}
		} else {
			while (keyFactoryIterator.hasNext()) {
				LocalFactory keyFactory = keyFactoryIterator.next();
				LocalFactory valueFactory = valueFactoryIterator.next();
				map.put(keyFactory.instance(parameters, localProducts), valueFactory);
			}
		}
		return map;
	}
}