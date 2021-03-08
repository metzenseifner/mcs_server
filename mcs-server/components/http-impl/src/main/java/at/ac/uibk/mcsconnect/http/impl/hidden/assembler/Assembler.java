package at.ac.uibk.mcsconnect.http.impl.hidden.assembler;


import at.ac.uibk.mcsconnect.http.api.DTO;

/**
 * Assemblers can write (or read) Data Transfer Objects.
 *
 * @param <Q> The domain object
 * @param <T> The DTO object
 */
public interface Assembler<Q, T extends DTO> {

    <T> T writeDTO(Q object);
}
