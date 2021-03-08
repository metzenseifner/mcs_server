package at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject;

import at.ac.uibk.mcsconnect.common.api.Immutable;
import at.ac.uibk.mcsconnect.common.api.Views;
import at.ac.uibk.mcsconnect.http.api.DTO;
import com.fasterxml.jackson.annotation.JsonView;

@Immutable
public class TerminalDTO implements DTO {

    @JsonView(Views.Public.class)
    public final  String id;
    @JsonView(Views.Public.class)
    public final  NetworkTargetDTO networkTarget;

    public TerminalDTO(String id, NetworkTargetDTO networkTargetDTO) {
        this.id = id;
        this.networkTarget = networkTargetDTO;
    }
}
