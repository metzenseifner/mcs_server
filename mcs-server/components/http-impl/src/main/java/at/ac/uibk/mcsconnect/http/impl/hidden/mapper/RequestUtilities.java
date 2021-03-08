package at.ac.uibk.mcsconnect.http.impl.hidden.mapper;

import at.ac.uibk.mcsconnect.bookingrepo.api.BookingRepo;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.functional.common.Tuple;
import at.ac.uibk.mcsconnect.http.api.RecordingInstanceIntermediatePost;
import at.ac.uibk.mcsconnect.http.impl.hidden.assembler.RecordingInstanceAssembler;
import at.ac.uibk.mcsconnect.http.impl.hidden.assembler.RecordingInstanceIntermediate;
import at.ac.uibk.mcsconnect.http.impl.hidden.assembler.TerminalAssembler;
import at.ac.uibk.mcsconnect.http.impl.hidden.assembler.UserAssembler;
import at.ac.uibk.mcsconnect.http.impl.hidden.assembler.VersionAssembler;
import at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject.TerminalDTO;
import at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject.UserDTO;
import at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject.VersionDTO;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.person.api.UserFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstance;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstanceFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomRepo;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MultivaluedMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static at.ac.uibk.mcsconnect.http.impl.hidden.filter.Utility.getHeaderValue;
import static at.ac.uibk.mcsconnect.http.impl.hidden.mapper.Conventions.USER_REQUEST_KEY;

/**
 * My job is to keep the domain object dependencies out of the resources, yet still support
 * JAX-RS filters.
 */
