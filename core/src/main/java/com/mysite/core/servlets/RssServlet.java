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
package com.mysite.core.servlets;

import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.PageManager;
import com.mysite.core.Constants;
import com.mysite.core.ResourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Servlet that writes some sample content into the response. It is mounted for
 * all resources of a specific Sling resource type. The
 * {@link SlingSafeMethodsServlet} shall be used for HTTP methods that are
 * idempotent. For write operations use the {@link SlingAllMethodsServlet}.
 */
@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes="mysite/components/structure/page",
        methods=HttpConstants.METHOD_GET,
        selectors = "rss",
        extensions="xml")
@ServiceDescription("Rss Demo Servlet")
public class RssServlet extends SlingSafeMethodsServlet {
    protected static final Logger LOGGER = LoggerFactory.getLogger(RssServlet.class);

    private static final long serialVersionUID = 1L;

    private static final String PARAMETER_NAME_RESOURCETYPE = "resourceType";
    private static final String RESOURCE_TYPE_EVENT_DETAILS = "mysite/components/content/event";
    private static final String STATEMENT_EVENT_QUERY = "SELECT * FROM [nt:unstructured] AS s"
        + " WHERE ISDESCENDANTNODE([%s]) AND [sling:resourceType]=$" + PARAMETER_NAME_RESOURCETYPE;
    private static final String STATEMENT_EVENT_ORDER_BY = " ORDER BY startDateTime DESC";
    private static final String PARAMETER_NAME_ENDDATE = "endDateTime";
    private static final String PARAMETER_NAME_STARTDATE = "startDateTime";
    private static final String URL_PATH_PAST = "past";
    private static final String URL_PATH_FUTURE = "future";
    private static final String CRITERIA_BY_PAST = " AND [endDateTime] < $" + PARAMETER_NAME_ENDDATE;
    private static final String CRITERIA_BY_FUTURE = " AND [endDateTime] >= $" + PARAMETER_NAME_STARTDATE;

    private DocumentBuilder documentBuilder;
    private Marshaller marshaller;

    @XmlRootElement(name = "rss")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class Rss {
        @XmlAttribute
        String version = "2.0";
        @XmlAttribute(name="xmlns:dc")
        String dc = "http://purl.org/dc/elements/1.1/";
        @XmlElement
        Channel channel;

        public Rss() {
        }

        public Rss(Channel channel) {
            this.channel = channel;
        }
    }

    private static class Channel {
        @XmlElement(name="item")
        List<Event> events;

        public Channel() {
        }

        public Channel(List<Event> events) {
            this.events = events;
        }
    }

    private static class Event implements Cloneable {
        @XmlElement
        private String title, startDateTime, endDateTime;
        @XmlElement
        private String topics[];

        public Event() {
        }

        public Event(String title, String startDateTime, String endDateTime, String topics[]) {
            this.title = title;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.topics = topics;
        }

    }

    @Activate
    protected final void activate(Map<String, Object> properties) {
        LOGGER.info("RssServlet Activating");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOGGER.error("Can not instantiate DocumentBuilder object", e);
        }
        marshaller = createMarshallerObj(Rss.class);
        LOGGER.info("EventRssService Activate");
    }

    static Marshaller createMarshallerObj(Class objClass) {
        try {
            JAXBContext contextObj = JAXBContext.newInstance(objClass);
            Marshaller marshallerObj = contextObj.createMarshaller();
            marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshallerObj.setProperty(Marshaller.JAXB_FRAGMENT, true);
            return marshallerObj;
        } catch (JAXBException e) {
            LOGGER.error("Can not instantiate JAXB objects", e);
        }
        return null;
    }

    @Override
    protected void doGet(final SlingHttpServletRequest request,
            final SlingHttpServletResponse response) throws ServletException, IOException {
        ResourceResolver resourceResolver = request.getResourceResolver();
        Session session = resourceResolver.adaptTo(Session.class);
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        TagManager tagManager = resourceResolver.adaptTo(TagManager.class);

        String resourcePath = request.getResource().getParent().getPath();
        RequestPathInfo requestPathInfo = request.getRequestPathInfo();
        String[] selectors = requestPathInfo.getSelectors();

        LOGGER.info("start processing: " + resourcePath);
        List<Event> allEvents = new ArrayList<Event>();
        try {
            QueryManager queryManager = session.getWorkspace().getQueryManager();
            Query query = createEventQuery(session, queryManager, resourcePath, selectors);
            Iterator<Node> nodeIterator = query.execute().getNodes();
            while(nodeIterator.hasNext()) {
                Node node = nodeIterator.next();
                allEvents.add(new Event(
                    ResourceUtils.getSinglePropertyValue(node, "title", String.class),
                    DateFormatUtils.format(ResourceUtils.getSinglePropertyValue(node, "startDateTime", Calendar.class), Constants.DATE_TIME_FORMAT),
                    DateFormatUtils.format(ResourceUtils.getSinglePropertyValue(node, "endDateTime", Calendar.class), Constants.DATE_TIME_FORMAT),
                    ResourceUtils.getMultiPropertyValue(node, "topics", String.class))
                );
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            marshaller.marshal(new Rss(new Channel(allEvents)), os);

            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/xml");
            PrintWriter writer = response.getWriter();
            writer.write(os.toString());
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private Query createEventQuery(Session session, QueryManager queryManager, String resourcePath, String[] filters) throws RepositoryException, ParseException, RepositoryException {
        Calendar currentDateTime = Calendar.getInstance();
        String sql = String.format(STATEMENT_EVENT_QUERY, resourcePath);
        Calendar startDate = null, endDate = null;
        for (int index = 0; index < filters.length; index++) {
            if (StringUtils.equalsAnyIgnoreCase(filters[index], URL_PATH_PAST)) {
                sql += CRITERIA_BY_PAST;
                endDate = currentDateTime;
            } else if (StringUtils.equalsAnyIgnoreCase(filters[index], URL_PATH_FUTURE)) {
                sql += CRITERIA_BY_FUTURE;
                startDate = currentDateTime;
            }
        }
        Query query = queryManager.createQuery(sql + STATEMENT_EVENT_ORDER_BY, Query.JCR_SQL2);
        ValueFactory valueFactory = session.getValueFactory();
        bindParamIntoQuery(query, PARAMETER_NAME_RESOURCETYPE, valueFactory, RESOURCE_TYPE_EVENT_DETAILS, true);
        bindParamIntoQuery(query, PARAMETER_NAME_ENDDATE, valueFactory, endDate, endDate != null);
        bindParamIntoQuery(query, PARAMETER_NAME_STARTDATE, valueFactory, startDate, startDate != null);
        query.setLimit(200);
        return query;
    }

    private void bindParamIntoQuery(Query query, String paramName, ValueFactory valueFactory, Object value, boolean bindCondition) throws RepositoryException {
        if (bindCondition) {
            if (value instanceof String) {
                query.bindValue(paramName, valueFactory.createValue((String) value));
            } else if (value instanceof Calendar) {
                query.bindValue(paramName, valueFactory.createValue((Calendar) value));
            }
        }
    }
}
