package at.ac.uibk.mcsconnect.roomrepo.impl.integration;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;
import at.ac.uibk.mcsconnect.roomrepo.api.TerminalFactory;

public class FakeTerminalFactory implements TerminalFactory {
    @Override
    public Terminal create(String id, NetworkTarget networkTarget) {
        return FakeTerminal.create(id, "", networkTarget.getHost());
    }

    @Override
    public Terminal create(String id, String name, NetworkTarget networkTarget) {
        return FakeTerminal.create(id, name, networkTarget.getHost());
    }
}
