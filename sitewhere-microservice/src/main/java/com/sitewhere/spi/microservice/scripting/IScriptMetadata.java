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

import java.util.List;

/**
 * Contains metadata about a script.
 */
public interface IScriptMetadata {

    /**
     * Get unique script id.
     * 
     * @return
     */
    String getId();

    /**
     * Get display name for script.
     * 
     * @return
     */
    String getName();

    /**
     * Get description of what script is used for.
     * 
     * @return
     */
    String getDescription();

    /**
     * Get script interpreter type.
     * 
     * @return
     */
    String getInterpreterType();

    /**
     * Get category.
     * 
     * @return
     */
    String getCategory();

    /**
     * Get version identifier for active script.
     * 
     * @return
     */
    String getActiveVersion();

    /**
     * Get list of available script versions.
     * 
     * @return
     */
    List<IScriptVersion> getVersions();
}