package at.ac.uibk.mcsconnect.common.impl.integration;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.common.api.NetworkTargetFactory;

public class FakeNetworkTargetFactory implements NetworkTargetFactory {
    @Override
    public NetworkTarget create(String host) {
        return FakeNetworkTarget.create(host, 443, "", "");
    }

    @Override
    public NetworkTarget create(String host, int port) {
        return FakeNetworkTarget.create(host, port, "", "");
    }

    @Override
    public NetworkTarget create(String host, int port, String username, String password) {
        return FakeNetworkTarget.create(host, port, username, password);
    }
}
