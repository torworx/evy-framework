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

import evyframework.container.factory.GlobalFactory;

/**

 */
@SuppressWarnings("rawtypes")
public class GlobalFactoryProxy implements GlobalFactory {

	protected GlobalFactory delegateFactory = null;

	public GlobalFactoryProxy(GlobalFactory delegateFactory) {
		this.delegateFactory = delegateFactory;
	}

	public synchronized GlobalFactory getDelegateFactory() {
		return delegateFactory;
	}

	public synchronized GlobalFactory setDelegateFactory(GlobalFactory delegateFactory) {
		GlobalFactory oldFactory = this.delegateFactory;
		this.delegateFactory = delegateFactory;
		return oldFactory;
	}

	public synchronized Class getReturnType() {
		return this.delegateFactory.getReturnType();
	}

	public Object instance(Object... parameters) {
		GlobalFactory localDelegateFactory = null;
		synchronized (this) {
			localDelegateFactory = this.delegateFactory;
		}
		return localDelegateFactory.instance(parameters);
	}

	public Object[] execPhase(String phase, Object... parameters) {
		GlobalFactory localDelegateFactory = null;
		synchronized (this) {
			localDelegateFactory = this.delegateFactory;
		}
		return localDelegateFactory.execPhase(phase, parameters);
	}
}
