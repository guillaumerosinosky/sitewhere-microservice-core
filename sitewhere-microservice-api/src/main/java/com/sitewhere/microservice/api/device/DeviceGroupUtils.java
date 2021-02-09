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
package com.sitewhere.microservice.api.device;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.sitewhere.microservice.api.asset.IAssetManagement;
import com.sitewhere.rest.model.device.marshaling.MarshaledDeviceGroupElement;
import com.sitewhere.rest.model.search.SearchCriteria;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.IDevice;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.IDeviceType;
import com.sitewhere.spi.device.group.IDeviceGroup;
import com.sitewhere.spi.device.group.IDeviceGroupElement;
import com.sitewhere.spi.search.ISearchCriteria;
import com.sitewhere.spi.search.ISearchResults;
import com.sitewhere.spi.search.device.IDeviceSearchCriteria;

/**
 * Utility methods for maniupulating device groups.
 */
public class DeviceGroupUtils {

    /**
     * Get devices in a group that match the given criteria.
     * 
     * @param group
     * @param criteria
     * @param deviceManagement
     * @param assetManagement
     * @return
     * @throws SiteWhereException
     */
    public static List<IDevice> getDevicesInGroup(IDeviceGroup group, IDeviceSearchCriteria criteria,
	    IDeviceManagement deviceManagement, IAssetManagement assetManagement) throws SiteWhereException {
	Collection<IDevice> devices = getDevicesInGroup(group.getId(), deviceManagement, assetManagement);
	List<IDevice> matches = new ArrayList<IDevice>();
	for (IDevice device : devices) {

	    // Handle filter by device type.
	    if (criteria.getDeviceTypeToken() != null) {
		IDeviceType deviceType = deviceManagement.getDeviceTypeByToken(criteria.getDeviceTypeToken());
		if (!device.getDeviceTypeId().equals(deviceType.getId())) {
		    continue;
		}
	    }

	    // Handle exclude assigned.
	    List<? extends IDeviceAssignment> assignments = deviceManagement.getActiveDeviceAssignments(device.getId());
	    if (criteria.isExcludeAssigned() && (assignments.size() > 0)) {
		continue;
	    }
	    if ((criteria.getStartDate() != null) && (device.getCreatedDate().before(criteria.getStartDate()))) {
		continue;
	    }
	    if ((criteria.getEndDate() != null) && (device.getCreatedDate().after(criteria.getEndDate()))) {
		continue;
	    }
	    matches.add(device);
	}
	return matches;
    }

    /**
     * Get list of all devices in a group. Recurse into nested groups and prevent
     * duplicates or loops in the group hierarchy.
     * 
     * @param groupId
     * @param deviceManagement
     * @param assetManagement
     * @return
     * @throws SiteWhereException
     */
    public static List<IDevice> getDevicesInGroup(UUID groupId, IDeviceManagement deviceManagement,
	    IAssetManagement assetManagement) throws SiteWhereException {
	Map<String, IDevice> devices = new HashMap<>();
	Map<String, IDeviceGroup> groups = new HashMap<>();
	getDevicesInGroup(groupId, deviceManagement, assetManagement, devices, groups);
	List<IDevice> sorted = new ArrayList<>();
	sorted.addAll(devices.values());
	sorted.sort(new Comparator<IDevice>() {

	    @Override
	    public int compare(IDevice o1, IDevice o2) {
		return o1.getCreatedDate().compareTo(o2.getCreatedDate());
	    }
	});
	return sorted;
    }

    /**
     * Get the list of unique devices in a group. (Recurses into subgroups and
     * removes duplicates). Also prevents loops in group references.
     * 
     * @param groupId
     * @param deviceManagement
     * @param assetManagement
     * @param devices
     * @param groups
     * @throws SiteWhereException
     */
    protected static void getDevicesInGroup(UUID groupId, IDeviceManagement deviceManagement,
	    IAssetManagement assetManagement, Map<String, IDevice> devices, Map<String, IDeviceGroup> groups)
	    throws SiteWhereException {
	ISearchResults<? extends IDeviceGroupElement> elements = deviceManagement.listDeviceGroupElements(groupId,
		SearchCriteria.ALL);
	DeviceGroupElementMarshalHelper helper = new DeviceGroupElementMarshalHelper(deviceManagement);
	for (IDeviceGroupElement element : elements.getResults()) {
	    MarshaledDeviceGroupElement inflated = helper.convert(element, assetManagement);
	    if (inflated.getDevice() != null) {
		IDevice device = inflated.getDevice();
		devices.put(device.getToken(), device);
	    } else if (inflated.getDeviceGroup() != null) {
		IDeviceGroup nested = inflated.getDeviceGroup();

		// Prevent loops.
		if (groups.get(nested.getToken()) != null) {
		}
	    }
	}
    }

    /**
     * Gets devices in all groups that have the given role. Duplicates are removed.
     * 
     * @param groupRole
     * @param criteria
     * @param deviceManagement
     * @param assetManagement
     * @return
     * @throws SiteWhereException
     */
    public static Collection<IDevice> getDevicesInGroupsWithRole(String groupRole, IDeviceSearchCriteria criteria,
	    IDeviceManagement deviceManagement, IAssetManagement assetManagement) throws SiteWhereException {
	Map<String, IDevice> devices = new HashMap<String, IDevice>();
	ISearchCriteria groupCriteria = new SearchCriteria(1, 0);
	ISearchResults<? extends IDeviceGroup> groups = deviceManagement.listDeviceGroupsWithRole(groupRole,
		groupCriteria);
	for (IDeviceGroup group : groups.getResults()) {
	    List<IDevice> groupDevices = getDevicesInGroup(group, criteria, deviceManagement, assetManagement);
	    for (IDevice groupDevice : groupDevices) {
		devices.put(groupDevice.getToken(), groupDevice);
	    }
	}
	return devices.values();
    }
}