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
 * This class is a local singleton factory.
 * 
 * Local singletons do not have their own life cycle phases. The products managed by a local singleton will share life
 * cycle phases with the global factory they are part of. If you need to manage the life cycle of a local singleton,
 * make it a named local factory, and reference the named local product (the singleton instance) from the global
 * factory's life cycle phases.
 * 
 * @author Jakob Jenkov - Copyright 2004-2006 Jenkov Development
 */
public class LocalSingletonFactory extends LocalFactoryBase implements LocalFactory {

	protected LocalFactory sourceFactory = null;
	protected Object instance = null;

	public LocalSingletonFactory(LocalFactory sourceFactory) {
		this.sourceFactory = sourceFactory;
	}

	public Class<?> getReturnType() {
		return this.sourceFactory.getReturnType();
	}

	public synchronized Object instance(Object[] parameters, Object[] localProducts) {
		if (this.instance == null) {
			this.instance = this.sourceFactory.instance(parameters, localProducts);
		}
		return this.instance;
	}

}
