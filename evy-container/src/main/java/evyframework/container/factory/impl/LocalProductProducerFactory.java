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

/**
 * @author Jakob Jenkov - Copyright 2004-2006 Jenkov Development
 */
public class LocalProductProducerFactory extends LocalFactoryBase implements LocalFactory {

	protected LocalFactory instantiationFactory = null;
	protected int index = 0;

	public LocalProductProducerFactory(LocalFactory localProductFactory, int index) {
		if (localProductFactory == null) {
			throw new IllegalArgumentException("Local product factory cannot be null");
		}
		this.instantiationFactory = localProductFactory;
		this.index = index;
	}

	public LocalFactory getInstantiationFactory() {
		return instantiationFactory;
	}

	public int getIndex() {
		return index;
	}

	public Class<?> getReturnType() {
		return this.instantiationFactory.getReturnType();
	}

	public Object instance(Object[] parameters, Object[] localProducts) {
		Object product = this.instantiationFactory.instance(parameters, localProducts);
		localProducts[this.index] = product;
		return product;
	}
}
