package at.ac.uibk.mcsconnect.http.impl.hidden.assembler;

import at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject.NetworkTargetDTO;
import at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject.TerminalDTO;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;

/**
 * Convert a Terminal type between object and serialized data.
 */
public class TerminalAssembler implements Assembler<Terminal, TerminalDTO> {

    public TerminalDTO writeDTO(Terminal terminal) {

        NetworkTargetDTO networkTargetDTO =
                new NetworkTargetAssembler().writeDTO(
                        terminal.getNetworkTarget());

        //RoomDTO roomDTO =
        //        new RoomAssembler().writeDTO(terminal.getRoom());

        return new TerminalDTO(
                terminal.getId(),
                networkTargetDTO
                );
    }

}
