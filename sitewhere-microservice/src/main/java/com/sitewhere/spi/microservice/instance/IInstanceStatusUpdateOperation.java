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
package com.sitewhere.spi.microservice.instance;

import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.microservice.IMicroservice;

import io.sitewhere.k8s.crd.instance.SiteWhereInstance;
import io.sitewhere.k8s.crd.instance.SiteWhereInstanceStatus;

/**
 * Operation that mutates the SiteWhere instance resource status.
 */
public interface IInstanceStatusUpdateOperation {

    /**
     * Executes the operation in the context of the given microservice.
     * 
     * @param microservice
     * @return
     * @throws SiteWhereException
     */
    SiteWhereInstance execute(IMicroservice<?, ?> microservice) throws SiteWhereException;

    /**
     * Makes an update to the current instance status.
     * 
     * @param current
     * @throws SiteWhereException
     */
    void update(SiteWhereInstanceStatus current) throws SiteWhereException;
}
