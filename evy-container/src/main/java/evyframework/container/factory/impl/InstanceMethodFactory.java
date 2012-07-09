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
import java.util.List;
import java.util.ArrayList;

/**

 */
public class InstanceMethodFactory extends LocalFactoryBase implements LocalFactory {

	protected Method method = null;
	protected LocalFactory methodInvocationTargetFactory = null;
	protected List<LocalFactory> methodArgFactories = new ArrayList<LocalFactory>();

	public InstanceMethodFactory(Method method, LocalFactory methodInvocationTargetFactory,
			List<LocalFactory> methodArgFactories) {
		if (method == null)
			throw new IllegalArgumentException("Method cannot be null");
		if (methodInvocationTargetFactory == null)
			throw new IllegalArgumentException("Method invocation target cannot be null");
		this.method = method;
		this.methodInvocationTargetFactory = methodInvocationTargetFactory;
		this.methodArgFactories = methodArgFactories;
	}

	public Class<?> getReturnType() {
		// if a method returns void, it should return the invocation target instead, enabling method chaining on methods
		// returning void.
		if (isVoidReturnType()) {
			return methodInvocationTargetFactory.getReturnType();
		}

		return this.method.getReturnType();
	}

	public Object instance(Object[] parameters, Object[] localProducts) {
		Object[] arguments = FactoryUtil.toArgumentArray(this.methodArgFactories, parameters, localProducts);
		try {
			Object target = this.methodInvocationTargetFactory.instance(parameters, localProducts);
			if (target == null) {
				throw new NullPointerException("The object call the method " + method.toString() + " on was null");
			}
			Object returnValue = method.invoke(target, arguments);

			// if a method returns void, it should return the invocation target instead, enabling method chaining on
			// methods returning void.
			if (isVoidReturnType()) {
				return target;
			} else {
				return returnValue;
			}

		} catch (Throwable t) {
			throw new FactoryException("InstanceMethodFactory", "INSTANTIATION_ERROR",
					"Error instantiating object from instance method [" + this.method + "]", t);
		} finally {
			for (int j = 0; j < arguments.length; j++)
				arguments[j] = null;
		}
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("<InstanceMethodFactory: ");
		builder.append(method);
		builder.append("> --> ");

		builder.append(this.methodInvocationTargetFactory);

		return builder.toString();
	}

	// this method is added only for testability. It is not part of the IFactory interface.
	public LocalFactory getMethodInvocationTargetFactory() {
		return methodInvocationTargetFactory;
	}

	public Method getMethod() {
		return method;
	}

	private boolean isVoidReturnType() {
		return void.class.equals(this.method.getReturnType()) || this.method.getReturnType() == null;
	}

}
