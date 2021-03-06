/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/
package evyframework.di.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import evyframework.di.ConfigurationException;
import evyframework.di.Provider;

/**
 * @since initial
 */
class MapProvider implements Provider<Map<String, ?>> {

    private Map<String, Provider<?>> providers;

    public MapProvider() {
        this.providers = new HashMap<String, Provider<?>>();
    }

    public Map<String, ?> get() throws ConfigurationException {
        Map<String, Object> map = new HashMap<String, Object>();

        for (Entry<String, Provider<?>> entry : providers.entrySet()) {
            map.put(entry.getKey(), entry.getValue().get());
        }

        return map;
    }

    void put(String key, Provider<?> provider) {
        providers.put(key, provider);
    }
}
