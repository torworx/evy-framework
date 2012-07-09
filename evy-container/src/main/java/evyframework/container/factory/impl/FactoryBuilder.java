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
import evyframework.container.factory.support.ParserException;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
@SuppressWarnings("rawtypes")
public class FactoryBuilder {

	// ConstructorFactory methods
	public LocalFactory createConstructorFactory(String className, List<LocalFactory> constructorArgFactories,
			Class[] forcedArgumentTypes) {

		Class theClass = FactoryUtil.getClassForName(className);
		if (theClass == null) {
			throw new ParserException("FactoryBuilder", "CONSTRUCTOR_FACTORY_ERROR",
					"Error creating constructor factory for class " + className
							+ ". The class was not found on the classpath.");
		}
		return createConstructorFactory(theClass, constructorArgFactories, forcedArgumentTypes);
	}

	public LocalFactory createConstructorFactory(Class theClass, List<LocalFactory> constructorArgFactories,
			Class[] forcedArgumentTypes) {
		Constructor constructor = findMatchingConstructor(theClass, constructorArgFactories, forcedArgumentTypes);

		setFactoryInterfacesOnFactoryFactories(constructorArgFactories, constructor.getParameterTypes());
		setCollectionTypesOnFactories(constructorArgFactories, constructor.getParameterTypes(),
				constructor.getGenericParameterTypes());
		wrapInConversionFactoriesIfNecessary(constructorArgFactories, constructor.getParameterTypes());
		return new ConstructorFactory(constructor, constructorArgFactories);
	}

	public LocalFactory createInstanceMethodFactory(String methodName, LocalFactory methodOwnerFactory) {
		return createInstanceMethodFactory(methodName, methodOwnerFactory, new ArrayList<LocalFactory>(),
				FactoryUtil.createEmptyClassArray(0));
	}

	public LocalFactory createInstanceMethodFactory(String methodName, LocalFactory methodOwnerFactory,
			List<LocalFactory> methodArgFactories, Class[] forcedArgumentTypes) {
		if (methodOwnerFactory == null) {
			throw new ParserException(
					"FactoryBuilder",
					"INSTANCE_METHOD_FACTORY_ERROR",
					"No owner found for method "
							+ methodName
							+ ". "
							+ "If "
							+ methodName
							+ " is really a class name and not a method name, most likely the class was not found on the classpath.");
		}
		Method method = findMatchingMethod(methodOwnerFactory.getReturnType(), methodName, methodArgFactories,
				forcedArgumentTypes);
		if (Modifier.isStatic(method.getModifiers())) {
			throw new ParserException("FactoryBuilder", "INSTANCE_METHOD_FACTORY_ERROR", "The method [" + method
					+ "] is static and cannot be called on an instance. ");
		}

		setFactoryInterfacesOnFactoryFactories(methodArgFactories, method.getParameterTypes());
		setCollectionTypesOnFactories(methodArgFactories, method.getParameterTypes(), method.getGenericParameterTypes());
		wrapInConversionFactoriesIfNecessary(methodArgFactories, method.getParameterTypes());

		return new InstanceMethodFactory(method, methodOwnerFactory, methodArgFactories);
	}

	public LocalFactory createStaticMethodFactory(String methodName, String methodOwnerClassName,
			List<LocalFactory> methodArgFactories, Class[] forcedArgumentTypes) {

		Class theClass = FactoryUtil.getClassForName(methodOwnerClassName);
		if (theClass == null) {
			throw new ParserException("FactoryBuilder", "STATIC_METHOD_FACTORY_ERROR",
					"Error creating method factory for method " + methodName + " of class " + methodName
							+ ". The class was not found on the classpath.");
		}
		return createStaticMethodFactory(methodName, theClass, methodArgFactories, forcedArgumentTypes);
	}

