package at.ac.uibk.mcsconnect.common.impl.hidden;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;

public class NetworkTargetImpl implements NetworkTarget {

    private String host;
    private int port;
    private String username;
    private String password;

    public NetworkTargetImpl(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    //@JsonGetter("host")
    @Override
    public String getHost() {
        return host;
    }

    //@JsonGetter("port")
    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String toString() {
        return String.format("%s(%s, %s, %s, %s)", this.getClass().getSimpleName(), this.host, this.port, "Username logging disabled", "Password logging disabled");
    }
}
