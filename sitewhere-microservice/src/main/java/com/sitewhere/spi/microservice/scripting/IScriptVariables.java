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
package com.sitewhere.spi.microservice.scripting;

/**
 * Names used when injecting variables into scripts.
 */
public interface IScriptVariables {

    /** Variable used for tenant information */
    public static final String VAR_TENANT = "tenant";

    /** Variable used for device management implementaton */
    public static final String VAR_DEVICE_MANAGEMENT = "deviceManagement";

    /** Variable used for device information */
    public static final String VAR_DEVICE = "device";

    /** Variable used for event information */
    public static final String VAR_EVENT = "event";

    /** Variable used for event context information */
    public static final String VAR_EVENT_CONTEXT = "context";

    /** Variable used for passing assignment */
    public static final String VAR_ASSIGNMENT = "assignment";

    /** Variable used for passing active assignments */
    public static final String VAR_ACTIVE_ASSIGNMENTS = "assignments";

    /** Variable used for device management builder */
    public static final String VAR_DEVICE_MANAGEMENT_BUILDER = "deviceBuilder";

    /** Variable used for event management builder */
    public static final String VAR_EVENT_MANAGEMENT_BUILDER = "eventBuilder";

    /** Variable used for asset management builder */
    public static final String VAR_ASSET_MANAGEMENT_BUILDER = "assetBuilder";

    /** Variable used for tenant management builder */
    public static final String VAR_TENANT_MANAGEMENT_BUILDER = "tenantBuilder";

    /** Variable used for schedule management builder */
    public static final String VAR_SCHEDULE_MANAGEMENT_BUILDER = "scheduleBuilder";

    /** Variable used for decoded events */
    public static final String VAR_DECODED_EVENTS = "events";

    /** Variable used for interacting with decoded device request */
    public static final String VAR_DECODED_DEVICE_REQUEST = "request";

    /** Variable used for passing payload */
    public static final String VAR_PAYLOAD = "payload";

    /** Variable used for passing payload metadata */
    public static final String VAR_PAYLOAD_METADATA = "metadata";

    /** Variable used for a list of event payloads */
    public static final String VAR_EVENT_PAYLOADS = "payloads";

    /** Variable used for passing a REST client */
    public static final String VAR_REST_CLIENT = "rest";

    /** Variable used for passing logger */
    public static final String VAR_LOGGER = "logger";

    /** Variable used for passing command execution */
    public static final String VAR_COMMAND_EXECUTION = "execution";

    /** Variable used for passing system command */
    public static final String VAR_SYSTEM_COMMAND = "system";

    /** Variable used for passing command (or command execution) */
    public static final String VAR_NESTING_CONTEXT = "nesting";
}