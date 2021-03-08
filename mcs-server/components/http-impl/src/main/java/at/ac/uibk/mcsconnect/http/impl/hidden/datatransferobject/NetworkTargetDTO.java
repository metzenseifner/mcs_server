package at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject;


import at.ac.uibk.mcsconnect.common.api.Immutable;
import at.ac.uibk.mcsconnect.common.api.Views;
import at.ac.uibk.mcsconnect.http.api.DTO;
import com.fasterxml.jackson.annotation.JsonView;

@Immutable
public class NetworkTargetDTO implements DTO {

    @JsonView(Views.Public.class)
    public final String host;
    @JsonView(Views.Public.class)
    public final int port;

    public NetworkTargetDTO(String host, int port) {
        this.host = host;
        this.port = port;
    };

}