public class RequestUtilities {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestUtilities.class);

    private final RoomRepo roomRepo;

    public RequestUtilities(RoomRepo roomRepo) {
        this.roomRepo = roomRepo;
    }

    // /**
    //  * If CXF would support full JAX-RS functionality, then this would work.
    //  * @param contextConfiguration unsupported by CXF as of version 3.2.0
    //  * @return
    //  */
    //public Result<TerminalDTO> getTerminalAssociatedWithRequest(Configuration contextConfiguration) {
    //    Result<Terminal> rTerminal = getConfigValueAsType(TERMINAL_REQUEST_KEY, Terminal.class, contextConfiguration);
    //    rTerminal.forEachOrFail(e -> LOGGER.info(String.format("getConfigValueAsType returned: ", e.getName()))).forEach(RequestUtilities::log);
    //    return rTerminal.map(t -> new TerminalAssembler().writeDTO(t));
    //}

    private static Result<TerminalDTO> terminalToDTO(Tuple<Room, Terminal> tuple) {
        Result<Tuple<Room, Terminal>> rTuple = Result.of(tuple);
        return rTuple.flatMap(t -> RequestUtilities.terminalAssembler(t._2));
    }

    private static Result<TerminalDTO> terminalAssembler(Terminal terminal) {
        try {
            return Result.success(new TerminalAssembler().writeDTO(terminal));
        } catch (Exception e) {
            return Result.failure(String.format("Could not assemble terminal as terminal dto: %s", terminal));
        }
    }

    //public Result<RoomDTO> getRoomAssociatedWithRequest(Configuration contextConfiguration) {
    //    Result<Room> rRoom = getConfigValueAsType(ROOM_REQUEST_KEY, Room.class, contextConfiguration);
    //    return rRoom.map(r -> new RoomAssembler().writeDTO(r));
    //}

    public Result<UserDTO> getUserAssociatedWithRequest(Configuration contextConfiguration) {
        Result<User> rUser = getConfigValueAsType(USER_REQUEST_KEY, User.class, contextConfiguration);
        return rUser.map(u -> new UserAssembler().writeDTO(u));
    }

    public Result<VersionDTO> getVersion() {
        return Result.of(new VersionAssembler().fetchAndCreateDTO());
    }

    /**
     * Extract a JAX-RS {@link Configuration} key and return the value.
     *
     * @param key
     * @param realType
     * @param contextConfiguration
     * @param <T>
     * @return
     */
    private <T> Result<T> getConfigValueAsType(final String key, final Class<T> realType, final Configuration contextConfiguration) {
        Result<String> rKey = Result.of(key);
        return rKey.flatMap( k -> {
            Object obj = contextConfiguration.getProperty(k);
            if (obj != null) {
                return Result.success((T) obj);
            } else {
                return Result.failure(String.format("Key \"%s\" not found in jax-rs context: %s", key, contextConfiguration.getProperties()));
            }
        });
    }

    public static Result<String> extractHostFromContext(MessageContext context) {
        MultivaluedMap<String, String> headers = extractHeaders(context);
        Result<String> rHost = getHeaderValue("x-forwarded-for", headers).orElse(() -> getHeaderValue("host", headers));
        return rHost;
    }

    public static Result<User> extractUserFromContext(MessageContext context, UserFactory userFactory) {
        MultivaluedMap<String, String> headers = extractHeaders(context);
        Result<String> rRemoteUser = getHeaderValue("eppn", headers).flatMap(eppn -> eppnToCKennung(eppn));// remote_user is qualified (e-mail); take up until @
        Result<String> rDisplayName = getHeaderValue("displayName", headers);
        Result<String> rEMailAddress = getHeaderValue("mail", headers);
        Result<User> rUser = rRemoteUser
                .flatMap(id -> rDisplayName
                        .flatMap(name -> rEMailAddress
                                .map(email -> userFactory.create(
                                        id,
                                        name,
                                        email))));
        return rUser;
    }

    /**
     * This function assumes that remoteUser is
     * formatted as an e-mail address (like uibk's sso provider does)
     * and shibd sets the header.
     *
     * @param remoteUser
     * @return
     */
    private static Result<String> remoteUserToEmail(String remoteUser) {
        Result<String> rStart = Result.of(remoteUser);
        return rStart.flatMap(n -> {
            try {
                return Result.success(n.split("@")[0]);
            } catch (Exception e) {
                return Result.failure(String.format("Could not extract email from remote_user: %s", remoteUser));
            }
        });
    }

    private static Result<String> eppnToCKennung(String eppn) {
        Result<String> rEppn = Result.of(eppn);
        return rEppn.flatMap(x -> {
            try {
                return Result.success(x.split("@")[0]);
            } catch (Exception e) {
                return Result.failure(String.format("Could not extract c-kennung from eppn: %s", eppn));
            }
        });
    }

    public static MultivaluedMap<String, String> extractHeaders(MessageContext context) {
        return context.getHttpHeaders().getRequestHeaders();
    }

    /**
     * Helper method to convert a {@link RecordingInstanceIntermediatePost}
     * implemented by the {@link at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject.RecordingInstanceDTO}
     * into a {@link RecordingInstanceIntermediate}.
     *
     * Also a good testpoint.
     *
     * @param bookingRepo
     * @param recordingInstanceFactory
     * @param recordingInstanceIntermediatePost
     * @return
     */
    private static Result<RecordingInstanceIntermediate> recordingInstancePostToRecordingInstanceIntermediate(
            BookingRepo bookingRepo,
            RecordingInstanceFactory recordingInstanceFactory,
            RecordingInstanceIntermediatePost recordingInstanceIntermediatePost
    ) {
        Result<BookingRepo> rBookingRepo = Result.of(bookingRepo, "Booking repo may not be null");
        Result<RecordingInstanceFactory> rRecordingInstanceFactory = Result.of(recordingInstanceFactory, "Recording instance factory may not be null");
        Result<RecordingInstanceIntermediatePost> rDto = Result.of(recordingInstanceIntermediatePost, "Data transfer object may not be null");

        return rBookingRepo
                .flatMap(bookRepo -> rRecordingInstanceFactory
                        .flatMap(recFactory -> rDto
                                .map(dto ->
                                        new RecordingInstanceIntermediate(
                                                bookingRepo,
                                                recordingInstanceFactory,
                                                dto.getBookingId(),
                                                dto.getStopTime(),
                                                dto.getRecordingName(),
                                                dto.getRecordingRunningState()))));
    }


    /**
     * Helper method to convert a {@link RecordingInstanceIntermediate} into a {@link RecordingInstance}.
     *
     * Critical functionality is located in {@link RecordingInstanceIntermediate#apply(Room, User)}.
     * The decision-making for how to update the {@link RecordingInstance} at a {@link Room}
     * is made here.
     *
     * @return
     */
    private static Result<RecordingInstance> recordingInstanceIntermediateToRecordingInstance(RecordingInstanceIntermediate recordingInstanceIntermediate, Room room, User user) {
        Result<RecordingInstanceIntermediate> rRecordingInstanceIntermediate = Result.of(recordingInstanceIntermediate, "Recording Instance Intermediate may not be null.");
        Result<Room> rRoom = Result.of(room, "Room may not be null");
        Result<User> rUser = Result.of(user, "User may not be null");
        return rRecordingInstanceIntermediate.flatMap(recInt -> rRoom.flatMap(rom -> rUser.flatMap(usr -> recInt.apply(rom, usr))));
    }

    /**
     * TODO Consider using to simplify the http layer
     *
     * This is an example of what is possible
     *
     * */
    public static Function<RecordingInstanceFactory, Function<BookingRepo, Function<Room, Function<User, Function<RecordingInstanceIntermediatePost, Result<RecordingInstance>>>>>> recording_instance_post_to_recording_instance =
             recFactory -> bookRepo -> room -> user -> post -> recordingInstanceDTOToRecordingInstance(post, room, user, bookRepo, recFactory);

    /**
     * The main function for the http layer
     *
     * It creates a {@link RecordingInstanceIntermediate} for a DTO
     * and
     */
    public static Result<RecordingInstance> recordingInstanceDTOToRecordingInstance(
            RecordingInstanceIntermediatePost post,
            Room room,
            User user,
            BookingRepo bookingRepo,
            RecordingInstanceFactory recordingInstanceFactory) {
        Result<RecordingInstanceIntermediate> rRecordingInstanceIntermediate = recordingInstancePostToRecordingInstanceIntermediate(bookingRepo, recordingInstanceFactory, post);
        Result<Room> rRoom = Result.of(room, "Room may not be null");
        Result<User> rUser = Result.of(user, "User may not be null");
        return rRoom
                .flatMap(rom -> rUser
                        .flatMap(usr -> rRecordingInstanceIntermediate
                                .flatMap(recInt -> recordingInstanceIntermediateToRecordingInstance(recInt, room, user))));
    }

    public static Result<RecordingInstance> getRecordingInstanceAsResult(Room room) {
        Result<Room> rRoom = Result.of(room, "Room may not be null");
        return rRoom.flatMap(r ->  {
            return r.getRecordingInstance().isPresent()
                    ? Result.success(r.getRecordingInstance().get())
                    : Result.failure("No recording instance present");
        });
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
    public static Result<Tuple<Room, Terminal>> getTerminalByHost(RoomRepo roomRepo, String host) {
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
                : Result.failure(String.format("Failed to find terminal by host (ip/hostname): %s", host)); // TODO replace with requestContext.abortWith(Response.serverError());
    }

    public static String toRequestMsg(String msg) {
        return String.format("Processing request from source identified as: %s", msg);
    }
    private static void log(String s) {
        LOGGER.info(String.format("%s", s));
    }

    private static void logInfo(String msg) {
        LOGGER.info(msg);
    }
    private static void logError(String errMsg) {
        LOGGER.error(errMsg);
    }
    private static void logWarning(String errMsg) {
        LOGGER.warn(errMsg);
    }
}
