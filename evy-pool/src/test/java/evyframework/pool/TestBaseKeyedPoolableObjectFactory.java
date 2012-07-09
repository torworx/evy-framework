/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package evyframework.pool;

import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import evyframework.pool.BaseKeyedPoolableObjectFactory;
import evyframework.pool.KeyedPoolableObjectFactory;

/**
 * @version $Revision: 1333195 $
 */
public class TestBaseKeyedPoolableObjectFactory {

    @Test
    public void testDefaultMethods() throws Exception {
        KeyedPoolableObjectFactory<Object,Object> factory = new BaseKeyedPoolableObjectFactory<Object,Object>() { 
            @Override
            public Object makeObject(Object key) throws Exception {
                return null;
            }
        };   
        
        factory.activateObject("key",null); // a no-op
        factory.passivateObject("key",null); // a no-op
        factory.destroyObject("key",null); // a no-op
        assertTrue(factory.validateObject("key",null)); // constant true
    }
}
