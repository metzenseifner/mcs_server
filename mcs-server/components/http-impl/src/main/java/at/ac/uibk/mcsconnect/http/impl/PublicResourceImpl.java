package at.ac.uibk.mcsconnect.http.impl;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.bookingrepo.api.BookingRepo;
import at.ac.uibk.mcsconnect.common.api.McsConfiguration;
import at.ac.uibk.mcsconnect.functional.common.Executable;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.functional.common.Tuple;
import at.ac.uibk.mcsconnect.http.api.OldApiEndpoint;
import at.ac.uibk.mcsconnect.http.api.PublicResourceApi;
import at.ac.uibk.mcsconnect.http.api.RecordingInstanceIntermediatePost;
import at.ac.uibk.mcsconnect.http.impl.hidden.TerminalCheckRequired;
import at.ac.uibk.mcsconnect.http.impl.hidden.mapper.RequestUtilities;
import at.ac.uibk.mcsconnect.http.impl.hidden.mapper.VersionOneAdapter;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.person.api.UserFactory;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstance;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstanceConfiguration;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstanceFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomRepo;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.message.Message;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Optional;
import java.util.Set;

import static at.ac.uibk.mcsconnect.http.impl.hidden.mapper.RequestUtilities.recordingInstanceDTOToRecordingInstance;

/**
 * Represents a "Remote Facade" over the domain logic.
 *
 * I unfortunately had to add a bunch of complicated code to support the old APIv1
 * in this new modular environment. Removal of the bloat will require cooperation with
 * client development.
 *
 * See here for CXF-specific stuff https://stackoverflow.com/questions/53495565/returning-a-object-from-filter-back-to-resource
 **
 * By default a new resource class instance is created for each request of that resource. First, the
 * constructor is called, then any injectors are called (@Context). Unfortunately, CXF does not
 * support JAX-RS Configuration injection like Jersey, but it certainly should. Because of this,
 * it is not possible to pass properties from filters to resources using only the jax-rs api.
 * A possible workaround is to use the ServletContext, which supports attribute objects.
 *
 * Classes should ideally depend only on classes in the org.apache.cxf.jaxrs.ext package
 * To support field injection for a Per Request instance of this resource, it is necessary to
 * use RequestScopeResourceFactory, but this again, is not a dep on JAX-RS. Therefore, I
 * chose to use method injection (despite not wanting to clutter inputs for open api doc)
 *
 *
 * <p>
 * <p>
 * https://download.oracle.com/javaee-archive/jax-rs-spec.java.net/jsr339-experts/att-3593/spec.pdf
 */

//@Component// RSA version ... run in karaf: rsa:endpoints to list all endpoints detected
//(// run in karaf: scr:list to see whether org.apache.cxf.dosgi.dsw.handlers.rest.RsProvider is active
//    immediate = true,
//    service = {PublicResourceApi.class},
//    name = "at.ac.uibk.mcsconnect.rest.api", //
//    property = // setup https://github.com/apache/cxf-dosgi/blob/master/provider-rs/src/main/java/org/apache/cxf/dosgi/dsw/handlers/rest/RsProvider.java
//        { //
//            "service.exported.configs=org.apache.cxf.rs", // ids this as a cxf jax-rs resource
//            "org.apache.cxf.rs.httpservice.context=/api", // default cxf
//            //"org.apache.cxf.rs.httpservice.context.properties.*",
//            "service.exported.interfaces=*", //PublicResourceApi.class
//            "org.apache.cxf.rs.address=/public", // defaults to http://localhost:9000/fully/qualified/ClassName.
//            // By default CXF will favor the default json provider
//                /// "aries.rsa.port=8201"
//            "cxf.bus.prop.skip.default.json.provider.registration=true",
//            "org.apache.cxf.rs.provider=com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider"
//                // EntityResolverFilter
//        } // see https://issues.apache.org/jira/browse/DOSGI-140
//) // https://www.mail-archive.com/user@karaf.apache.org/msg04674.html

public class PublicResourceImpl implements PublicResourceApi {

    // IMPORTANT: A JAX-RS Resource implemented by CXF does not have access to the ContainerRequestContext

