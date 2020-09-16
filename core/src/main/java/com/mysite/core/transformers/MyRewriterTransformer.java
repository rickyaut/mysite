package com.mysite.core.transformers;

import java.io.IOException;

import org.apache.cocoon.xml.sax.AbstractSAXPipe;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Transformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.servlet.http.HttpServletRequest;

public class MyRewriterTransformer extends AbstractSAXPipe implements Transformer {

    private static final Logger log = LoggerFactory.getLogger(MyRewriterTransformer.class);
    private SlingHttpServletRequest httpRequest;
    /* The element and attribute to act on  */
    private static final String ATT_NAME = new String("src");
    private static final String EL_NAME = new String("img");

    public MyRewriterTransformer () {
    }
    public void dispose() {
    }
    public void init(ProcessingContext context, ProcessingComponentConfiguration config) throws IOException {
        this.httpRequest = context.getRequest();
        log.debug("Transforming request {}.", httpRequest.getRequestURI());
    }
    @Override
    public void startElement (String nsUri, String localname, String qname, Attributes atts) throws SAXException {
        /* copy the element attributes */
        AttributesImpl linkAtts = new AttributesImpl(atts);
        /* Only interested in EL_NAME elements */
        if(EL_NAME.equalsIgnoreCase(localname)){

            /* iterate through the attributes of the element and act only on ATT_NAME attributes */
            for (int i=0; i < linkAtts.getLength(); i++) {
                if (ATT_NAME.equalsIgnoreCase(linkAtts.getLocalName(i))) {
                    String path_in_link = linkAtts.getValue(i);

                    /* use the resource resolver of the http request to reverse-resolve the path  */
                    String mappedPath = httpRequest.getResourceResolver().map(httpRequest, path_in_link);

                    log.info("Tranformed {} to {}.", path_in_link,mappedPath);

                    /* update the attribute value */
                    linkAtts.setValue(i,mappedPath);
                }
            }

        }
        /* return updated attributes to super and continue with the transformer chain */
        super.startElement(nsUri, localname, qname, linkAtts);
    }
}

