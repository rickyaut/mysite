/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mysite.core.models;

import com.mysite.core.Constants;
import com.mysite.core.ResourceUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.settings.SlingSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import java.util.Calendar;

import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;

@Model(adaptables = Resource.class)
public class EventModel {
    Logger LOGGER = LoggerFactory.getLogger(EventModel.class);
    private final String ATTR_START_DATE_TIME = "startDateTime";
    private final String ATTR_END_DATE_TIME = "endDateTime";

    @ValueMapValue(name=PROPERTY_RESOURCE_TYPE, injectionStrategy=InjectionStrategy.OPTIONAL)
    @Default(values="No resourceType")
    protected String resourceType;

    @OSGiService
    private SlingSettingsService settings;
    @SlingObject
    private Resource currentResource;
    @SlingObject
    private ResourceResolver resourceResolver;

    private String startDateTime;
    private String endDateTime;

    @PostConstruct
    protected void init() {
        Node currentNode = currentResource.adaptTo(Node.class);
        try {
            if(currentNode.hasProperty(ATTR_START_DATE_TIME)){
                Calendar calStartDateTime = ResourceUtils.getSinglePropertyValue(currentNode, ATTR_START_DATE_TIME, Calendar.class);
                startDateTime = DateFormatUtils.format(calStartDateTime.getTime(), Constants.DATE_TIME_FORMAT);
            }
            if(currentNode.hasProperty(ATTR_END_DATE_TIME)){
                Calendar calStartDateTime = ResourceUtils.getSinglePropertyValue(currentNode, ATTR_END_DATE_TIME, Calendar.class);
                endDateTime = DateFormatUtils.format(calStartDateTime.getTime(), Constants.DATE_TIME_FORMAT);
            }
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }
}
