package at.ac.uibk.mcsconnect.http.impl.hidden.assembler;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject.NetworkTargetDTO;

public class NetworkTargetAssembler implements Assembler<NetworkTarget, NetworkTargetDTO> {

    public NetworkTargetDTO writeDTO(NetworkTarget nt) {
        return new NetworkTargetDTO(
                nt.getHost(),
                nt.getPort()
        );
    }

}
