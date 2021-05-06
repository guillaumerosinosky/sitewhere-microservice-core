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
package com.sitewhere.grpc.client.devicestate;

import java.util.List;
import java.util.UUID;

import com.sitewhere.grpc.client.GrpcUtils;
import com.sitewhere.grpc.client.MultitenantApiChannel;
import com.sitewhere.grpc.client.spi.client.IDeviceStateApiChannel;
import com.sitewhere.grpc.common.CommonModelConverter;
import com.sitewhere.grpc.devicestate.DeviceStateModelConverter;
import com.sitewhere.grpc.service.DeviceStateGrpc;
import com.sitewhere.grpc.service.GCreateDeviceStateRequest;
import com.sitewhere.grpc.service.GCreateDeviceStateResponse;
import com.sitewhere.grpc.service.GDeleteDeviceStateRequest;
import com.sitewhere.grpc.service.GDeleteDeviceStateResponse;
import com.sitewhere.grpc.service.GGetDeviceStateByAssignmentRequest;
import com.sitewhere.grpc.service.GGetDeviceStateByAssignmentResponse;
import com.sitewhere.grpc.service.GGetDeviceStateRequest;
import com.sitewhere.grpc.service.GGetDeviceStateResponse;
import com.sitewhere.grpc.service.GGetDeviceStatesByDeviceRequest;
import com.sitewhere.grpc.service.GGetDeviceStatesByDeviceResponse;
import com.sitewhere.grpc.service.GMergeDeviceStateRequest;
import com.sitewhere.grpc.service.GMergeDeviceStateResponse;
import com.sitewhere.grpc.service.GSearchDeviceStatesRequest;
import com.sitewhere.grpc.service.GSearchDeviceStatesResponse;
import com.sitewhere.grpc.service.GUpdateDeviceStateRequest;
import com.sitewhere.grpc.service.GUpdateDeviceStateResponse;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.state.IDeviceState;
import com.sitewhere.spi.device.state.request.IDeviceStateCreateRequest;
import com.sitewhere.spi.device.state.request.IDeviceStateEventMergeRequest;
import com.sitewhere.spi.microservice.IFunctionIdentifier;
import com.sitewhere.spi.microservice.IInstanceSettings;
import com.sitewhere.spi.microservice.MicroserviceIdentifier;
import com.sitewhere.spi.microservice.grpc.GrpcServiceIdentifier;
import com.sitewhere.spi.microservice.grpc.IGrpcServiceIdentifier;
import com.sitewhere.spi.microservice.grpc.IGrpcSettings;
import com.sitewhere.spi.search.ISearchResults;
import com.sitewhere.spi.search.device.IDeviceStateSearchCriteria;

/**
 * Supports SiteWhere device state APIs on top of a
 * {@link DeviceStateGrpcChannel}.
 */