    //public List<?> getIntents() {
    //    return new ArrayList<>();
    //}

    private final RoomRepo roomRepo;
    private final BookingRepo bookingRepo;
    private final UserFactory userFactory;
    private final RecordingInstanceFactory recordingInstanceFactory;
    private final RequestUtilities requestUtilities;
    private final McsConfiguration mcsConfiguration;
    private final RecordingInstanceConfiguration recordingInstanceConfiguration;





    private static final Logger LOGGER = LoggerFactory.getLogger(PublicResourceImpl.class); // causes conflict when using @Path and cxf-dosgi DS

    //@Context
    //private Configuration contextConfiguration;

    /** CXF JAXRS does not offer a context implementation which can be used to access a request-specific information common for both JAXWS and JAXRS requests */
    //@Context
    //MessageContext messageContext;

    // Keep in mind that the ContainerRequestContext
    // attributes are automatically synced with
    // HttpServletRequest. When using CXF the
    // ContainerRequestContext can not be directly
    // accessed in the Resource, so we need to use
    // HttpServletRequest
    public PublicResourceImpl(
            RoomRepo roomRepo,
            BookingRepo bookingRepo,
            UserFactory userFactory,
            RecordingInstanceFactory recordingInstanceFactory,
            McsConfiguration mcsConfiguration,
            RecordingInstanceConfiguration recordingInstanceConfiguration) {
        this.roomRepo = roomRepo;
        this.bookingRepo = bookingRepo;
        this.userFactory = userFactory;
        this.recordingInstanceFactory = recordingInstanceFactory;
        this.mcsConfiguration = mcsConfiguration;
        this.recordingInstanceConfiguration = recordingInstanceConfiguration;
        this.requestUtilities = new RequestUtilities(roomRepo);
    }

    @Override
    public Response getApiHelp() {
        return Response.ok("This is the api help for MCS connect.").build();
    }

    @TerminalCheckRequired
    @Override
    /**
     *
     *
     * Query for room based on headers identifying terminal (highly insecure, but acceptable risk).
     *
     * TODO I had to make the new modular code work with the old APIv1, so that
     * TODO mcs-client could be used without changes. This should be corrected
     * TODO someday because it makes this endpoint too complicated.
     *
     * Algorithm uses values for keys in this priority:
     * x-forwarded-for
     * host
     *
     * It is not strictly required, but it is possible to set
     * the recordinginstance if only one booking is available.
     *
     * Abort Conditions
     *  - if request cannot be associated with room (the header host or x-forwarded for)
     *  - if user cannot be detected (headers set by shibd)
     *
     */
    public Response whoami(@Context MessageContext context) { // @Context Configuration contextConfiguration does nto
        Result<String> rHost = RequestUtilities.extractHostFromContext(context);
        Result<User> rUser = RequestUtilities.extractUserFromContext(context, userFactory);

        rHost.map(RequestUtilities::toRequestMsg).forEach(PublicResourceImpl::logInfo);

        Result<Room> rRoom =  rHost.flatMap(host -> roomRepo.getRoomForHost(host));
        Result<Set<Booking>> rBookings = rUser.flatMap(user -> getBookings(user, bookingRepo));

        Result<VersionOneAdapter.OldTerminal> rOldTerminal =
                rRoom.flatMap(room -> rUser
                        .flatMap(user -> rHost
                            .flatMap(host -> rBookings
                                .flatMap(bookings -> adaptToOldWhoAmIResponse(room, user, host, bookings)))));

        // ALL ERRORS PROPOGATE UNTIL THIS POINT
        rOldTerminal.forEachOrFail(ot -> LOGGER.info(String.format("apiv1: Old Terminal created: %s", ot))).forEach(e -> logError(String.format("Problem creating APIv1 OldTerminal response, because: %s", e)));
        if (rOldTerminal.isFailure()) {
            return handleFailureResponse(rOldTerminal.failureValue().toString());
        } else {
            return handleSuccessResponse(rOldTerminal.successValue());
        }
    }



