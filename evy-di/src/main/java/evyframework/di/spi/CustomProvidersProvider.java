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

import evyframework.di.ConfigurationException;
import evyframework.di.Provider;

/**
 * A wrapper around a provider that itself generates providers.
 * 
 * @since initial
 */
class CustomProvidersProvider<T> implements Provider<T> {

    private Provider<Provider<? extends T>> providerOfProviders;

    CustomProvidersProvider(Provider<Provider<? extends T>> providerOfProviders) {
        this.providerOfProviders = providerOfProviders;
    }

    public T get() throws ConfigurationException {
        return providerOfProviders.get().get();
    }
}
