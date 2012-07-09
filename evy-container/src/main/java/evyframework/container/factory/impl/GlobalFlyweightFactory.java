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

import java.util.HashMap;
import java.util.Map;

/**
 * todo fix phase execution for local products
 * @author Jakob Jenkov - Copyright 2004-2006 Jenkov Development
 */
@SuppressWarnings("rawtypes")
public class GlobalFlyweightFactory extends GlobalFactoryBase implements GlobalFactory {

    public Map<FlyweightKey, Object[]> localProducts = new HashMap<FlyweightKey, Object[]>();

    public Class getReturnType() {
        return getLocalInstantiationFactory().getReturnType();
    }

    public Object instance(Object ... parameters) {
        FlyweightKey key = new FlyweightKey(parameters);
        Object[] keyLocalProducts = this.localProducts.get(key);

        if(keyLocalProducts != null) return keyLocalProducts[0];
        keyLocalProducts = new Object[getLocalProductCount()];
        return instance(parameters, keyLocalProducts);
    }

    public synchronized Object instance(Object[] parameters, Object[] localProducts) {
        FlyweightKey key = new FlyweightKey(parameters);
        Object[] keyLocalProducts = this.localProducts.get(key);
        if(keyLocalProducts == null){
            keyLocalProducts = localProducts;
            keyLocalProducts[0] = getLocalInstantiationFactory().instance(parameters, localProducts);
            this.localProducts.put(key, keyLocalProducts);
            execPhase("config", parameters, keyLocalProducts);
        }
        return keyLocalProducts[0];
    }

    public Object[] execPhase(String phase, Object ... parameters) {
        for(FlyweightKey key : localProducts.keySet()){
            Object[] keyLocalProducts = localProducts.get(key);
            execPhase(phase, parameters, keyLocalProducts);
        }
        return null;
    }

    public String toString() {
        return "<GlobalFlyweightFactory> --> "+ getLocalInstantiationFactory().toString();
    }

}
