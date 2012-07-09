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
import evyframework.container.factory.LocalFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**

 */
@SuppressWarnings("rawtypes")
public abstract class GlobalFactoryBase implements GlobalFactory {
	protected LocalFactory localInstantiationFactory = null;
	// protected IGlobalFactory globalInstantiationFactory = null;
	protected Map<String, List<LocalFactory>> phases = new ConcurrentHashMap<String, List<LocalFactory>>();

	protected int localProductCount = 0;

	public void setLocalProductCount(int localProductCount) {
		this.localProductCount = localProductCount;
	}

	public int getLocalProductCount() {
		return localProductCount;
	}

	public LocalFactory getLocalInstantiationFactory() {
		return localInstantiationFactory;
	}

	public void setLocalInstantiationFactory(LocalFactory localInstantiationFactory) {
		this.localInstantiationFactory = localInstantiationFactory;
	}

	// public IGlobalFactory getGlobalInstantiationFactory() {
	// return globalInstantiationFactory;
	// }

	// public void setGlobalInstantiationFactory(IGlobalFactory globalInstantiationFactory) {
	// this.globalInstantiationFactory = globalInstantiationFactory;
	// }
	
	public List<LocalFactory> getPhase(String phase) {
		return this.phases.get(phase);
	}

	public void setPhase(String phase, List<LocalFactory> factories) {
		this.phases.put(phase, factories);
	}

	/**
	 * This method is called by the container when executing a phase in a factory that supports life cycle phases. The
	 * container knows nothing about local products, therefore this method is called. Only the concrete factory knows
	 * about cached local products (if any).
	 * 
	 * <br/>
	 * <br/>
	 * This is the method a global factory will override when implementing life cycle phase behaviour, e.g. for cached
	 * objects.
	 * 
	 * @param phase
	 *            The name of the phase to execute. For instance, "config" or "dispose".
	 * @param parameters
	 *            The parameters passed to the container when the phase begins. For instance to an instance() method
	 *            call, or an execPhase(phase, factory, parameters) call.
	 * @return Null, or the local products the phase ends up being executed on. If executed for several local product
	 *         arrays (e.g. in pools or flyweights), null will be returned, since it does not make sense to return
	 *         anything. Returning anything would only make sense for the "create" phase, but currently this phase does
	 *         not use the execPhase() method to carry out its work. It uses the factory.instance() methods instead.
	 */
	public Object[] execPhase(String phase, Object... parameters) {
		return null;
	}

	/*
	 * { Object[] localProducts = this.localProductCount > 0? new Object[this.localProductCount] : null; return
	 * execPhase(phase, parameters, localProducts); }
	 */

	/**
	 * Executes a life cycle phase on the given local products, using the given input parameters. This method is a
	 * utility method that global factories can use to implement their phase execution.
	 * 
	 * @param phase
	 *            The name of the phase to execute.
	 * @param parameters
	 *            Any input parameters to pass to the phase factory chain.
	 * @param localProducts
	 *            Any local products (typically cached products) to pass to the phase factory chain.
	 * 
	 * @return Null, or the local products the phase ends up being executed on. If executed for several local product
	 *         arrays (e.g. in pools or flyweights), null will be returned, since it does not make sense to return
	 *         anything. Returning anything would only make sense for the "create" phase, but currently this phase does
	 *         not use the execPhase() method to carry out its work. It uses the factory.instance() methods instead.
	 */
	protected Object[] execPhase(String phase, Object[] parameters, Object[] localProducts) {
		List<LocalFactory> phaseFactories = this.phases.get(phase);
		if (phaseFactories != null) {
			for (LocalFactory factory : phaseFactories) {
				factory.instance(parameters, localProducts);
			}
		}
		return localProducts;
	}

}
