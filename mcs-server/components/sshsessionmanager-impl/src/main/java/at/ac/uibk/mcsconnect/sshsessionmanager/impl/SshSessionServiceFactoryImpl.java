//package at.ac.uibk.mcsconnect.sshsessionmanager.impl;
//
//import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
//import at.ac.uibk.mcsconnect.common.api.PreparableFactory;
//import at.ac.uibk.mcsconnect.executorservice.api.McsExecutorService;
//import at.ac.uibk.mcsconnect.sshsessionmanager.api.SshSessionManagerService;
//import at.ac.uibk.mcsconnect.sshsessionmanager.api.SshSessionServiceFactory;
//import at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden.GreeterPattern;
//import at.ac.uibk.mcsconnect.sshsessionmanager.impl.SshChannelShellLockable;
//import org.osgi.service.component.annotations.Component;
//import org.osgi.service.component.annotations.Reference;
//import org.osgi.service.component.annotations.ReferencePolicy;
//import org.osgi.service.component.annotations.ReferenceScope;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.regex.Pattern;
//
//import static at.ac.uibk.mcsconnect.sshsessionmanager.api.SshSessionServiceFactory.FACTORY_ID;
//
//
//@Component(
//        name = "at.ac.uibk.mcsconnect.sshsessionmanager.impl.SshSessionServiceFactory",
//        factory = FACTORY_ID
//)
//@GreeterPattern("")
//public class SshSessionServiceFactoryImpl implements SshSessionServiceFactory {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(SshSessionServiceFactoryImpl.class);
//
//    @Reference(service = McsExecutorService.class, scope = ReferenceScope.BUNDLE, policy = ReferencePolicy.STATIC)
//    private McsExecutorService mcsExecutorService;
//
//    @Reference(service = PreparableFactory.class, scope = ReferenceScope.BUNDLE, policy = ReferencePolicy.STATIC)
//    private PreparableFactory<NetworkTarget, SshChannelShellLockable> preparableFactory;
//
//    public SshSessionManagerService create() {
//        Pattern pattern = Pattern.compile("");
//        return new SshSessionServiceImpl(this.mcsExecutorService, this.preparableFactory, pattern);
//    };
//    public SshSessionManagerService create(Pattern greeter) {
//        return new SshSessionServiceImpl(this.mcsExecutorService, this.preparableFactory, greeter);
//    };
//
//}
//