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

 */
public class FieldAssignmentFactory extends LocalFactoryBase implements LocalFactory {

	protected Field field = null;
	protected Class<?> fieldOwningClass = null;
	protected LocalFactory fieldAssignmentTargetFactory = null;
	protected LocalFactory assignmentValueFactory = null;

	public FieldAssignmentFactory(Field field, LocalFactory assignmentTargetFactory, LocalFactory assignmentValueFactory) {
		this.field = field;
		this.fieldAssignmentTargetFactory = assignmentTargetFactory;
		this.assignmentValueFactory = assignmentValueFactory;
	}

	public FieldAssignmentFactory(Field field, Class<?> fieldOwningClass, LocalFactory assignmentValueFactory) {
		this.field = field;
		this.fieldOwningClass = fieldOwningClass;
		this.assignmentValueFactory = assignmentValueFactory;
	}

	public Class<?> getReturnType() {
		return this.field.getType();
	}

	/* todo clean up this method. Field can never be void return types. Fields always have a type. */
	public Object instance(Object[] parameters, Object[] localProducts) {
		Object value = this.assignmentValueFactory.instance(parameters, localProducts);
		try {
			if (isInstanceField()) {
				field.set(this.fieldAssignmentTargetFactory.instance(parameters, localProducts), value);
				return value;
			}

			field.set(null, value);
			return value;
		} catch (Throwable t) {
			throw new FactoryException("FieldAssignmentFactory", "ERROR_FLYWEIGHT_KEY_PARAMETER",
					"Error setting field value " + value + " on field " + this.field, t);
		}
	}

	private boolean isInstanceField() {
		return this.fieldAssignmentTargetFactory != null;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("<FieldAssignmentFactory: ");
		builder.append(field);
		builder.append("> --> ");
		if (isInstanceField()) {
			builder.append(this.fieldAssignmentTargetFactory);
		} else {
			builder.append("<");
			builder.append(this.fieldOwningClass);
			builder.append(">");
		}

		return builder.toString();
	}

	// this method is added only for testability. It is not part of the IFactory interface.
	public LocalFactory getFieldAssignmentTargetFactory() {
		return fieldAssignmentTargetFactory;
	}

	public Field getField() {
		return field;
	}

	@SuppressWarnings("unused")
	private boolean isVoidReturnType() {
		return void.class.equals(this.field.getType()) || this.field.getType() == null;
	}

}
