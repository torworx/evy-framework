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

import java.lang.reflect.Field;

/**
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public class FieldFactory extends LocalFactoryBase implements LocalFactory {
	protected Field field = null;
	protected Object fieldOwner = null;
	protected LocalFactory fieldOwnerFactory = null;

	public FieldFactory(Field method) {
		this.field = method;
	}

	public FieldFactory(Field field, Object fieldOwner) {
		this.field = field;
		this.fieldOwner = fieldOwner;
	}

	public FieldFactory(Field field, LocalFactory fieldOwnerFactory) {
		this.field = field;
		this.fieldOwnerFactory = fieldOwnerFactory;
	}

	public Class<?> getReturnType() {
		return this.field.getType();
	}

	public Object instance(Object[] parameters, Object[] localProducts) {
		try {
			if (this.fieldOwnerFactory != null) {
				return this.field.get(this.fieldOwnerFactory.instance(parameters, localProducts));
			}
			return this.field.get(this.fieldOwner);
		} catch (IllegalAccessException e) {
			throw new FactoryException("FieldFactory", "ERROR_ACCESSING_FIELD", "Error accessing field " + field, e);
		}
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("field: ");
		builder.append(field);

		return builder.toString();
	}

}
