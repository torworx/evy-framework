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

import evyframework.container.factory.FactoryException;
import evyframework.container.factory.LocalFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**

 */
public class StaticMethodFactory extends LocalFactoryBase implements LocalFactory {

	protected Method method = null;
	protected List<LocalFactory> methodArgFactories = new ArrayList<LocalFactory>();

	public StaticMethodFactory(Method method, List<LocalFactory> methodArgFactories) {
		if (method == null)
			throw new IllegalArgumentException("Method cannot be null");
		this.method = method;
		this.methodArgFactories = methodArgFactories;
	}

	public Class<?> getReturnType() {
		// if a method returns void, it should return the invocation target instead, enabling method chaining on methods
		// returning void.
		if (isVoidReturnType()) {
			return Class.class;
		}

		return this.method.getReturnType();
	}

	public Object instance(Object[] parameters, Object[] localProducts) {
		Object[] arguments = FactoryUtil.toArgumentArray(this.methodArgFactories, parameters, localProducts);
		try {
			// if a method returns void, it should return the invocation target instead, enabling method chaining on
			// methods returning void.
			Object returnValue = method.invoke(null, arguments);
			if (isVoidReturnType()) {
				return null;
			}

			return returnValue;
		} catch (NullPointerException e) {
			throw new FactoryException("StaticMethodFactory", "INSTANTIATION_ERROR",
					"Error instantiating object from static method [" + this.method
							+ "]. Are you sure the method is declared static?", e);
		} catch (Throwable t) {
			throw new FactoryException("StaticMethodFactory", "INSTANTIATION_ERROR",
					"Error instantiating object from static method [" + this.method + "]", t);
		} finally {
			// for(int j=0; j<arguments.length; j++)arguments[j] = null;
		}
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("<StaticMethodFactory: ");
		builder.append(method);
		builder.append("> --> ");

		return builder.toString();
	}

	public Method getMethod() {
		return method;
	}

	private boolean isVoidReturnType() {
		return void.class.equals(this.method.getReturnType()) || this.method.getReturnType() == null;
	}

}