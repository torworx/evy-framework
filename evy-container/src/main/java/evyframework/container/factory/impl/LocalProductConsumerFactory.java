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
public class LocalProductConsumerFactory extends LocalFactoryBase implements LocalFactory {

	protected int index = 0;
	protected Class<?> returnType = null;

	public LocalProductConsumerFactory(Class<?> returnType, int index) {
		this.returnType = returnType;
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public Class<?> getReturnType() {
		return returnType;
	}

	public void setReturnType(Class<?> returnType) {
		this.returnType = returnType;
	}

	public Object instance(Object[] parameters, Object[] localProducts) {
		return localProducts[this.index];
	}

}
