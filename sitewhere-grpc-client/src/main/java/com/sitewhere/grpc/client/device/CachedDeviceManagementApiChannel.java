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
package com.sitewhere.grpc.client.device;

import java.util.List;
import java.util.UUID;

import com.sitewhere.grpc.client.spi.client.IDeviceManagementApiChannel;
import com.sitewhere.microservice.api.device.IDeviceManagement;
import com.sitewhere.microservice.cache.CacheConfiguration;
import com.sitewhere.microservice.lifecycle.TenantEngineLifecycleComponent;
import com.sitewhere.microservice.security.UserContext;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.area.IArea;
import com.sitewhere.spi.area.IAreaType;
import com.sitewhere.spi.area.IZone;
import com.sitewhere.spi.area.request.IAreaCreateRequest;
import com.sitewhere.spi.area.request.IAreaTypeCreateRequest;
import com.sitewhere.spi.area.request.IZoneCreateRequest;
import com.sitewhere.spi.customer.ICustomer;
import com.sitewhere.spi.customer.ICustomerType;
import com.sitewhere.spi.customer.request.ICustomerCreateRequest;
import com.sitewhere.spi.customer.request.ICustomerTypeCreateRequest;
import com.sitewhere.spi.device.IDevice;
import com.sitewhere.spi.device.IDeviceAlarm;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.IDeviceAssignmentSummary;
import com.sitewhere.spi.device.IDeviceElementMapping;
import com.sitewhere.spi.device.IDeviceStatus;
import com.sitewhere.spi.device.IDeviceSummary;
import com.sitewhere.spi.device.IDeviceType;
import com.sitewhere.spi.device.command.IDeviceCommand;
import com.sitewhere.spi.device.group.IDeviceGroup;
import com.sitewhere.spi.device.group.IDeviceGroupElement;
import com.sitewhere.spi.device.request.IDeviceAlarmCreateRequest;
import com.sitewhere.spi.device.request.IDeviceAssignmentCreateRequest;
import com.sitewhere.spi.device.request.IDeviceCommandCreateRequest;
import com.sitewhere.spi.device.request.IDeviceCreateRequest;
import com.sitewhere.spi.device.request.IDeviceGroupCreateRequest;
import com.sitewhere.spi.device.request.IDeviceGroupElementCreateRequest;
import com.sitewhere.spi.device.request.IDeviceStatusCreateRequest;
import com.sitewhere.spi.device.request.IDeviceTypeCreateRequest;
import com.sitewhere.spi.microservice.cache.ICacheConfiguration;
import com.sitewhere.spi.microservice.cache.ICacheProvider;
import com.sitewhere.spi.microservice.lifecycle.ILifecycleProgressMonitor;
import com.sitewhere.spi.search.ISearchCriteria;
import com.sitewhere.spi.search.ISearchResults;
import com.sitewhere.spi.search.ITreeNode;
import com.sitewhere.spi.search.area.IAreaSearchCriteria;
import com.sitewhere.spi.search.customer.ICustomerSearchCriteria;
import com.sitewhere.spi.search.device.IDeviceAlarmSearchCriteria;
import com.sitewhere.spi.search.device.IDeviceAssignmentSearchCriteria;
import com.sitewhere.spi.search.device.IDeviceCommandSearchCriteria;
import com.sitewhere.spi.search.device.IDeviceSearchCriteria;
import com.sitewhere.spi.search.device.IDeviceStatusSearchCriteria;
import com.sitewhere.spi.search.device.IZoneSearchCriteria;

/**
 * Adds caching support to device management API channel.
 */
public class CachedDeviceManagementApiChannel extends TenantEngineLifecycleComponent implements IDeviceManagement {

    /** Cache settings */
    private CacheSettings cacheSettings;

    /** Wrapped API channel */
    private IDeviceManagementApiChannel<?> wrapped;

    /** Area cache */
    private ICacheProvider<String, IArea> areaCache;

    /** Area by id cache */
    private ICacheProvider<UUID, IArea> areaByIdCache;

    /** Device type cache */
    private ICacheProvider<String, IDeviceType> deviceTypeCache;

    /** Device type by id cache */
    private ICacheProvider<UUID, IDeviceType> deviceTypeByIdCache;

    /** Device cache */
    private ICacheProvider<String, IDevice> deviceCache;

    /** Device by id cache */
    private ICacheProvider<UUID, IDevice> deviceByIdCache;

    /** Device assignment cache */
    private ICacheProvider<String, IDeviceAssignment> deviceAssignmentCache;

    /** Device assignment by id cache */
    private ICacheProvider<UUID, IDeviceAssignment> deviceAssignmentByIdCache;

    public CachedDeviceManagementApiChannel(IDeviceManagementApiChannel<?> wrapped, CacheSettings cacheSettings) {
	this.wrapped = wrapped;
	this.cacheSettings = cacheSettings;
    }

