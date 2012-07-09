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

/**

 */
@SuppressWarnings("rawtypes")
public class InputAdaptingFactory extends LocalFactoryBase implements LocalFactory {
    
	GlobalFactory targetFactory = null;
    List<LocalFactory> inputFactories = null;

    public InputAdaptingFactory(GlobalFactory targetFactory, List<LocalFactory> inputFactories) {
        this.targetFactory  = targetFactory;
        this.inputFactories = inputFactories;
    }

    public Class<?> getReturnType() {
        return this.targetFactory.getReturnType();
    }

    public Object instance(Object[] parameters, Object[] localProducts) {
        Object[] adaptedParameters = new Object[inputFactories.size()];
        for(int i=0; i<adaptedParameters.length; i++){
            adaptedParameters[i] = this.inputFactories.get(i).instance(parameters, localProducts);
        }
        //here we do NOT want to pass the localProducts on to the global factory called.
        // The called factory will create its own
        //array of local products and pass around internally. To the called global factory this
        //call looks like a call directly from the container, or directly to the instance(Object[] parameters) method.
        return this.targetFactory.instance(adaptedParameters);
    }
}