	public LocalFactory createStaticMethodFactory(String methodName, Class methodOwner,
			List<LocalFactory> methodArgFactories, Class[] forcedArgumentTypes) {
		Method method = findMatchingMethod(methodOwner, methodName, methodArgFactories, forcedArgumentTypes);

		if ((!Modifier.isStatic(method.getModifiers()))) {
			throw new ParserException("FactoryBuilder", "STATIC_METHOD_FACTORY_ERROR", "The method [" + method
					+ "] is not static. ");
		}

		setFactoryInterfacesOnFactoryFactories(methodArgFactories, method.getParameterTypes());
		setCollectionTypesOnFactories(methodArgFactories, method.getParameterTypes(), method.getGenericParameterTypes());
		wrapInConversionFactoriesIfNecessary(methodArgFactories, method.getParameterTypes());

		return new StaticMethodFactory(method, methodArgFactories);
	}

	// FieldFactory methods

	public LocalFactory createFieldFactory(String fieldName, String fieldOwnerClassName) {
		Class ownerClass = FactoryUtil.getClassForName(fieldOwnerClassName);

		if (ownerClass == null) {
			throw new ParserException("FactoryBuilder", "FIELD_FACTORY_ERROR",
					"Error creating field factory for field " + fieldName + " in class " + fieldOwnerClassName
							+ ". The class was not found on the classpath.");
		}

		if ("class".equals(fieldName)) {
			return new ValueFactory(ownerClass);
		}

		Field field = null;
		try {
			field = ownerClass.getField(fieldName);
		} catch (NoSuchFieldException e) {
			throw new ParserException("FactoryBuilder", "FIELD_FACTORY_ERROR",
					"Error creating field factory for field " + fieldName + " in class " + fieldOwnerClassName
							+ ". The field was not found.", e);
		}

		return new FieldFactory(field);
	}

	public LocalFactory createFieldFactory(String fieldName, LocalFactory fieldOwnerInstantiator) {

		Field field = null;
		try {
			field = fieldOwnerInstantiator.getReturnType().getField(fieldName);
		} catch (NoSuchFieldException e) {
			throw new ParserException("FactoryBuilder", "FIELD_FACTORY_ERROR", "Error creating field factory", e);
		}

		return new FieldFactory(field, fieldOwnerInstantiator);
	}

	public LocalFactory createFieldAssignmentFactory(String fieldName, String fieldOwnerClassName,
			LocalFactory assignmentValueFactory) {
		Class fieldOwnerClass = FactoryUtil.getClassForName(fieldOwnerClassName);

		if (fieldOwnerClass == null) {
			throw new ParserException("FactoryBuilder", "FIELD_ASSIGNMENT_FACTORY_ERROR",
					"Error creating field assignment factory for field " + fieldName + " in class "
							+ fieldOwnerClassName + ". The class was not found on the classpath");
		}

		Field field = null;
		try {
			field = fieldOwnerClass.getField(fieldName);
		} catch (NoSuchFieldException e) {
			throw new ParserException("FactoryBuilder", "FIELD_FACTORY_ERROR",
					"Error creating field assignment factory for field " + fieldName + " in class "
							+ fieldOwnerClassName + ". The field was not found.", e);
		}

		setFactoryInterfaceOnFactoryFactory(assignmentValueFactory, field.getType());
		setCollectionTypesOnFactory(assignmentValueFactory, field.getType(), field.getGenericType());
		return new FieldAssignmentFactory(field, fieldOwnerClass, wrapInConversionFactoryIfNecessary(
				assignmentValueFactory, field.getType()));
	}

	public LocalFactory createFieldAssignmentFactory(String fieldName, LocalFactory fieldAssignmentTargetFactory,
			LocalFactory assignmentValueFactory) {
		Field field = null;
		try {
			field = fieldAssignmentTargetFactory.getReturnType().getField(fieldName);
		} catch (NoSuchFieldException e) {
			throw new ParserException("FactoryBuilder", "FIELD_FACTORY_ERROR",
					"Error creating field assignment factory", e);
		}

		setFactoryInterfaceOnFactoryFactory(assignmentValueFactory, field.getType());
		setCollectionTypesOnFactory(assignmentValueFactory, field.getType(), field.getGenericType());
		return new FieldAssignmentFactory(field, fieldAssignmentTargetFactory, wrapInConversionFactoryIfNecessary(
				assignmentValueFactory, field.getType()));
	}