    /*
     * @see
     * com.sitewhere.grpc.client.ApiChannel#initialize(com.sitewhere.spi.server.
     * lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void initialize(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	initializeNestedComponent(getWrapped(), monitor, true);
	this.areaCache = new DeviceManagementCacheProviders.AreaByTokenCache(getMicroservice(),
		getCacheSettings().getAreaConfiguration());
	this.areaByIdCache = new DeviceManagementCacheProviders.AreaByIdCache(getMicroservice(),
		getCacheSettings().getAreaConfiguration());
	this.deviceTypeCache = new DeviceManagementCacheProviders.DeviceTypeByTokenCache(getMicroservice(),
		getCacheSettings().getDeviceTypeConfiguration());
	this.deviceTypeByIdCache = new DeviceManagementCacheProviders.DeviceTypeByIdCache(getMicroservice(),
		getCacheSettings().getDeviceTypeConfiguration());
	this.deviceCache = new DeviceManagementCacheProviders.DeviceByTokenCache(getMicroservice(),
		getCacheSettings().getDeviceConfiguration());
	this.deviceByIdCache = new DeviceManagementCacheProviders.DeviceByIdCache(getMicroservice(),
		getCacheSettings().getDeviceConfiguration());
	this.deviceAssignmentCache = new DeviceManagementCacheProviders.DeviceAssignmentByTokenCache(getMicroservice(),
		getCacheSettings().getDeviceAssignmentConfiguration());
	this.deviceAssignmentByIdCache = new DeviceManagementCacheProviders.DeviceAssignmentByIdCache(getMicroservice(),
		getCacheSettings().getDeviceAssignmentConfiguration());
    }

    /*
     * @see
     * com.sitewhere.grpc.client.ApiChannel#start(com.sitewhere.spi.server.lifecycle
     * .ILifecycleProgressMonitor)
     */
    @Override
    public void start(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	startNestedComponent(getWrapped(), monitor, true);
    }

    /*
     * @see
     * com.sitewhere.grpc.client.ApiChannel#stop(com.sitewhere.spi.server.lifecycle.
     * ILifecycleProgressMonitor)
     */
    @Override
    public void stop(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	stopNestedComponent(getWrapped(), monitor);
    }

    /*
     * @see
     * com.sitewhere.grpc.client.device.DeviceManagementApiChannel#getAreaByToken(
     * java.lang.String)
     */
    @Override
    public IArea getAreaByToken(String token) throws SiteWhereException {
	String tenantId = UserContext.getCurrentTenantId();
	IArea area = getAreaCache().getCacheEntry(tenantId, token);
	if (area == null) {
	    area = getWrapped().getAreaByToken(token);
	    getAreaCache().setCacheEntry(tenantId, token, area);
	}
	return area;
    }

