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

/**
 * 
 * @author Jakob Jenkov - Copyright 2004-2006 Jenkov Development
 */
@SuppressWarnings("rawtypes")
public class GlobalNewInstanceFactory extends GlobalFactoryBase implements GlobalFactory {

	public Class getReturnType() {
		return getLocalInstantiationFactory().getReturnType();
	}

	public Object instance(Object... parameters) {
		Object[] localProducts = getLocalProductCount() > 0 ? new Object[getLocalProductCount()] : null;
		return instance(parameters, localProducts);
	}

	/* todo remove this method, because it will only be called from the instance() method above */
	public Object instance(Object[] parameters, Object[] localProducts) {
		Object instance = getLocalInstantiationFactory().instance(parameters, localProducts);
		if (localProducts != null)
			localProducts[0] = instance;

		execPhase("config", parameters, localProducts);

		return instance;
	}

	public Object[] execPhase(String phase, Object... parameters) {
		return null;
	}

}
