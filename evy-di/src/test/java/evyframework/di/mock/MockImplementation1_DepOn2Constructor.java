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
package evyframework.di.mock;

import evyframework.di.Inject;

public class MockImplementation1_DepOn2Constructor implements MockInterface1 {

    private MockInterface2 interface2;

    // this creates a circular dependency when MockImplementation2 is bound to
    // MockInterface2.
    public MockImplementation1_DepOn2Constructor(@Inject MockInterface2 interface2) {
        this.interface2 = interface2;
    }

    public String getName() {
        return interface2.getName();
    }
}
