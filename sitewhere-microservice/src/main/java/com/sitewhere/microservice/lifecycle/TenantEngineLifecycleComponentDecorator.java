/**
 * Copyright © 2014-2021 The SiteWhere Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sitewhere.microservice.lifecycle;

import com.sitewhere.spi.microservice.lifecycle.ITenantEngineLifecycleComponent;
import com.sitewhere.spi.microservice.multitenant.IMicroserviceTenantEngine;

public class TenantEngineLifecycleComponentDecorator<T extends ITenantEngineLifecycleComponent>
	extends LifecycleComponentDecorator<T> implements ITenantEngineLifecycleComponent {

    public TenantEngineLifecycleComponentDecorator(T delegate) {
	super(delegate);
    }

    /*
     * @see
     * com.sitewhere.spi.microservice.lifecycle.ITenantEngineLifecycleComponent#
     * buildLabels(java.lang.String[])
     */
    @Override
    public String[] buildLabels(String... labels) {
	return getDelegate().buildLabels(labels);
    }

    /*
     * @see
     * com.sitewhere.spi.microservice.lifecycle.ITenantEngineLifecycleComponent#
     * setTenantEngine(com.sitewhere.spi.microservice.multitenant.
     * IMicroserviceTenantEngine)
     */
    @Override
    public void setTenantEngine(IMicroserviceTenantEngine<?> tenantEngine) {
	getDelegate().setTenantEngine(tenantEngine);
    }

    /*
     * @see
     * com.sitewhere.spi.microservice.lifecycle.ITenantEngineLifecycleComponent#
     * getTenantEngine()
     */
    @Override
    public IMicroserviceTenantEngine<?> getTenantEngine() {
	return getDelegate().getTenantEngine();
    }
}