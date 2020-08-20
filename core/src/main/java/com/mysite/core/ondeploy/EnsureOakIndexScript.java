package com.mysite.core.ondeploy;

import com.adobe.acs.commons.oak.EnsureOakIndexManager;
import com.adobe.acs.commons.ondeploy.scripts.OnDeployScriptBase;
import org.apache.felix.scr.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnsureOakIndexScript extends OnDeployScriptBase {
    protected static final Logger LOGGER = LoggerFactory.getLogger(EnsureOakIndexScript.class);

    @Reference
    private EnsureOakIndexManager ensureOakIndexManager;


    @Override
    protected void execute() {
        LOGGER.error("RICKY: EnsureOakIndexScript executing");
        ensureOakIndexManager.ensureAll(true);
        LOGGER.error("RICKY: EnsureOakIndexScript executed");
    }
}