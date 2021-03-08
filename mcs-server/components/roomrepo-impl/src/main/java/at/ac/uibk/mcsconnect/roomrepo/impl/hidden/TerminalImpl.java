package at.ac.uibk.mcsconnect.roomrepo.impl.hidden;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;

public class TerminalImpl implements Terminal {

    private final String terminalId;
    private final NetworkTarget networkTarget;
    private final String name;


    public TerminalImpl(String terminalId, NetworkTarget networkTarget, String name) {
        this.terminalId = terminalId;
        this.networkTarget = networkTarget;
        this.name = name;
    }

    @Override
    public NetworkTarget getNetworkTarget() {
        return networkTarget;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return terminalId;
    }
}
