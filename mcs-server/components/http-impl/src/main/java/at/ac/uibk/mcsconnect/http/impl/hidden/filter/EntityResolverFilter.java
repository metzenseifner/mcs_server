package at.ac.uibk.mcsconnect.http.impl.hidden.filter;

import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.functional.common.Tuple;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomRepo;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static at.ac.uibk.mcsconnect.http.impl.hidden.filter.Utility.getHeaderValue;
import static at.ac.uibk.mcsconnect.http.impl.hidden.filter.Utility.getHeaders;
import static at.ac.uibk.mcsconnect.http.impl.hidden.mapper.Conventions.TERMINAL_REQUEST_KEY;

/**
 * Entry point for inbound requests on the public API.
 * Because of the university architecture, the ip address
 * is what uniquely identifies a valid client, not a certificate
 * or otherwise. This class effectively validates a request by
 * adding the mcsTerminal and mcsRoom properties to the request
 * context if a match is found in the registry.
 *
 * NOTE CXF does not expose the Request or Response ContainerRequestFilter to the resource,
 * which is something Jersey does and is expected in JAX-RS. Therefore, I would need to use
 * vendor-specific CXF code to pass information to the resource class from this provider.
 *
 * Because of this, it might be a good idea to abandon the idea of using JAX-RS filters
 * for this and just handle the logic in the resource class using only JAX-RS classes to
 * minimize dependencies on CXF-specific code. The alternative is to switch to CXF-specific code
 * and implement a CXF interceptor.
 *
 */
//@PreMatching
@Provider
@Priority(Priorities.AUTHORIZATION-1)
public class EntityResolverFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityResolverFilter.class);

    @Reference
    RoomRepo roomRepo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

            LOGGER.info(String.format("%s called with headers: %s", this, requestContext.getHeaders()));

        MultivaluedMap<String, String> headers = getHeaders(requestContext);

        // func headers -> Entity
        Result<String> rXForwardedFor = getHeaderValue("x-forwarded-for", headers);
        Result<String> rHost = getHeaderValue("host", headers).map(h -> h.split(":")[0]);

        getHeaderValue("x-forwarded-for", headers).forEachOrFail(h -> LOGGER.info(String.format("x-forwarded-for: %s", h))).forEach(EntityResolverFilter::logWarning);
        getHeaderValue("host", headers).forEachOrFail(h -> LOGGER.info(String.format("host: %s", h))).forEach(EntityResolverFilter::logWarning);

        rXForwardedFor.orElse(() -> rHost)
                .flatMap(v -> getTerminalByHostAsResult(roomRepo, v))
                .forEachOrFail(t -> {
                        requestContext.setProperty(TERMINAL_REQUEST_KEY, t._2);
                        requestContext.setProperty("mcsRoom", t._1);
                })
                .map(e -> String.format("%s in %s", e, headers.toString()))
                .forEach(EntityResolverFilter::logError);
    }

    /**
     * 1. Filter set of locations by ones that have terminals installed.
     * 2. Get Set<Tuple<Room, Terminal>>
     * 3. Return Result<(Room, Terminal)> | Failure
     *
     * @param roomRepo
     * @param host (ip or hostname)
     * @return
     */
    private static Result<Tuple<Room, Terminal>> getTerminalByHostAsResult(RoomRepo roomRepo, String host) {
        Set<Room> rooms = roomRepo.getRoomsByFilter(r -> r.hasTerminals());
        Set<Tuple<Room, Terminal>> terminalByRoomSet = rooms.stream()
                .map(r -> new LinkedList<Tuple<Room, Terminal>>(
                        r.getTerminals().stream()
                                .map(t0 -> new Tuple<>(r, t0))
                                .collect(Collectors.toList())))
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        Optional<Tuple<Room, Terminal>> result = terminalByRoomSet.stream().filter(t -> t._2.getNetworkTarget().getHost().equals(host)).findFirst();


        return result.isPresent()
                ? Result.success(result.get())
                : Result.failure(String.format("Unknown host: %s", host)); // TODO replace with requestContext.abortWith(Response.serverError());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    private static void logError(String errMsg) {
        LOGGER.error(errMsg);
    }
    private static void logWarning(String errMsg) {
        LOGGER.warn(errMsg);
    }
}
