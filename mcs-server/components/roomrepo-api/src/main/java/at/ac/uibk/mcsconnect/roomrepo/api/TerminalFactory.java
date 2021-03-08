package at.ac.uibk.mcsconnect.roomrepo.api;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;

public interface TerminalFactory {
    Terminal create(String id, NetworkTarget networkTarget);
    Terminal create(String id, String name, NetworkTarget networkTarget);
}
