package at.ac.uibk.mcsconnect.http.api;

/**
 * DTO stands for data transfer object, whose job it is
 * to abstract what is serialized and deserialized
 * when communicating with remote clients.
 *
 * An object of this type should be kept minimal. It may not depend on
 * any domain objects. This is because this representation is known
 * on both ends of the connection (the client knows nothing about server
 * domain objects). The other direction is also true, domain objects may not
 * depend on DTOs, because DTOs may change independently of domain objects
 * to accommodate interface changes.
 */
public interface DTO {
}
