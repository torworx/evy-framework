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

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**

 */
public class CollectionFactory extends LocalFactoryBase implements LocalFactory {

	public enum CollectionKind {
		ARRAY, LIST, SET;
	}

	protected CollectionKind collectionKind = CollectionKind.LIST;
	protected Class<?> collectionArgumentType = null;
	protected Type collectionGenericType = String.class;
	protected Type collectionRawType = null;
	protected Class<?> collectionElementType = Object.class;

	protected List<LocalFactory> collectionContentFactories = null;

	public void setCollectionType(Class<?> argumentType, Type genericType) {
		this.collectionArgumentType = argumentType;
		this.collectionGenericType = genericType;

		if (genericType instanceof ParameterizedType) {
			ParameterizedType parameterizedCollectionType = (ParameterizedType) genericType;
			this.collectionRawType = parameterizedCollectionType.getRawType();
			this.collectionElementType = (Class<?>) parameterizedCollectionType.getActualTypeArguments()[0];
		}

		if (argumentType.isArray()) {
			this.collectionKind = CollectionKind.ARRAY;
			this.collectionElementType = this.collectionArgumentType.getComponentType();
		} else if (FactoryUtil.isSubstitutableFor(argumentType, List.class)) {
			this.collectionKind = CollectionKind.LIST;
		} else if (FactoryUtil.isSubstitutableFor(argumentType, Set.class)) {
			this.collectionKind = CollectionKind.SET;
		} else if (FactoryUtil.isSubstitutableFor(argumentType, Collection.class)) {
			this.collectionKind = CollectionKind.LIST;
		}

		/*
		 * todo move this code to wrapInConversionFactoryIfNecessary, if possible. Then the factory knows as little as
		 * possible about parsing
		 */
		FactoryBuilder builder = new FactoryBuilder();
		for (int i = 0; i < this.collectionContentFactories.size(); i++) {
			this.collectionContentFactories.set(i, builder.wrapInConversionFactoryIfNecessary(
					this.collectionContentFactories.get(i), this.collectionElementType));
		}

	}

	public CollectionFactory(List<LocalFactory> collectionContentFactories) {
		this.collectionContentFactories = collectionContentFactories;
	}

	public Class<?> getReturnType() {
		return this.collectionArgumentType;
	}

	public Object instance(Object[] parameters, Object[] localProducts) {
		if (this.collectionKind == CollectionKind.ARRAY) {
			Object array = Array.newInstance(this.collectionArgumentType.getComponentType(),
					this.collectionContentFactories.size());
			for (int i = 0; i < this.collectionContentFactories.size(); i++) {
				Array.set(array, i, this.collectionContentFactories.get(i).instance(parameters, localProducts));
			}
			return array;
		} else if (this.collectionKind == CollectionKind.LIST) {
			List<Object> list = new ArrayList<Object>();
			for (LocalFactory contentFactory : this.collectionContentFactories) {
				list.add(contentFactory.instance(parameters, localProducts));
			}
			return list;
		} else if (this.collectionKind == CollectionKind.SET) {
			Set<Object> set = new HashSet<Object>();
			for (LocalFactory contentFactory : this.collectionContentFactories) {
				set.add(contentFactory.instance(parameters, localProducts));
			}
			return set;
		}

		return null;
	}
}
