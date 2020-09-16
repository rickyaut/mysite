package com.mysite.core.transformers;

import com.mysite.core.ResourceUtils;
import org.apache.sling.rewriter.Transformer;
import org.apache.sling.rewriter.TransformerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(property = { "pipeline.type=mytransformer" }, service = { TransformerFactory.class })
public class MyRewriterTransformerFactory implements TransformerFactory {
    Logger LOGGER = LoggerFactory.getLogger(ResourceUtils.class);
    public Transformer createTransformer() {
        LOGGER.error("creating transformer...");
        return new MyRewriterTransformer ();
    }
}