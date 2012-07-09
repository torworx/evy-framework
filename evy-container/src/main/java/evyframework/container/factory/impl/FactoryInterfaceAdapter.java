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

import evyframework.container.Container;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**

 */
public class FactoryInterfaceAdapter implements InvocationHandler {

    Container          container                    = null;
    Map<Method, String> instanceMethodFactoryNameMap = new HashMap<Method, String>();
    String              defaultFactoryName           = null;

    public FactoryInterfaceAdapter(Container container, String defaultFactoryName) {
        this.container = container;
        this.defaultFactoryName = defaultFactoryName;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String factoryName = null;
        synchronized(this.instanceMethodFactoryNameMap){
            factoryName = this.instanceMethodFactoryNameMap.get(method);
            if(factoryName == null){
                String methodName = method.getName();
                if(methodName.endsWith("Instance") || methodName.endsWith("instance")){
                    factoryName = methodName.substring(0, methodName.length() - "Instance".length());
                } else {
                    factoryName = methodName;
                }
                if(factoryName.length() == 0) factoryName = defaultFactoryName;
                this.instanceMethodFactoryNameMap.put(method, factoryName);
            }
        }
        return this.container.getInstance(factoryName, args);
    }
}
