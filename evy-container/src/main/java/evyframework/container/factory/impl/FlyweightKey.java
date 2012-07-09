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

/**
 * @author Jakob Jenkov - Copyright 2004-2006 Jenkov Development
 */
public class FlyweightKey {
        Object[] parameters = null;
        int hashCode = 0;

        public FlyweightKey(Object[] parameters) {
            this.parameters = parameters;
            for(int i=0; i< parameters.length; i++){
                if(parameters[i] == null) throw new FactoryException(
                        "FlyweightKey", "ERROR_FLYWEIGHT_KEY_PARAMETER",
                        "Flyweight parameter " + i + " was null. You cannot use null parameters with flyweight factories");
                this.hashCode *= parameters[i].hashCode();
            }
        }

        public Object[] getParameters() {
            return parameters;
        }

        public boolean equals(Object otherKeyObj){
            FlyweightKey otherKey = (FlyweightKey) otherKeyObj;
            if(otherKey == null) return false;

            if(parameters.length != otherKey.getParameters().length) return false;

            for(int i=0; i<parameters.length; i++){
                if(!this.parameters[i].equals(otherKey.getParameters()[i])) return false;
            }
            return true;
        }

        public int hashCode(){
            return this.hashCode;
        }
    }
