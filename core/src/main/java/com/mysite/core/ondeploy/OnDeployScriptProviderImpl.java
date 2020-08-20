package com.mysite.core.ondeploy;

import com.adobe.acs.commons.ondeploy.OnDeployScriptProvider;
import com.adobe.acs.commons.ondeploy.scripts.OnDeployScript;
import com.adobe.acs.commons.ondeploy.scripts.OnDeployScriptBase;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

class MyScript1 extends OnDeployScriptBase {
    protected static final Logger LOGGER = LoggerFactory.getLogger(MyScript1.class);

    @Override
    protected void execute() throws Exception {
        LOGGER.error("RICKY: MyScript1 executed");
    }
}

@Component(service=OnDeployScriptProvider.class,
    property = {
        "service.description=Developer service that identifies code scripts to execute upon deployment"
    }
)
public class OnDeployScriptProviderImpl implements OnDeployScriptProvider {
    @Override
    public final List<OnDeployScript> getScripts() {
        return Arrays.asList(
                new MyScript1(), new EnsureOakIndexScript()
        );
    }
}