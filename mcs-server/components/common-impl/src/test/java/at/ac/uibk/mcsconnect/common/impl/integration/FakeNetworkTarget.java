package at.ac.uibk.mcsconnect.common.impl.integration;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;

public class FakeNetworkTarget implements NetworkTarget {

    private String host;
    private int port;
    private String username;
    private String password;

    public static NetworkTarget create(String host, int port, String username, String password) {
        return new FakeNetworkTarget(host, port, username, password);
    }

    private FakeNetworkTarget(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }
}
