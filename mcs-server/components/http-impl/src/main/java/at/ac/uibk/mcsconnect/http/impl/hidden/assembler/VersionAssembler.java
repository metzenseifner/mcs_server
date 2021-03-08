package at.ac.uibk.mcsconnect.http.impl.hidden.assembler;

import at.ac.uibk.mcsconnect.common.api.McsConfiguration;
import at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject.VersionDTO;
import org.osgi.service.component.annotations.Reference;

public class VersionAssembler implements Assembler<String, VersionDTO> {

    @Reference
    McsConfiguration mcsConfiguration;

    public VersionAssembler() {
    }

    @Override
    public VersionDTO writeDTO(String object) {
        return new VersionDTO(object);
    }

    public VersionDTO fetchAndCreateDTO() {
        return writeDTO(mcsConfiguration.getVersion());
    }
}