public class DeviceStateApiChannel extends MultitenantApiChannel<DeviceStateGrpcChannel>
	implements IDeviceStateApiChannel<DeviceStateGrpcChannel> {

    public DeviceStateApiChannel(IInstanceSettings settings) {
	super(settings, MicroserviceIdentifier.DeviceState, GrpcServiceIdentifier.DeviceState,
		IGrpcSettings.DEFAULT_API_PORT);
    }

    /*
     * @see
     * com.sitewhere.grpc.client.spi.IApiChannel#createGrpcChannel(com.sitewhere.spi
     * .microservice.instance.IInstanceSettings,
     * com.sitewhere.spi.microservice.IFunctionIdentifier,
     * com.sitewhere.spi.microservice.grpc.IGrpcServiceIdentifier, int)
     */
    @Override
    public DeviceStateGrpcChannel createGrpcChannel(IInstanceSettings settings, IFunctionIdentifier identifier,
	    IGrpcServiceIdentifier grpcServiceIdentifier, int port) {
	return new DeviceStateGrpcChannel(settings, identifier, grpcServiceIdentifier, port);
    }

    /*
     * @see
     * com.sitewhere.spi.device.state.IDeviceStateManagement#createDeviceState(com.
     * sitewhere.spi.device.state.request.IDeviceStateCreateRequest)
     */
    @Override
    public IDeviceState createDeviceState(IDeviceStateCreateRequest request) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, DeviceStateGrpc.getCreateDeviceStateMethod());
	    GCreateDeviceStateRequest.Builder grequest = GCreateDeviceStateRequest.newBuilder();
	    grequest.setRequest(DeviceStateModelConverter.asGrpcDeviceStateCreateRequest(request));
	    GCreateDeviceStateResponse gresponse = getGrpcChannel().getBlockingStub()
		    .createDeviceState(grequest.build());
	    IDeviceState response = (gresponse.hasDeviceState())
		    ? DeviceStateModelConverter.asApiDeviceState(gresponse.getDeviceState())
		    : null;
	    GrpcUtils.logClientMethodResponse(DeviceStateGrpc.getCreateDeviceStateMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(DeviceStateGrpc.getCreateDeviceStateMethod(), t);
	}
    }

    /*
     * @see
     * com.sitewhere.spi.device.state.IDeviceStateManagement#getDeviceState(java.
     * util.UUID)
     */
    @Override
    public IDeviceState getDeviceState(UUID id) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, DeviceStateGrpc.getGetDeviceStateMethod());
	    GGetDeviceStateRequest.Builder grequest = GGetDeviceStateRequest.newBuilder();
	    grequest.setId(CommonModelConverter.asGrpcUuid(id));
	    GGetDeviceStateResponse gresponse = getGrpcChannel().getBlockingStub().getDeviceState(grequest.build());
	    IDeviceState response = (gresponse.hasDeviceState())
		    ? DeviceStateModelConverter.asApiDeviceState(gresponse.getDeviceState())
		    : null;
	    GrpcUtils.logClientMethodResponse(DeviceStateGrpc.getGetDeviceStateMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(DeviceStateGrpc.getGetDeviceStateMethod(), t);
	}
    }

    /*
     * @see com.sitewhere.microservice.api.state.IDeviceStateManagement#
     * getDeviceStateByDeviceAssignment(java.util.UUID)
     */
    @Override
    public IDeviceState getDeviceStateByDeviceAssignment(UUID assignmentId) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, DeviceStateGrpc.getGetDeviceStateByAssignmentMethod());
	    GGetDeviceStateByAssignmentRequest.Builder grequest = GGetDeviceStateByAssignmentRequest.newBuilder();
	    grequest.setDeviceAssignmentId(CommonModelConverter.asGrpcUuid(assignmentId));
	    GGetDeviceStateByAssignmentResponse gresponse = getGrpcChannel().getBlockingStub()
		    .getDeviceStateByAssignment(grequest.build());
	    IDeviceState response = (gresponse.hasDeviceState())
		    ? DeviceStateModelConverter.asApiDeviceState(gresponse.getDeviceState())
		    : null;
	    GrpcUtils.logClientMethodResponse(DeviceStateGrpc.getGetDeviceStateByAssignmentMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(DeviceStateGrpc.getGetDeviceStateByAssignmentMethod(), t);
	}
    }

    /*
     * @see com.sitewhere.microservice.api.state.IDeviceStateManagement#
     * getDeviceStatesForDevice(java.util.UUID)
     */
    @Override
    public List<? extends IDeviceState> getDeviceStatesForDevice(UUID deviceId) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, DeviceStateGrpc.getGetDeviceStatesByDeviceMethod());
	    GGetDeviceStatesByDeviceRequest.Builder grequest = GGetDeviceStatesByDeviceRequest.newBuilder();
	    grequest.setDeviceId(CommonModelConverter.asGrpcUuid(deviceId));
	    GGetDeviceStatesByDeviceResponse gresponse = getGrpcChannel().getBlockingStub()
		    .getDeviceStatesByDevice(grequest.build());
	    List<? extends IDeviceState> response = DeviceStateModelConverter
		    .asApiDeviceStates(gresponse.getDeviceStatesList());
	    GrpcUtils.logClientMethodResponse(DeviceStateGrpc.getGetDeviceStatesByDeviceMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(DeviceStateGrpc.getGetDeviceStatesByDeviceMethod(), t);
	}
    }

    /*
     * @see
     * com.sitewhere.spi.device.state.IDeviceStateManagement#searchDeviceStates(com.
     * sitewhere.spi.search.device.IDeviceStateSearchCriteria)
     */
    @Override
    public ISearchResults<IDeviceState> searchDeviceStates(IDeviceStateSearchCriteria criteria)
	    throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, DeviceStateGrpc.getSearchDeviceStatesMethod());
	    GSearchDeviceStatesRequest.Builder grequest = GSearchDeviceStatesRequest.newBuilder();
	    grequest.setCriteria(DeviceStateModelConverter.asGrpcDeviceStateSearchCriteria(criteria));
	    GSearchDeviceStatesResponse gresponse = getGrpcChannel().getBlockingStub()
		    .searchDeviceStates(grequest.build());
	    ISearchResults<IDeviceState> results = DeviceStateModelConverter
		    .asApiDeviceStateSearchResults(gresponse.getResults());
	    GrpcUtils.logClientMethodResponse(DeviceStateGrpc.getSearchDeviceStatesMethod(), results);
	    return results;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(DeviceStateGrpc.getSearchDeviceStatesMethod(), t);
	}
    }

    /*
     * @see
     * com.sitewhere.spi.device.state.IDeviceStateManagement#updateDeviceState(java.
     * util.UUID, com.sitewhere.spi.device.state.request.IDeviceStateCreateRequest)
     */
    @Override
    public IDeviceState updateDeviceState(UUID id, IDeviceStateCreateRequest request) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, DeviceStateGrpc.getUpdateDeviceStateMethod());
	    GUpdateDeviceStateRequest.Builder grequest = GUpdateDeviceStateRequest.newBuilder();
	    grequest.setId(CommonModelConverter.asGrpcUuid(id));
	    grequest.setRequest(DeviceStateModelConverter.asGrpcDeviceStateCreateRequest(request));
	    GUpdateDeviceStateResponse gresponse = getGrpcChannel().getBlockingStub()
		    .updateDeviceState(grequest.build());
	    IDeviceState response = (gresponse.hasDeviceState())
		    ? DeviceStateModelConverter.asApiDeviceState(gresponse.getDeviceState())
		    : null;
	    GrpcUtils.logClientMethodResponse(DeviceStateGrpc.getUpdateDeviceStateMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(DeviceStateGrpc.getUpdateDeviceStateMethod(), t);
	}
    }

    /*
     * @see
     * com.sitewhere.microservice.api.state.IDeviceStateManagement#merge(java.util.
     * UUID, com.sitewhere.spi.device.state.request.IDeviceStateEventMergeRequest)
     */
    @Override
    public IDeviceState merge(UUID id, IDeviceStateEventMergeRequest events) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, DeviceStateGrpc.getMergeDeviceStateMethod());
	    GMergeDeviceStateRequest.Builder grequest = GMergeDeviceStateRequest.newBuilder();
	    grequest.setId(CommonModelConverter.asGrpcUuid(id));
	    grequest.setRequest(DeviceStateModelConverter.asGrpcDeviceStateEventMergeRequest(events));
	    GMergeDeviceStateResponse gresponse = getGrpcChannel().getBlockingStub().mergeDeviceState(grequest.build());
	    IDeviceState response = (gresponse.hasDeviceState())
		    ? DeviceStateModelConverter.asApiDeviceState(gresponse.getDeviceState())
		    : null;
	    GrpcUtils.logClientMethodResponse(DeviceStateGrpc.getMergeDeviceStateMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(DeviceStateGrpc.getMergeDeviceStateMethod(), t);
	}
    }

    /*
     * @see
     * com.sitewhere.spi.device.state.IDeviceStateManagement#deleteDeviceState(java.
     * util.UUID)
     */
    @Override
    public IDeviceState deleteDeviceState(UUID id) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, DeviceStateGrpc.getDeleteDeviceStateMethod());
	    GDeleteDeviceStateRequest.Builder grequest = GDeleteDeviceStateRequest.newBuilder();
	    grequest.setId(CommonModelConverter.asGrpcUuid(id));
	    GDeleteDeviceStateResponse gresponse = getGrpcChannel().getBlockingStub()
		    .deleteDeviceState(grequest.build());
	    IDeviceState response = (gresponse.hasDeviceState())
		    ? DeviceStateModelConverter.asApiDeviceState(gresponse.getDeviceState())
		    : null;
	    GrpcUtils.logClientMethodResponse(DeviceStateGrpc.getDeleteDeviceStateMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(DeviceStateGrpc.getDeleteDeviceStateMethod(), t);
	}
    }
}