    private Result<VersionOneAdapter.OldTerminal> adaptToOldWhoAmIResponse(Room room, User user, String host, Set<Booking> bookings) {
        Result<Room> rRoom = Result.of(room, String.format("Room may not be empty while executing adaptToOldWhoAmIResponse()"));
        Result<User> rUser = Result.of(user, String.format("User may not be null while executing adaptToOldWhoAmIResponse()"));
        Result<String> rHost = Result.of(host, String.format("Host may not be null while executing adaptToOldWhoAmIResponse()"));
        Result<Set<Booking>> rBookings = Result.of(bookings, String.format("Bookings set may not be null while executing adaptToOldWhoAmIResponse()"));
        // TODO This is where the design prob occurs: aggregate obj leak (terminal should not be needed, but it is for this).

        return rRoom.flatMap(rom -> rUser
                .flatMap(usr -> rBookings
                        .flatMap(bokings -> rHost
                                .flatMap(hst -> getTerminalFromHost(rom, hst)
                                        .map(term -> new VersionOneAdapter.OldTerminal(term, usr, rom, bokings, rom.getRecordingInstance()))))));
    }

    @OldApiEndpoint(
            description = "The client checks this every n seconds and uses it to notify the user of an unexpected stop. It only processes 200 reponses."
    )
    @Override
    public Response getPolledRecordingRunningState(Configuration contextConfiguration, String roomId) {
        Result<Room> room = roomRepo.get(roomId);
        //return Response.ok("{ \"runningState\" : \"RECORDING\" }").type(MediaType.APPLICATION_JSON).build();
        //return Response.ok(RecorderRunningStatesEnum.RECORDING).build();
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @Override
    public Response postRecordingInstanceIntermediate(@Context MessageContext context, String roomId, RecordingInstanceIntermediatePost recordingInstanceIntermediatePost) {
        //new RecordingInstanceIntermediateAssembler().createRecordingInstance(roomDTO, recordingInstanceIntermediateDTO);

        // Get room associated with request
        Result<String> rHost = RequestUtilities.extractHostFromContext(context);
        Result<User> rUser = RequestUtilities.extractUserFromContext(context, userFactory);

        /** Log Messages */
        rHost.map(RequestUtilities::toRequestMsg).forEach(PublicResourceImpl::logInfo);
        Result<Room> rRoom = rHost.flatMap(host -> roomRepo.getRoomForHost(host));

        /** Testable function for creating {@link RecordingInstance}. This also leaks domain into the http layer */
        Result<RecordingInstance> rRecordingInstance = rRoom.flatMap(rom -> rUser.flatMap(usr -> // Function RecordingInstanceIntermediatePost -> RecordingInstance
                recordingInstanceDTOToRecordingInstance(
                        recordingInstanceIntermediatePost,
                        rom,
                        usr,
                        bookingRepo,
                        recordingInstanceFactory,
                        recordingInstanceConfiguration
                )
        ));

        /** Set Recording Instance */
        rRoom.flatMap(rom -> rRecordingInstance
                .map(recIns -> new Tuple<>(rom, recIns)))
                .forEachOrFail(a -> a._1.setRecordingInstance(Optional.of(a._2)))
                .forEach(errMsg -> PublicResourceImpl.logError(String.format("postRecordingInstanceIntermediate failed to set recording instance (RoomID: %s, BookingID %s), because: %s", roomId, recordingInstanceIntermediatePost.getBookingId(), errMsg)));

        // TODO It stopped being totally functional above this line  ------------

        /** Function RecordingInstance -> OldRoom */
        Result<VersionOneAdapter.OldRoom> rOldRoom = rRoom.flatMap(rom -> rUser.flatMap(usr -> rRecordingInstance.map(recInstance -> adaptToOldPostRecordingInstanceIntermediateResponse(rom, usr, recInstance))));

        /** Function OldRoom -> Response */
        return handlePostRecordingInstanceIntermediateResponse(rOldRoom);

    }

    @Override
    public Response getRoom(String id) {
        Result<Room> rRoom = roomRepo.get(id);
        return rRoom.isSuccess()
                ? handleSuccessResponse(rRoom.successValue())
                : handleFailureResponse(rRoom.failureValue().toString());
    }

    @Override
    public Response getBookings(@Context MessageContext context, String userId) {
        Result<User> rUser = RequestUtilities.extractUserFromContext(context, userFactory);
        Result<Booking> rBooking = rUser.flatMap(usr -> bookingRepo.getBookingById(usr, userId));
        return rBooking.isSuccess()
                ? handleSuccessResponse(rBooking.successValue())
                : handleFailureResponse(rBooking.failureValue().toString());
    }

    private static Response handlePostRecordingInstanceIntermediateResponse(Result<VersionOneAdapter.OldRoom> rOldRoom) {
        return rOldRoom.isSuccess()
                ? handleSuccessResponse(rOldRoom.successValue())
                : handleFailureResponse(rOldRoom.failureValue().toString());
    }

    private VersionOneAdapter.OldRoom adaptToOldPostRecordingInstanceIntermediateResponse(Room room, User user, RecordingInstance recordingInstance) {
        return new VersionOneAdapter.OldRoom(room, bookingRepo.getBookings(user), user, Optional.of(recordingInstance));
    }

    @Override
    public Response getVersion() {
        //return Response.ok().entity(new VersionDTO(mcsConfiguration.getVersion())).build(); // TODO get working
        return Response.ok().entity(mcsConfiguration.getVersion()).build();
    }

    private Result<Terminal> getTerminalFromHost(Room room, String host) {
        Optional<Terminal> terminal = room.getTerminals().stream().filter(t -> t.getNetworkTarget().getHost().equals(host)).findFirst();
        return terminal.isPresent()
                ? Result.success(terminal.get())
                : Result.failure(String.format("Room %s does not own host: %s", room, host));
    }

    private static Result<Set<Booking>> getBookings(User user, BookingRepo bookingRepo){
        Result<User> rUser = Result.of(user, "User may not be null");
        Result<BookingRepo> rBookingService = Result.of(bookingRepo, String.format("BookingService may not be null"));
        return rUser.flatMap(usr -> rBookingService.flatMap(bokService -> {
            try {
                return Result.success(bokService.getBookings(usr));
            } catch (Exception e) {
                return Result.failure(String.format("Failed to fetch bookings for user \"%s\" because: %s", user, e));
            }
        }));
    }

    private static <T> Response defaultOk(T inputDTO) {
        return Response.status(Response.Status.ACCEPTED).entity(inputDTO).build();
    }

    private static Response defaultNotFound(String msg) {
        String json = new JSONObject()
                .put("statuscode", Response.Status.NOT_FOUND.getStatusCode())
                .put("msg", msg).toString();
        return Response.status(Response.Status.NOT_FOUND)
                .entity(json)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

    /**
     * Example diagnostics program that takes a CXF-specific type.
     *
     * @param message, acquired with Message message = PhaseInterceptorChain.getCurrentMessage();
     * @return
     */
    private Executable extractInfo(Message message) {
        return () -> {
            LOGGER.info(String.format("Message %s", message));
            LOGGER.info(String.format("Exchange %s", message.getExchange()));
            LOGGER.info(String.format("InMessage: %s", message.getExchange().getInMessage()));
            LOGGER.info(String.format("InterceptorChain: %s", message.getExchange().getInMessage().getInterceptorChain()));
            LOGGER.info(String.format("InMessageExchange: %s", message.getExchange().getInMessage().getExchange()));
        };
    }

    private static Response handleSuccessResponse(Object obj) {
        return Response.ok().entity(obj).build();
    }

    private static Response handleFailureResponse(String msg) {
        JSONObject json = new JSONObject();
        json.put("statuscode", Response.Status.UNAUTHORIZED);
        json.put("message", msg);
        return Response.status(Response.Status.NOT_FOUND).entity(json.toString()).build();
    }

    private static void logError(String errMsg) {
        LOGGER.error(errMsg);
    }
    private static void logWarning(String msg) {
        LOGGER.warn(msg);
    }
    private static void logInfo(String msg) {
        LOGGER.info(String.format("%s", msg));
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
