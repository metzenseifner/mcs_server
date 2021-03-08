package at.ac.uibk.mcsconnect.common.impl;

import at.ac.uibk.mcsconnect.common.api.McsConfiguration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

// TODO Set me up...I am a noncritical component
@Component(
        name = "at.ac.uibk.mcsconnect.common.impl.McsConfigurationImpl",
        immediate = true,
        scope = ServiceScope.SINGLETON
)
public class McsConfigurationImpl implements McsConfiguration {
    public String getVersion() {
        return getBuildVersion();
    }
    private static String getBuildVersion(){
        return McsConfigurationImpl.class.getPackage().getImplementationVersion();
    }
}