    /*
     * @see
     * com.sitewhere.grpc.client.device.DeviceManagementApiChannel#getArea(java.util
     * .UUID)
     */
    @Override
    public IArea getArea(UUID id) throws SiteWhereException {
	String tenantId = UserContext.getCurrentTenantId();
	IArea area = getAreaByIdCache().getCacheEntry(tenantId, id);
	if (area == null) {
	    area = getWrapped().getArea(id);
	    getAreaByIdCache().setCacheEntry(tenantId, id, area);
	}
	return area;
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createDeviceType(com.sitewhere.spi
     * .device.request.IDeviceTypeCreateRequest)
     */
    @Override
    public IDeviceType createDeviceType(IDeviceTypeCreateRequest request) throws SiteWhereException {
	IDeviceType created = getWrapped().createDeviceType(request);
	String tenantId = UserContext.getCurrentTenantId();
	getDeviceTypeCache().setCacheEntry(tenantId, created.getToken(), created);
	getDeviceTypeByIdCache().setCacheEntry(tenantId, created.getId(), created);
	return created;
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateDeviceType(java.util.UUID,
     * com.sitewhere.spi.device.request.IDeviceTypeCreateRequest)
     */
    @Override
    public IDeviceType updateDeviceType(UUID id, IDeviceTypeCreateRequest request) throws SiteWhereException {
	String tenantId = UserContext.getCurrentTenantId();
	IDeviceType updated = getWrapped().updateDeviceType(id, request);
	getDeviceTypeCache().setCacheEntry(tenantId, updated.getToken(), updated);
	getDeviceTypeByIdCache().setCacheEntry(tenantId, updated.getId(), updated);
	return updated;
    }

    /*
     * @see com.sitewhere.grpc.client.device.DeviceManagementApiChannel#
     * getDeviceTypeByToken(java.lang.String)
     */
    @Override
    public IDeviceType getDeviceTypeByToken(String token) throws SiteWhereException {
	String tenantId = UserContext.getCurrentTenantId();
	IDeviceType deviceType = getDeviceTypeCache().getCacheEntry(tenantId, token);
	if (deviceType == null) {
	    deviceType = getWrapped().getDeviceTypeByToken(token);
	    getDeviceTypeCache().setCacheEntry(tenantId, token, deviceType);
	}
	return deviceType;
    }

    /*
     * @see
     * com.sitewhere.grpc.client.device.DeviceManagementApiChannel#getDeviceType(
     * java.util.UUID)
     */
    @Override
    public IDeviceType getDeviceType(UUID id) throws SiteWhereException {
	String tenantId = UserContext.getCurrentTenantId();
	IDeviceType deviceType = getDeviceTypeByIdCache().getCacheEntry(tenantId, id);
	if (deviceType == null) {
	    deviceType = getWrapped().getDeviceType(id);
	    getDeviceTypeByIdCache().setCacheEntry(tenantId, id, deviceType);
	}
	return deviceType;
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteDeviceType(java.util.UUID)
     */
    @Override
    public IDeviceType deleteDeviceType(UUID id) throws SiteWhereException {
	String tenantId = UserContext.getCurrentTenantId();
	IDeviceType deleted = getWrapped().deleteDeviceType(id);
	getDeviceTypeCache().removeCacheEntry(tenantId, deleted.getToken());
	getDeviceTypeByIdCache().removeCacheEntry(tenantId, deleted.getId());
	return deleted;
    }

    /*
     * @see
     * com.sitewhere.grpc.client.device.DeviceManagementApiChannel#getDeviceByToken(
     * java.lang.String)
     */
    @Override
    public IDevice getDeviceByToken(String token) throws SiteWhereException {
	String tenantId = UserContext.getCurrentTenantId();
	IDevice device = getDeviceCache().getCacheEntry(tenantId, token);
	if (device == null) {
	    device = getWrapped().getDeviceByToken(token);
	    getDeviceCache().setCacheEntry(tenantId, token, device);
	}
	return device;
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createDevice(com.sitewhere.spi.
     * device.request.IDeviceCreateRequest)
     */
    @Override
    public IDevice createDevice(IDeviceCreateRequest device) throws SiteWhereException {
	String tenantId = UserContext.getCurrentTenantId();
	IDevice created = getWrapped().createDevice(device);
	getDeviceCache().setCacheEntry(tenantId, created.getToken(), created);
	getDeviceByIdCache().setCacheEntry(tenantId, created.getId(), created);
	return created;
    }

    /*
     * @see
     * com.sitewhere.grpc.client.device.DeviceManagementApiChannel#getDevice(java.
     * util.UUID)
     */
    @Override
    public IDevice getDevice(UUID deviceId) throws SiteWhereException {
	String tenantId = UserContext.getCurrentTenantId();
	IDevice device = getDeviceByIdCache().getCacheEntry(tenantId, deviceId);
	if (device == null) {
	    device = getWrapped().getDevice(deviceId);
	    getDeviceByIdCache().setCacheEntry(tenantId, deviceId, device);
	}
	return device;
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#updateDevice(java.util.UUID,
     * com.sitewhere.spi.device.request.IDeviceCreateRequest)
     */
    @Override
    public IDevice updateDevice(UUID deviceId, IDeviceCreateRequest request) throws SiteWhereException {
	String tenantId = UserContext.getCurrentTenantId();
	IDevice updated = getWrapped().updateDevice(deviceId, request);
	getDeviceCache().setCacheEntry(tenantId, updated.getToken(), updated);
	getDeviceByIdCache().setCacheEntry(tenantId, updated.getId(), updated);
	return updated;
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#deleteDevice(java.util.UUID)
     */
    @Override
    public IDevice deleteDevice(UUID deviceId) throws SiteWhereException {
	String tenantId = UserContext.getCurrentTenantId();
	IDevice deleted = getWrapped().deleteDevice(deviceId);
	getDeviceCache().removeCacheEntry(tenantId, deleted.getToken());
	getDeviceByIdCache().removeCacheEntry(tenantId, deleted.getId());
	return deleted;
    }

    /*
     * @see com.sitewhere.grpc.client.device.DeviceManagementApiChannel#
     * getDeviceAssignmentByToken(java.lang.String)
     */
    @Override
    public IDeviceAssignment getDeviceAssignmentByToken(String token) throws SiteWhereException {
	String tenantId = UserContext.getCurrentTenantId();
	IDeviceAssignment assignment = getDeviceAssignmentCache().getCacheEntry(tenantId, token);
	if (assignment == null) {
	    assignment = getWrapped().getDeviceAssignmentByToken(token);
	    getDeviceAssignmentCache().setCacheEntry(tenantId, token, assignment);
	}
	return assignment;
    }

    /*
     * @see com.sitewhere.grpc.client.device.DeviceManagementApiChannel#
     * getDeviceAssignment(java.util.UUID)
     */
    @Override
    public IDeviceAssignment getDeviceAssignment(UUID id) throws SiteWhereException {
	String tenantId = UserContext.getCurrentTenantId();
	IDeviceAssignment assignment = getDeviceAssignmentByIdCache().getCacheEntry(tenantId, id);
	if (assignment == null) {
	    assignment = getWrapped().getDeviceAssignment(id);
	    getDeviceAssignmentByIdCache().setCacheEntry(tenantId, id, assignment);
	}
	return assignment;
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateDeviceAssignment(java.util.
     * UUID, com.sitewhere.spi.device.request.IDeviceAssignmentCreateRequest)
     */
    @Override
    public IDeviceAssignment updateDeviceAssignment(UUID id, IDeviceAssignmentCreateRequest request)
	    throws SiteWhereException {
	String tenantId = UserContext.getCurrentTenantId();
	IDeviceAssignment updated = getWrapped().updateDeviceAssignment(id, request);
	getDeviceAssignmentCache().setCacheEntry(tenantId, updated.getToken(), updated);
	getDeviceAssignmentByIdCache().setCacheEntry(tenantId, updated.getId(), updated);
	return updated;
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteDeviceAssignment(java.util.
     * UUID)
     */
    @Override
    public IDeviceAssignment deleteDeviceAssignment(UUID id) throws SiteWhereException {
	String tenantId = UserContext.getCurrentTenantId();
	IDeviceAssignment deleted = getWrapped().deleteDeviceAssignment(id);
	getDeviceAssignmentCache().removeCacheEntry(tenantId, deleted.getToken());
	getDeviceAssignmentByIdCache().removeCacheEntry(tenantId, deleted.getId());
	return deleted;
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listDeviceTypes(com.sitewhere.spi.
     * search.ISearchCriteria)
     */
    @Override
    public ISearchResults<? extends IDeviceType> listDeviceTypes(ISearchCriteria criteria) throws SiteWhereException {
	return getWrapped().listDeviceTypes(criteria);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createDeviceCommand(com.sitewhere.
     * spi.device.request.IDeviceCommandCreateRequest)
     */
    @Override
    public IDeviceCommand createDeviceCommand(IDeviceCommandCreateRequest request) throws SiteWhereException {
	return getWrapped().createDeviceCommand(request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getDeviceCommand(java.util.UUID)
     */
    @Override
    public IDeviceCommand getDeviceCommand(UUID id) throws SiteWhereException {
	return getWrapped().getDeviceCommand(id);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getDeviceCommandByToken(java.util.
     * UUID, java.lang.String)
     */
    @Override
    public IDeviceCommand getDeviceCommandByToken(UUID deviceTypeId, String token) throws SiteWhereException {
	return getWrapped().getDeviceCommandByToken(deviceTypeId, token);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateDeviceCommand(java.util.
     * UUID, com.sitewhere.spi.device.request.IDeviceCommandCreateRequest)
     */
    @Override
    public IDeviceCommand updateDeviceCommand(UUID id, IDeviceCommandCreateRequest request) throws SiteWhereException {
	return getWrapped().updateDeviceCommand(id, request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listDeviceCommands(com.sitewhere.
     * spi.search.device.IDeviceCommandSearchCriteria)
     */
    @Override
    public ISearchResults<? extends IDeviceCommand> listDeviceCommands(IDeviceCommandSearchCriteria criteria)
	    throws SiteWhereException {
	return getWrapped().listDeviceCommands(criteria);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteDeviceCommand(java.util.
     * UUID)
     */
    @Override
    public IDeviceCommand deleteDeviceCommand(UUID id) throws SiteWhereException {
	return getWrapped().deleteDeviceCommand(id);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createDeviceStatus(com.sitewhere.
     * spi.device.request.IDeviceStatusCreateRequest)
     */
    @Override
    public IDeviceStatus createDeviceStatus(IDeviceStatusCreateRequest request) throws SiteWhereException {
	return getWrapped().createDeviceStatus(request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getDeviceStatus(java.util.UUID)
     */
    @Override
    public IDeviceStatus getDeviceStatus(UUID id) throws SiteWhereException {
	return getWrapped().getDeviceStatus(id);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getDeviceStatusByToken(java.util.
     * UUID, java.lang.String)
     */
    @Override
    public IDeviceStatus getDeviceStatusByToken(UUID deviceTypeId, String token) throws SiteWhereException {
	return getWrapped().getDeviceStatusByToken(deviceTypeId, token);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateDeviceStatus(java.util.UUID,
     * com.sitewhere.spi.device.request.IDeviceStatusCreateRequest)
     */
    @Override
    public IDeviceStatus updateDeviceStatus(UUID id, IDeviceStatusCreateRequest request) throws SiteWhereException {
	return getWrapped().updateDeviceStatus(id, request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listDeviceStatuses(com.sitewhere.
     * spi.search.device.IDeviceStatusSearchCriteria)
     */
    @Override
    public ISearchResults<? extends IDeviceStatus> listDeviceStatuses(IDeviceStatusSearchCriteria criteria)
	    throws SiteWhereException {
	return getWrapped().listDeviceStatuses(criteria);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteDeviceStatus(java.util.UUID)
     */
    @Override
    public IDeviceStatus deleteDeviceStatus(UUID id) throws SiteWhereException {
	return getWrapped().deleteDeviceStatus(id);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listDevices(com.sitewhere.spi.
     * search.device.IDeviceSearchCriteria)
     */
    @Override
    public ISearchResults<? extends IDevice> listDevices(IDeviceSearchCriteria criteria) throws SiteWhereException {
	return getWrapped().listDevices(criteria);
    }

    /*
     * @see
     * com.sitewhere.microservice.api.device.IDeviceManagement#listDeviceSummaries(
     * com.sitewhere.spi.search.device.IDeviceSearchCriteria)
     */
    @Override
    public ISearchResults<? extends IDeviceSummary> listDeviceSummaries(IDeviceSearchCriteria criteria)
	    throws SiteWhereException {
	return getWrapped().listDeviceSummaries(criteria);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createDeviceElementMapping(java.
     * util.UUID, com.sitewhere.spi.device.IDeviceElementMapping)
     */
    @Override
    public IDevice createDeviceElementMapping(UUID deviceId, IDeviceElementMapping mapping) throws SiteWhereException {
	return getWrapped().createDeviceElementMapping(deviceId, mapping);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteDeviceElementMapping(java.
     * util.UUID, java.lang.String)
     */
    @Override
    public IDevice deleteDeviceElementMapping(UUID deviceId, String path) throws SiteWhereException {
	return getWrapped().deleteDeviceElementMapping(deviceId, path);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#createDeviceAssignment(com.
     * sitewhere.spi.device.request.IDeviceAssignmentCreateRequest)
     */
    @Override
    public IDeviceAssignment createDeviceAssignment(IDeviceAssignmentCreateRequest request) throws SiteWhereException {
	return getWrapped().createDeviceAssignment(request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getActiveDeviceAssignments(java.
     * util.UUID)
     */
    @Override
    public List<? extends IDeviceAssignment> getActiveDeviceAssignments(UUID deviceId) throws SiteWhereException {
	return getWrapped().getActiveDeviceAssignments(deviceId);
    }

    /*
     * @see
     * com.sitewhere.microservice.api.device.IDeviceManagement#listDeviceAssignments
     * (com.sitewhere.spi.search.device.IDeviceAssignmentSearchCriteria)
     */
    @Override
    public ISearchResults<? extends IDeviceAssignment> listDeviceAssignments(IDeviceAssignmentSearchCriteria criteria)
	    throws SiteWhereException {
	return getWrapped().listDeviceAssignments(criteria);
    }

    /*
     * @see com.sitewhere.microservice.api.device.IDeviceManagement#
     * listDeviceAssignmentSummaries(com.sitewhere.spi.search.device.
     * IDeviceAssignmentSearchCriteria)
     */
    @Override
    public ISearchResults<? extends IDeviceAssignmentSummary> listDeviceAssignmentSummaries(
	    IDeviceAssignmentSearchCriteria criteria) throws SiteWhereException {
	return getWrapped().listDeviceAssignmentSummaries(criteria);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#endDeviceAssignment(java.util.
     * UUID)
     */
    @Override
    public IDeviceAssignment endDeviceAssignment(UUID id) throws SiteWhereException {
	return getWrapped().endDeviceAssignment(id);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createDeviceAlarm(com.sitewhere.
     * spi.device.request.IDeviceAlarmCreateRequest)
     */
    @Override
    public IDeviceAlarm createDeviceAlarm(IDeviceAlarmCreateRequest request) throws SiteWhereException {
	return getWrapped().createDeviceAlarm(request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateDeviceAlarm(java.util.UUID,
     * com.sitewhere.spi.device.request.IDeviceAlarmCreateRequest)
     */
    @Override
    public IDeviceAlarm updateDeviceAlarm(UUID id, IDeviceAlarmCreateRequest request) throws SiteWhereException {
	return getWrapped().updateDeviceAlarm(id, request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getDeviceAlarm(java.util.UUID)
     */
    @Override
    public IDeviceAlarm getDeviceAlarm(UUID id) throws SiteWhereException {
	return getWrapped().getDeviceAlarm(id);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#searchDeviceAlarms(com.sitewhere.
     * spi.search.device.IDeviceAlarmSearchCriteria)
     */
    @Override
    public ISearchResults<? extends IDeviceAlarm> searchDeviceAlarms(IDeviceAlarmSearchCriteria criteria)
	    throws SiteWhereException {
	return getWrapped().searchDeviceAlarms(criteria);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteDeviceAlarm(java.util.UUID)
     */
    @Override
    public IDeviceAlarm deleteDeviceAlarm(UUID id) throws SiteWhereException {
	return getWrapped().deleteDeviceAlarm(id);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createCustomerType(com.sitewhere.
     * spi.customer.request.ICustomerTypeCreateRequest)
     */
    @Override
    public ICustomerType createCustomerType(ICustomerTypeCreateRequest request) throws SiteWhereException {
	return getWrapped().createCustomerType(request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getCustomerType(java.util.UUID)
     */
    @Override
    public ICustomerType getCustomerType(UUID id) throws SiteWhereException {
	return getWrapped().getCustomerType(id);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getCustomerTypeByToken(java.lang.
     * String)
     */
    @Override
    public ICustomerType getCustomerTypeByToken(String token) throws SiteWhereException {
	return getWrapped().getCustomerTypeByToken(token);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateCustomerType(java.util.UUID,
     * com.sitewhere.spi.customer.request.ICustomerTypeCreateRequest)
     */
    @Override
    public ICustomerType updateCustomerType(UUID id, ICustomerTypeCreateRequest request) throws SiteWhereException {
	return getWrapped().updateCustomerType(id, request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listCustomerTypes(com.sitewhere.
     * spi.search.ISearchCriteria)
     */
    @Override
    public ISearchResults<? extends ICustomerType> listCustomerTypes(ISearchCriteria criteria)
	    throws SiteWhereException {
	return getWrapped().listCustomerTypes(criteria);
    }

    /*
     * @see com.sitewhere.microservice.api.device.IDeviceManagement#
     * getContainedCustomerTypes(java.util.UUID)
     */
    @Override
    public List<? extends ICustomerType> getContainedCustomerTypes(UUID customerTypeId) throws SiteWhereException {
	return getWrapped().getContainedCustomerTypes(customerTypeId);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteCustomerType(java.util.UUID)
     */
    @Override
    public ICustomerType deleteCustomerType(UUID id) throws SiteWhereException {
	return getWrapped().deleteCustomerType(id);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createCustomer(com.sitewhere.spi.
     * customer.request.ICustomerCreateRequest)
     */
    @Override
    public ICustomer createCustomer(ICustomerCreateRequest request) throws SiteWhereException {
	return getWrapped().createCustomer(request);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#getCustomer(java.util.UUID)
     */
    @Override
    public ICustomer getCustomer(UUID id) throws SiteWhereException {
	return getWrapped().getCustomer(id);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#getCustomerByToken(java.lang.
     * String)
     */
    @Override
    public ICustomer getCustomerByToken(String token) throws SiteWhereException {
	return getWrapped().getCustomerByToken(token);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getCustomerChildren(java.lang.
     * String)
     */
    @Override
    public List<? extends ICustomer> getCustomerChildren(String token) throws SiteWhereException {
	return getWrapped().getCustomerChildren(token);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateCustomer(java.util.UUID,
     * com.sitewhere.spi.customer.request.ICustomerCreateRequest)
     */
    @Override
    public ICustomer updateCustomer(UUID id, ICustomerCreateRequest request) throws SiteWhereException {
	return getWrapped().updateCustomer(id, request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listCustomers(com.sitewhere.spi.
     * search.customer.ICustomerSearchCriteria)
     */
    @Override
    public ISearchResults<? extends ICustomer> listCustomers(ICustomerSearchCriteria criteria)
	    throws SiteWhereException {
	return getWrapped().listCustomers(criteria);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#getCustomersTree()
     */
    @Override
    public List<? extends ITreeNode> getCustomersTree() throws SiteWhereException {
	return getWrapped().getCustomersTree();
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteCustomer(java.util.UUID)
     */
    @Override
    public ICustomer deleteCustomer(UUID id) throws SiteWhereException {
	return getWrapped().deleteCustomer(id);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createAreaType(com.sitewhere.spi.
     * area.request.IAreaTypeCreateRequest)
     */
    @Override
    public IAreaType createAreaType(IAreaTypeCreateRequest request) throws SiteWhereException {
	return getWrapped().createAreaType(request);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#getAreaType(java.util.UUID)
     */
    @Override
    public IAreaType getAreaType(UUID id) throws SiteWhereException {
	return getWrapped().getAreaType(id);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#getAreaTypeByToken(java.lang.
     * String)
     */
    @Override
    public IAreaType getAreaTypeByToken(String token) throws SiteWhereException {
	return getWrapped().getAreaTypeByToken(token);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateAreaType(java.util.UUID,
     * com.sitewhere.spi.area.request.IAreaTypeCreateRequest)
     */
    @Override
    public IAreaType updateAreaType(UUID id, IAreaTypeCreateRequest request) throws SiteWhereException {
	return getWrapped().updateAreaType(id, request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listAreaTypes(com.sitewhere.spi.
     * search.ISearchCriteria)
     */
    @Override
    public ISearchResults<? extends IAreaType> listAreaTypes(ISearchCriteria criteria) throws SiteWhereException {
	return getWrapped().listAreaTypes(criteria);
    }

    /*
     * @see
     * com.sitewhere.microservice.api.device.IDeviceManagement#getContainedAreaTypes
     * (java.util.UUID)
     */
    @Override
    public List<? extends IAreaType> getContainedAreaTypes(UUID areaTypeId) throws SiteWhereException {
	return getWrapped().getContainedAreaTypes(areaTypeId);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteAreaType(java.util.UUID)
     */
    @Override
    public IAreaType deleteAreaType(UUID id) throws SiteWhereException {
	return getWrapped().deleteAreaType(id);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createArea(com.sitewhere.spi.area.
     * request.IAreaCreateRequest)
     */
    @Override
    public IArea createArea(IAreaCreateRequest request) throws SiteWhereException {
	return getWrapped().createArea(request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getAreaChildren(java.lang.String)
     */
    @Override
    public List<? extends IArea> getAreaChildren(String token) throws SiteWhereException {
	return getWrapped().getAreaChildren(token);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#updateArea(java.util.UUID,
     * com.sitewhere.spi.area.request.IAreaCreateRequest)
     */
    @Override
    public IArea updateArea(UUID id, IAreaCreateRequest request) throws SiteWhereException {
	return getWrapped().updateArea(id, request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listAreas(com.sitewhere.spi.search
     * .area.IAreaSearchCriteria)
     */
    @Override
    public ISearchResults<? extends IArea> listAreas(IAreaSearchCriteria criteria) throws SiteWhereException {
	return getWrapped().listAreas(criteria);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#getAreasTree()
     */
    @Override
    public List<? extends ITreeNode> getAreasTree() throws SiteWhereException {
	return getWrapped().getAreasTree();
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#deleteArea(java.util.UUID)
     */
    @Override
    public IArea deleteArea(UUID id) throws SiteWhereException {
	return getWrapped().deleteArea(id);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createZone(com.sitewhere.spi.area.
     * request.IZoneCreateRequest)
     */
    @Override
    public IZone createZone(IZoneCreateRequest request) throws SiteWhereException {
	return getWrapped().createZone(request);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#getZone(java.util.UUID)
     */
    @Override
    public IZone getZone(UUID id) throws SiteWhereException {
	return getWrapped().getZone(id);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getZoneByToken(java.lang.String)
     */
    @Override
    public IZone getZoneByToken(String zoneToken) throws SiteWhereException {
	return getWrapped().getZoneByToken(zoneToken);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#updateZone(java.util.UUID,
     * com.sitewhere.spi.area.request.IZoneCreateRequest)
     */
    @Override
    public IZone updateZone(UUID id, IZoneCreateRequest request) throws SiteWhereException {
	return getWrapped().updateZone(id, request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listZones(com.sitewhere.spi.search
     * .device.IZoneSearchCriteria)
     */
    @Override
    public ISearchResults<? extends IZone> listZones(IZoneSearchCriteria criteria) throws SiteWhereException {
	return getWrapped().listZones(criteria);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#deleteZone(java.util.UUID)
     */
    @Override
    public IZone deleteZone(UUID id) throws SiteWhereException {
	return getWrapped().deleteZone(id);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createDeviceGroup(com.sitewhere.
     * spi.device.request.IDeviceGroupCreateRequest)
     */
    @Override
    public IDeviceGroup createDeviceGroup(IDeviceGroupCreateRequest request) throws SiteWhereException {
	return getWrapped().createDeviceGroup(request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getDeviceGroup(java.util.UUID)
     */
    @Override
    public IDeviceGroup getDeviceGroup(UUID id) throws SiteWhereException {
	return getWrapped().getDeviceGroup(id);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getDeviceGroupByToken(java.lang.
     * String)
     */
    @Override
    public IDeviceGroup getDeviceGroupByToken(String token) throws SiteWhereException {
	return getWrapped().getDeviceGroupByToken(token);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateDeviceGroup(java.util.UUID,
     * com.sitewhere.spi.device.request.IDeviceGroupCreateRequest)
     */
    @Override
    public IDeviceGroup updateDeviceGroup(UUID id, IDeviceGroupCreateRequest request) throws SiteWhereException {
	return getWrapped().updateDeviceGroup(id, request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listDeviceGroups(com.sitewhere.spi
     * .search.ISearchCriteria)
     */
    @Override
    public ISearchResults<? extends IDeviceGroup> listDeviceGroups(ISearchCriteria criteria) throws SiteWhereException {
	return getWrapped().listDeviceGroups(criteria);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listDeviceGroupsWithRole(java.lang
     * .String, com.sitewhere.spi.search.ISearchCriteria)
     */
    @Override
    public ISearchResults<? extends IDeviceGroup> listDeviceGroupsWithRole(String role, ISearchCriteria criteria)
	    throws SiteWhereException {
	return getWrapped().listDeviceGroupsWithRole(role, criteria);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteDeviceGroup(java.util.UUID)
     */
    @Override
    public IDeviceGroup deleteDeviceGroup(UUID id) throws SiteWhereException {
	return getWrapped().deleteDeviceGroup(id);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#addDeviceGroupElements(java.util.
     * UUID, java.util.List, boolean)
     */
    @Override
    public List<? extends IDeviceGroupElement> addDeviceGroupElements(UUID groupId,
	    List<IDeviceGroupElementCreateRequest> elements, boolean ignoreDuplicates) throws SiteWhereException {
	return getWrapped().addDeviceGroupElements(groupId, elements, ignoreDuplicates);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#removeDeviceGroupElements(java.
     * util.List)
     */
    @Override
    public List<? extends IDeviceGroupElement> removeDeviceGroupElements(List<UUID> elements)
	    throws SiteWhereException {
	return getWrapped().removeDeviceGroupElements(elements);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listDeviceGroupElements(java.util.
     * UUID, com.sitewhere.spi.search.ISearchCriteria)
     */
    @Override
    public ISearchResults<? extends IDeviceGroupElement> listDeviceGroupElements(UUID groupId, ISearchCriteria criteria)
	    throws SiteWhereException {
	return getWrapped().listDeviceGroupElements(groupId, criteria);
    }

    /**
     * Contains default cache settings for device management entities.
     */
    public static class CacheSettings {

	/** Cache configuraton for areas */
	private ICacheConfiguration areaConfiguration = new CacheConfiguration(1000, 60);

	/** Cache configuration for device types */
	private ICacheConfiguration deviceTypeConfiguration = new CacheConfiguration(1000, 60);

	/** Cache configuration for devices */
	private ICacheConfiguration deviceConfiguration = new CacheConfiguration(10000, 60);

	/** Cache configuration for device assignments */
	private ICacheConfiguration deviceAssignmentConfiguration = new CacheConfiguration(10000, 60);

	public ICacheConfiguration getAreaConfiguration() {
	    return areaConfiguration;
	}

	public void setAreaConfiguration(ICacheConfiguration areaConfiguration) {
	    this.areaConfiguration = areaConfiguration;
	}

	public ICacheConfiguration getDeviceTypeConfiguration() {
	    return deviceTypeConfiguration;
	}

	public void setDeviceTypeConfiguration(ICacheConfiguration deviceTypeConfiguration) {
	    this.deviceTypeConfiguration = deviceTypeConfiguration;
	}

	public ICacheConfiguration getDeviceConfiguration() {
	    return deviceConfiguration;
	}

	public void setDeviceConfiguration(ICacheConfiguration deviceConfiguration) {
	    this.deviceConfiguration = deviceConfiguration;
	}

	public ICacheConfiguration getDeviceAssignmentConfiguration() {
	    return deviceAssignmentConfiguration;
	}

	public void setDeviceAssignmentConfiguration(ICacheConfiguration deviceAssignmentConfiguration) {
	    this.deviceAssignmentConfiguration = deviceAssignmentConfiguration;
	}
    }

    public ICacheProvider<String, IArea> getAreaCache() {
	return areaCache;
    }

    public ICacheProvider<UUID, IArea> getAreaByIdCache() {
	return areaByIdCache;
    }

    public ICacheProvider<String, IDeviceType> getDeviceTypeCache() {
	return deviceTypeCache;
    }

    public ICacheProvider<UUID, IDeviceType> getDeviceTypeByIdCache() {
	return deviceTypeByIdCache;
    }

    protected ICacheProvider<String, IDevice> getDeviceCache() {
	return deviceCache;
    }

    public ICacheProvider<UUID, IDevice> getDeviceByIdCache() {
	return deviceByIdCache;
    }

    protected ICacheProvider<String, IDeviceAssignment> getDeviceAssignmentCache() {
	return deviceAssignmentCache;
    }

    protected ICacheProvider<UUID, IDeviceAssignment> getDeviceAssignmentByIdCache() {
	return deviceAssignmentByIdCache;
    }

    protected IDeviceManagementApiChannel<?> getWrapped() {
	return wrapped;
    }

    protected CacheSettings getCacheSettings() {
	return cacheSettings;
    }
}