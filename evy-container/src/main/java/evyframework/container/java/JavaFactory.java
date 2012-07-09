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

package evyframework.container.java;

import evyframework.container.factory.GlobalFactory;

/**

 */
public class JavaFactory<T> implements GlobalFactory<T> {

	protected Class<T> returnType = null;

	@Override
	public Class<T> getReturnType() {
		return returnType;
	}

	public void setReturnType(Class<T> returnType) {
		this.returnType = returnType;
	}

	@Override
	public T instance(Object... parameters) {
		return null;
	}

	@Override
	public Object[] execPhase(String phase, Object... parameters) {
		return new Object[0];
	}
}