	private Constructor findMatchingConstructor(Class theClass, List<LocalFactory> constructorArgFactories,
			Class[] forcedArgumentTypes) {
		Class[] factoryReturnTypes = FactoryUtil.factoriesToArgumentTypeArray(constructorArgFactories);
		return findMatchingConstructor(theClass, factoryReturnTypes, forcedArgumentTypes);
	}

	private Constructor findMatchingConstructor(Class theClass, Class[] factoryReturnTypes, Class[] forcedArgumentTypes) {
		if (factoryReturnTypes.length != forcedArgumentTypes.length) {
			throw new IllegalArgumentException(
					"The factoryReturnTypes and the forcedArgumentTypes arrays must be of equal length.");
		}
		Constructor[] allConstructors = theClass.getConstructors();
		Set<Constructor> bestMatches = new HashSet<Constructor>();
		int bestMatchLevel = NO_MATCH;

		for (Constructor constructor : allConstructors) {
			Class[] candidateArgTypes = constructor.getParameterTypes();
			if (candidateArgTypes.length != factoryReturnTypes.length)
				continue;

			int candidateMatchLevel = argumentMatch(candidateArgTypes, factoryReturnTypes, forcedArgumentTypes);

			if (candidateMatchLevel == NO_MATCH) {
				// do nothing, ignore constructor
			} else if (candidateMatchLevel > bestMatchLevel) {
				bestMatches.clear();
				bestMatches.add(constructor);
				bestMatchLevel = candidateMatchLevel;
			} else if (candidateMatchLevel == bestMatchLevel) {
				bestMatches.add(constructor);
			}
		}

		if (bestMatches.size() == 0) {
			throw new ParserException("FactoryBuilder", "CONSTRUCTOR_FACTORY_ERROR", "No constructors in " + theClass
					+ " matched the return types of the argument factories: " + toClassNames(factoryReturnTypes));
		} else if (bestMatches.size() > 1) {
			throw new ParserException("FactoryBuilder", "CONSTRUCTOR_FACTORY_ERROR", "More than one constructor in "
					+ theClass + " matched the return types of the argument factories:"
					+ toClassNames(factoryReturnTypes));
		}

		return bestMatches.iterator().next();
	}

