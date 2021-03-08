package at.ac.uibk.mcsconnect.common.api;

public interface NetworkTargetFactory {

    NetworkTarget create(String host);

    NetworkTarget create(String host, int port);

    NetworkTarget create(String host, int port, String username, String password);

}
