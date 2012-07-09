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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**

 */
public class ConstructorFactory extends LocalFactoryBase implements LocalFactory {

	protected Constructor<?> constructor = null;
	protected List<LocalFactory> constructorArgFactories = new ArrayList<LocalFactory>();

	public ConstructorFactory(Constructor<?> constructor) {
		setConstructor(constructor);
	}

	public ConstructorFactory(Constructor<?> constructor, List<LocalFactory> contructorArgFactories) {
		setConstructor(constructor);
		this.constructorArgFactories = contructorArgFactories;
	}

	public ConstructorFactory(Constructor<?> constructor, LocalFactory[] constructorArgFactories) {
		setConstructor(constructor);
		for (LocalFactory factory : constructorArgFactories) {
			this.constructorArgFactories.add(factory);
		}
	}

	public void setConstructor(Constructor<?> constructor) {
		this.constructor = constructor;
	}

	public Constructor<?> getConstructor() {
		return constructor;
	}

	public List<LocalFactory> getConstructorArgFactories() {
		return constructorArgFactories;
	}

	public Class<?> getReturnType() {
		return this.constructor.getDeclaringClass();
	}

	public Object instance(Object[] parameters, Object[] localProducts) {
		Object[] arguments = FactoryUtil.toArgumentArray(this.constructorArgFactories, parameters, localProducts);

		Object returnValue = null;
		try {
			returnValue = this.constructor.newInstance(arguments);
		} catch (Throwable t) {
			throw new FactoryException("ConstructorFactory", "CONSTRUCTOR_EXCEPTION",
					"Error instantiating object from constructor " + this.constructor, t);
		} finally {
			for (int j = 0; j < arguments.length; j++)
				arguments[j] = null;
		}

		return returnValue;
	}

	public String toString() {
		return "<ConstructorFactory : " + getReturnType() + ">";
	}

}