	private Class[] toClassNames(Class[] factoryReturnTypes) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < factoryReturnTypes.length; i++) {
			builder.append(factoryReturnTypes[i] != null ? factoryReturnTypes[i].getName() : "null");
			if (i < factoryReturnTypes.length - 1) {
				builder.append(", ");
			}
		}
		return factoryReturnTypes;
	}

	private Method findMatchingMethod(Class methodOwner, String methodName, List<LocalFactory> methodArgFactories,
			Class[] forcedArgumentTypes) {
		return findMatchingMethod(methodOwner, methodName,
				FactoryUtil.factoriesToArgumentTypeArray(methodArgFactories), forcedArgumentTypes);
	}

	private Method findMatchingMethod(Class methodOwner, String methodName, Class[] argumentFactoryReturnTypes,
			Class[] forcedArgumentTypes) {
		if (argumentFactoryReturnTypes.length != forcedArgumentTypes.length) {
			throw new IllegalArgumentException(
					"The argumentFactoryReturnTypes and the forcedArgumentTypes arrays must be of equal length.");
		}
		if (methodOwner == null) {
			throw new IllegalArgumentException("The methodOwner (class) parameter for method " + methodName
					+ " was null!");
		}

		Method[] allMethods = methodOwner.getMethods();
		Set<Method> bestMatches = new HashSet<Method>();
		int bestMatchLevel = NO_MATCH;

		for (Method method : allMethods) {
			if (!method.getName().equals(methodName))
				continue;
			Class[] candidateArgTypes = method.getParameterTypes();
			if (candidateArgTypes.length != argumentFactoryReturnTypes.length)
				continue;

			int candidateMatchLevel = argumentMatch(candidateArgTypes, argumentFactoryReturnTypes, forcedArgumentTypes);
			if (candidateMatchLevel == NO_MATCH) {
				// do nothing, ignore it.
			} else if (candidateMatchLevel > bestMatchLevel) {
				bestMatches.clear();
				bestMatches.add(method);
				bestMatchLevel = candidateMatchLevel;
			} else if (candidateMatchLevel == bestMatchLevel && bestMatchLevel != NO_MATCH) {
				bestMatches.add(method);
			}
		}

		if (bestMatches.size() > 1) {
			throw new ParserException("FactoryBuilder", "METHOD_FACTORY_ERROR",
					"More than one method matches signature "
							+ createMethodSignature(methodName, argumentFactoryReturnTypes, forcedArgumentTypes)
							+ " in class " + methodOwner);
		}

		if (bestMatchLevel == NO_MATCH) {
			throw new ParserException("FactoryBuilder", "METHOD_FACTORY_ERROR", "No methods matches signature "
					+ createMethodSignature(methodName, argumentFactoryReturnTypes, forcedArgumentTypes) + " in class "
					+ methodOwner);
		}

		return bestMatches.iterator().next();
	}

	private static final int DIRECT_MATCH = 5;
	private static final int UP_CAST_MATCH = 4;
	private static final int CONVERSION_MATCH = 3;
	private static final int DOWN_CAST_MATCH = 2;
	private static final int NULL_MATCH = 1;
	private static final int NO_MATCH = 0;

	public int argumentMatch(Class[] candidateArgTypes, Class[] factoryReturnTypes, Class[] forcedArgumentTypes) {

		if (candidateArgTypes.length != factoryReturnTypes.length)
			return NO_MATCH;
		if (candidateArgTypes.length == 0)
			return DIRECT_MATCH;

		int argumentMatch = DIRECT_MATCH;

		for (int i = 0; i < candidateArgTypes.length; i++) {

			// check forced argument type match
			if (forcedArgumentTypes[i] != null && !forcedArgumentTypes[i].equals(candidateArgTypes[i])) {
				argumentMatch = NO_MATCH;
				break;
			}

			// check for null factory return type... null == always a match
			if (factoryReturnTypes[i] == null) {
				if (argumentMatch >= DOWN_CAST_MATCH) {
					argumentMatch = NULL_MATCH;
				}
				continue;
			}

			// check direct match
			if (factoryReturnTypes[i].equals(candidateArgTypes[i])) {
				continue; // this argument matches directly, no reason to change the argumentMatch variable.
			}

			// check up cast match
			if (FactoryUtil.isSubstitutableFor(factoryReturnTypes[i], candidateArgTypes[i])) {
				if (argumentMatch == DIRECT_MATCH) {
					argumentMatch = UP_CAST_MATCH;
				}
				continue;
			}

			// check conversion match
			if (FactoryUtil.isConvertibleTo(factoryReturnTypes[i], candidateArgTypes[i])) {
				if (argumentMatch >= UP_CAST_MATCH) {
					argumentMatch = CONVERSION_MATCH;
				}
				continue;
			}

			// check down cast match
			if (FactoryUtil.isDownCastableTo(factoryReturnTypes[i], candidateArgTypes[i])) {
				if (argumentMatch >= CONVERSION_MATCH) {
					argumentMatch = DOWN_CAST_MATCH;
				}
				continue;
			}
			argumentMatch = NO_MATCH;
			break;
		}
		return argumentMatch;
	}

	private String createMethodSignature(String methodName, Class[] argumentFactoryReturnTypes,
			Class[] forcedArgumentTypes) {
		StringBuilder methodSignature = new StringBuilder();
		methodSignature.append(methodName);
		methodSignature.append("(");
		for (int i = 0; i < argumentFactoryReturnTypes.length; i++) {
			if (argumentFactoryReturnTypes[i] != null) {
				methodSignature.append(argumentFactoryReturnTypes[i].getName());
			} else {
				methodSignature.append("unknown");
			}

			if (i < argumentFactoryReturnTypes.length - 1) {
				methodSignature.append(", ");
			}
		}
		methodSignature.append(")");

		return methodSignature.toString();
	}

	public void wrapInConversionFactoriesIfNecessary(List<LocalFactory> argumentFactories, Class[] argumentTypes) {
		for (int i = 0; i < argumentTypes.length; i++) {
			Class factoryReturnType = argumentFactories.get(i).getReturnType();

			// a null type means an input parameter. The type is not known until creation time when the input is
			// passed to the container.instance("name", ...) method.
			if (factoryReturnType == null)
				continue;

			if (!argumentTypes[i].equals(factoryReturnType)
					&& !FactoryUtil.isSubstitutableFor(factoryReturnType, argumentTypes[i])) {
				LocalFactory conversionFactory = FactoryUtil.createConversionFactory(argumentFactories.get(i),
						argumentTypes[i]);
				argumentFactories.set(i, conversionFactory);
			}
		}
	}

	/**
	 * This method is public because it is called from inside CollectionFactory, when determining if the collection
	 * content factories need to be wrapped in conversion factories.
	 * 
	 * @param argumentFactory
	 * @param argumentType
	 * @return The wrapped factory.
	 */
	public LocalFactory wrapInConversionFactoryIfNecessary(LocalFactory argumentFactory, Class argumentType) {
		Class factoryReturnType = argumentFactory.getReturnType();
		if (factoryReturnType == null)
			return argumentFactory;

		if (!argumentType.equals(factoryReturnType) && !FactoryUtil.isSubstitutableFor(factoryReturnType, argumentType)) {
			LocalFactory conversionFactory = FactoryUtil.createConversionFactory(argumentFactory, argumentType);
			return conversionFactory;
		}
		return argumentFactory;
	}

	private void setFactoryInterfacesOnFactoryFactories(List<LocalFactory> argumentFactories, Class[] parameterTypes) {
		for (int i = 0; i < parameterTypes.length; i++) {
			if (argumentFactories.get(i) instanceof FactoryFactory) {
				FactoryFactory factory = (FactoryFactory) argumentFactories.get(i);
				factory.setCustomFactoryInterface(parameterTypes[i]);
			}
		}
	}

	private LocalFactory setFactoryInterfaceOnFactoryFactory(LocalFactory argumentFactory, Class customFactoryInterface) {
		if (argumentFactory instanceof FactoryFactory) {
			FactoryFactory factory = (FactoryFactory) argumentFactory;
			factory.setCustomFactoryInterface(customFactoryInterface);
		}

		return argumentFactory;
	}

	private void setCollectionTypesOnFactories(List<LocalFactory> argumentFactories, Class[] argumentTypes,
			Type[] genericParameterTypes) {
		for (int i = 0; i < genericParameterTypes.length; i++) {
			if (argumentFactories.get(i) instanceof CollectionFactory) {
				setCollectionTypesOnFactory(argumentFactories.get(i), argumentTypes[i], genericParameterTypes[i]);
			}
		}
	}

	private LocalFactory setCollectionTypesOnFactory(LocalFactory argumentFactory, Class argumentType, Type genericType) {
		if (argumentFactory instanceof CollectionFactory) {
			CollectionFactory collectionFactory = (CollectionFactory) argumentFactory;
			collectionFactory.setCollectionType(argumentType, genericType);
		}
		return argumentFactory;
	}

}
