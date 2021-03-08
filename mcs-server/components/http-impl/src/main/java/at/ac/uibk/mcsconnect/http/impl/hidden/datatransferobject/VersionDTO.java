package at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject;


import at.ac.uibk.mcsconnect.common.api.Immutable;
import at.ac.uibk.mcsconnect.common.api.Views;
import at.ac.uibk.mcsconnect.http.api.DTO;
import com.fasterxml.jackson.annotation.JsonView;

@Immutable
public class VersionDTO implements DTO {

    @JsonView(Views.Public.class)
    private final String version;

    public VersionDTO(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
