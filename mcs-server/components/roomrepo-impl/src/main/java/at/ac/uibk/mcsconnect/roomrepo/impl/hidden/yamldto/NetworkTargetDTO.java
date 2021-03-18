package at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto;

import java.io.Serializable;

public class NetworkTargetDTO implements Serializable {

    private String host;
    private String port;
    private String username;
    private String password;

    public NetworkTargetDTO() {}

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toString() {
        return String.format("%s(%s, %s, %s, %s)", this.getClass().getSimpleName(), getHost(), getPort(), getUsername(), "Cannot log passwords!");
    }
}
