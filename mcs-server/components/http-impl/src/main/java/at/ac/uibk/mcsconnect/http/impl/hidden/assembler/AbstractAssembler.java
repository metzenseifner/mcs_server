package at.ac.uibk.mcsconnect.http.impl.hidden.assembler;

import at.ac.uibk.mcsconnect.http.api.DTO;

public abstract class AbstractAssembler<Q, T extends DTO> implements Assembler<Q, T>{
    abstract protected  Q readDTO(T dto);
}
