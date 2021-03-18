package at.ac.uibk.mcsconnect.common.impl;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.common.api.NetworkTargetFactory;
import at.ac.uibk.mcsconnect.common.api.StrUtils;
import at.ac.uibk.mcsconnect.common.impl.hidden.NetworkTargetImpl;
import at.ac.uibk.mcsconnect.functional.common.Result;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
        name = "at.ac.uibk.mcsconnect.common.impl.NetworkTargetFactory",
        immediate = true
)
public class NetworkTargetFactoryImpl implements NetworkTargetFactory {

    @Activate
    public NetworkTargetFactoryImpl() {};

    @Override
    public NetworkTarget create(String host) {
        return new NetworkTargetImpl(host, 443, "", "");
    }

    @Override
    public NetworkTarget create(String host, int port) {
        return new NetworkTargetImpl(host, port, "", "");
    }

    @Override
    public NetworkTarget create(String host, int port, String username, String password) {
        return new NetworkTargetImpl(host, port, username, password);
    }

    @Override
    public Result<NetworkTarget> create(String host, String port, String username, String password) {
        Result<String> rHost = Result.of(host, "Host may not be null");
        Result<Integer> rPort = StrUtils.parseAsInteger(port).mapFailure("Port could not be parsed as an integer");
        Result<String> rUsername = Result.of(username, "Username may not be null.");
        Result<String> rPassword = Result.of(password, "Password may not be null.");
        return rHost.flatMap(h -> rPort
                .flatMap(p -> rUsername
                        .flatMap(usr -> rPassword
                                .map(ps -> new NetworkTargetImpl(h, p, usr, ps)))));
    }
}
