package at.ac.uibk.mcsconnect.roomrepo.impl.integration;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.common.impl.integration.FakeNetworkTarget;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;

public class FakeTerminal implements Terminal {

    private String id;
    private String name;
    private NetworkTarget networkTarget;

    public static Terminal create(String id, String name, String host) {
        return new FakeTerminal(id, name, FakeNetworkTarget.create(host, 123, "", ""));
    }

    private FakeTerminal(String id, String name, NetworkTarget networkTarget) {
        this.id = id;
        this.name = name;
        this.networkTarget = networkTarget;
    }

    @Override
    public NetworkTarget getNetworkTarget() {
        return this.networkTarget;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", this.getClass().getSimpleName(), getId());
    }
}
