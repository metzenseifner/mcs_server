package at.ac.uibk.mcsconnect.common.api;

import at.ac.uibk.mcsconnect.functional.common.Result;

public interface NetworkTargetFactory {

    NetworkTarget create(String host);

    NetworkTarget create(String host, int port);

    NetworkTarget create(String host, int port, String username, String password);

    Result<NetworkTarget> create(String host, String port, String username, String password);

}
