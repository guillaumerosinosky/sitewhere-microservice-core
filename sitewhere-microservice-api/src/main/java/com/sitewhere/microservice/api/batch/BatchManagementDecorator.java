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
package com.sitewhere.microservice.api.batch;

import java.util.UUID;

import com.sitewhere.microservice.lifecycle.TenantEngineLifecycleComponentDecorator;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.batch.IBatchElement;
import com.sitewhere.spi.batch.IBatchOperation;
import com.sitewhere.spi.batch.request.IBatchCommandInvocationRequest;
import com.sitewhere.spi.batch.request.IBatchElementCreateRequest;
import com.sitewhere.spi.batch.request.IBatchOperationCreateRequest;
import com.sitewhere.spi.batch.request.IBatchOperationUpdateRequest;
import com.sitewhere.spi.search.ISearchResults;
import com.sitewhere.spi.search.batch.IBatchOperationSearchCriteria;
import com.sitewhere.spi.search.device.IBatchElementSearchCriteria;

/**
 * Wraps a batch management implementation. Subclasses can implement only the
 * methods they need to override.
 */
public class BatchManagementDecorator extends TenantEngineLifecycleComponentDecorator<IBatchManagement>
	implements IBatchManagement {

    public BatchManagementDecorator(IBatchManagement delegate) {
	super(delegate);
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#createBatchOperation(com.sitewhere.
     * spi.batch.request.IBatchOperationCreateRequest)
     */
    @Override
    public IBatchOperation createBatchOperation(IBatchOperationCreateRequest request) throws SiteWhereException {
	return getDelegate().createBatchOperation(request);
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#updateBatchOperation(java.util.UUID,
     * com.sitewhere.spi.batch.request.IBatchOperationUpdateRequest)
     */
    @Override
    public IBatchOperation updateBatchOperation(UUID batchOperationId, IBatchOperationUpdateRequest request)
	    throws SiteWhereException {
	return getDelegate().updateBatchOperation(batchOperationId, request);
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#getBatchOperation(java.util.UUID)
     */
    @Override
    public IBatchOperation getBatchOperation(UUID batchOperationId) throws SiteWhereException {
	return getDelegate().getBatchOperation(batchOperationId);
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#getBatchOperationByToken(java.lang.
     * String)
     */
    @Override
    public IBatchOperation getBatchOperationByToken(String token) throws SiteWhereException {
	return getDelegate().getBatchOperationByToken(token);
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#listBatchOperations(com.sitewhere.
     * spi.search.batch.IBatchOperationSearchCriteria)
     */
    @Override
    public ISearchResults<? extends IBatchOperation> listBatchOperations(IBatchOperationSearchCriteria criteria)
	    throws SiteWhereException {
	return getDelegate().listBatchOperations(criteria);
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#deleteBatchOperation(java.util.UUID)
     */
    @Override
    public IBatchOperation deleteBatchOperation(UUID batchOperationId) throws SiteWhereException {
	return getDelegate().deleteBatchOperation(batchOperationId);
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#createBatchElement(java.util.UUID,
     * com.sitewhere.spi.batch.request.IBatchElementCreateRequest)
     */
    @Override
    public IBatchElement createBatchElement(UUID batchOperationId, IBatchElementCreateRequest request)
	    throws SiteWhereException {
	return getDelegate().createBatchElement(batchOperationId, request);
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#listBatchElements(java.util.UUID,
     * com.sitewhere.spi.search.device.IBatchElementSearchCriteria)
     */
    @Override
    public ISearchResults<? extends IBatchElement> listBatchElements(UUID batchOperationId,
	    IBatchElementSearchCriteria criteria) throws SiteWhereException {
	return getDelegate().listBatchElements(batchOperationId, criteria);
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#updateBatchElement(java.util.UUID,
     * com.sitewhere.spi.batch.request.IBatchElementCreateRequest)
     */
    @Override
    public IBatchElement updateBatchElement(UUID elementId, IBatchElementCreateRequest request)
	    throws SiteWhereException {
	return getDelegate().updateBatchElement(elementId, request);
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#createBatchCommandInvocation(com.
     * sitewhere.spi.batch.request.IBatchCommandInvocationRequest)
     */
    @Override
    public IBatchOperation createBatchCommandInvocation(IBatchCommandInvocationRequest request)
	    throws SiteWhereException {
	return getDelegate().createBatchCommandInvocation(request);
    }
}