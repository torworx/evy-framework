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
 * Extend this factory when implementing custom factories. Extending this class will reduce the risk of a faulty
 * implementation. Since the class is abstract the compiler will help spotting wrong implementations.
 */
public abstract class LocalFactoryBase implements LocalFactory {

	public abstract Class<?> getReturnType();

	public abstract Object instance(Object[] parameters, Object[] localProducts);
}
