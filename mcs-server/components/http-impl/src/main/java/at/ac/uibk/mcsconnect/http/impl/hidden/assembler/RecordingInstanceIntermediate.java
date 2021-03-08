package at.ac.uibk.mcsconnect.http.impl.hidden.assembler;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.bookingrepo.api.BookingRepo;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstance;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstanceFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

import static at.ac.uibk.mcsconnect.common.api.StrUtils.parseAsEnum;
import static at.ac.uibk.mcsconnect.common.api.StrUtils.parseAsLocalDateTime;

/**
 * Intermediate class to store input values from client until they
 * can be finally constructed as a {@link RecordingInstance}.
 * <p>
 * This allows the server to control certain aspects of the {@link RecordingInstance}
 * such as read-only booking details and recording owner ({@link User}).
 */
@Schema(name = "RecordingInstanceIntermediate",
        description = "This is how the recording instance is created and changed. It provides indirect access " +
                "to the writable fields in the recording instance.",
        accessMode = Schema.AccessMode.WRITE_ONLY
)
public class RecordingInstanceIntermediate {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordingInstanceIntermediate.class);

    //LoginService loginService; // TODO not implemented. Would replace the User input.

    BookingRepo bookingRepo;

    RecordingInstanceFactory recordingInstanceFactory;

    @Schema(required = true, description = "The ID of a booking. It must be provided on the first call, but may be left out on subsequent calls.")
    public final String bookingId;
    @Schema(required = false, allowableValues = "The stop time must not exceed the maximum time limit.", description = "The stop time according to the ISO 8601 instant format without the time zone identifier, because the university database engineers decided not to include time zones!")
    public final String stopTime;
    @Schema(required = false, description = "An optional recording name to overwrite the default name.")
    public final String recordingName;
    @Schema(required = false, description = "The running state acts as a switch between states {STOPPED, RECORDING}. Once the state RECORDING has been set, nothing else may be changed.")
    public final String recordingRunningState;

    @JsonCreator
    public RecordingInstanceIntermediate(
            BookingRepo bookingRepo,
            RecordingInstanceFactory recordingInstanceFactory,
            @JsonProperty("bookingId") String bookingId,
            @JsonProperty("stopTime") String stopTime,
            @JsonProperty("recordingName") String recordingName,
            @JsonProperty("recordingRunningState") String recordingRunningState) {
        this.bookingRepo = bookingRepo;
        this.recordingInstanceFactory = recordingInstanceFactory;

        // STUFF THE CLIENT CAN SEND
        this.bookingId = bookingId;
        this.stopTime = stopTime;
        this.recordingName = recordingName;
        this.recordingRunningState = recordingRunningState;
    }

    // TODO handle Jackson's Caused by: com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException: Unrecognized field "recorderRunningState" (class at.ac.uibk.unit.recording.RecordingInstanceIntermediate), not marked as ignorable (4 known properties: "recordingRunningState", "bookingId", "stopTime", "recordingName"])
    /**
     * This algorithm works by examining the desired state (the class members)
     * and the existing state (@link RecordingInstance}.
     *
     * new version cannot assume a room has a user, so pass it in.
     *
     *
     * */
    public Result<RecordingInstance> apply(Room room, User user) { // Room input because that is what the http service layer has access too.
        LOGGER.debug(String.format("%s.apply(%s) called.", this, room));

        // SETUP ALL PARAMETERS: SUCCEED OR FAIL HERE (fail in assertions with error message)
        Result<Room> rRoom = RecordingInstanceAssertions.assertValidRoom(room, "Invalid room.");
        Result<User> rUser = Result.of(user, "The user may not be null."); // will be the user or missing rec instance. old version got this from room.

        // Wrap all fields.
        Result<RecordingInstanceFactory> rRecordingInstanceFactory = Result.of(this.recordingInstanceFactory, "Recording Instance Factory may not be null.");
        Result<String> rBookingId = RecordingInstanceAssertions.assertValidBookingId(bookingId, "Invalid booking id. A booking id is always required.");
        Result<RecordingInstance> rOldRecordingInstance = rRoom.flatMap(r -> getExistingRecordingInstance(r)).flatMap(ri -> rBookingId.flatMap(bokId -> RecordingInstanceAssertions.assertSameBooking(ri, bokId, String.format("New booking with id %s detected, ignoring existing recording instance: %s", bokId, ri)))); // valid by comparing booking ids
        Result<Booking> rBooking = rBookingId.flatMap(id -> rUser.flatMap(usr -> getBookingById(usr, id))); //   getBookingById(rUser.successValue(), this.bookingId)); // TODO: User MUST exist, so halfway acceptable, but this should be made more robust.
        Result<LocalDateTime> rDesiredStopTime = parseAsLocalDateTime(this.stopTime)
                .flatMap(stopTime -> rBooking.flatMap(booking -> RecordingInstanceAssertions.assertValidStopTime(stopTime, booking, "Invalid stop time."))); // if fails, use orElse below to determine course of action
        Result<String> rRecordingName = RecordingInstanceAssertions.assertValidRecordingName(this.recordingName, "Invalid recording name."); // TODO: Ensure reset on booking change
        Result<RecorderRunningStatesEnum> rDesiredRecorderRunningState = parseAsEnum(this.recordingRunningState, RecorderRunningStatesEnum.class);

        debugLogResult("Desired booking id", rBookingId);
        debugLogResult("Desired stop time", rDesiredStopTime);
        debugLogResult("Desired recording name", rRecordingName);
        debugLogResult("Desired recorder running state", rDesiredRecorderRunningState);


        /** Needed to create new {@link RecordingInstance} in the old version*/
        //Result<Set<Recorder>> rObservers = RecordingInstanceAssertion.assertValidObservers(room.getRecorders(), "Invalid observers.");

        Result<RecordingInstance> rNewRecordingInstance =
                rRoom
                    .flatMap(rom -> rUser // always the user from the current request. If missing, propogate failure.
                            .flatMap(usr -> rBooking // always booking based on id in request. If missing, propogate failure.
                                    .flatMap(bok -> rDesiredStopTime.orElse(() -> rOldRecordingInstance.map(o -> o.getStopTime()).orElse(() -> rBooking.map(Booking::getTimeEnd)))
                                            .flatMap(stp -> rRecordingName.orElse(() -> rBooking.map(Booking::getTimeBegin).map(startTime -> String.format("%s - %s", startTime.format(DateTimeFormatter.ISO_LOCAL_DATE), bok.getCourseName()))) // TODO default recording name should be made configurable.
                                                    .flatMap(recName -> rDesiredRecorderRunningState.orElse(() -> useDefaultRecordingRunningState())
                                                            .flatMap(recState -> rRecordingInstanceFactory // if desired state not sent, STOPPED is assumed. This has effects:
                                                                .map(recFactory -> recFactory.create(rom, usr, bok, stp, recName, recState))))))));

        // Below is the old side effect of this function. Now it has been made functional and thereby testable.
        //rNewRecordingInstance.forEachOrFail(r -> room.setRecordingInstance(Optional.of(r))).forEach(e -> LOGGER.error(String.format("%s error: %s", this.getClass().getSimpleName(), e))); // FOREACH WILL LOG ERROR BUT NOT THROW IT! It catches everything not caught in stack below.

        // TODO Consider this code for setting the polledRecorderRunningState to expected state and allow it to be updated to actual state slowly later
        //rRoom.flatMap(rume -> rDesiredRecorderRunningState.forEachOrFail(state -> rume.setPolledRecordingRunningState(state))); // This fails whenever this.recordingRunningState is null eg client does not set it
        return rNewRecordingInstance; // This is testable.
    }

    private static <T> void debugLogResult(String label, Result<T> result) {
        if (result.isFailure()) LOGGER.debug(String.format("%s: %s(%s)", label, result.getClass().getSimpleName(), result.failureValue()));
    }

    // TODO: Consider adding abstract assertions ie: Result<User> rUser = Assertion.assertValidUser(getUserLoggedIn), "Bad id") and combine the results.
    private static Result<Room> getLocation(Room location) {
        return Result.of(location, String.format("Invalid location %s.", location));
    }

    private static Result<Set<Recorder>> getRecorders(Room l) {
        return Result.of(l.getRecorders(), String.format("Invalid recorders %s", l.getRecorders()));
    }

    private Result<Booking> getBookingById(User user, String id) {
        try {
            Set<Booking> bookings = bookingRepo.getBookings(user);
            Optional<Booking> oBooking = bookings.stream().filter(b -> b.getBookingId() == Long.valueOf(id)).findFirst();
            return Result.success(oBooking.get());
        } catch (Exception e) {
            return Result.failure(String.format("Could not find any bookings for %s", user));
        }
    }

    private Result<LocalDateTime> useBookingEndTimeForStopTime(Result<Booking> b) {
        return b.map(Booking::getTimeEnd).mapFailure(String.format("Invalid end time in booking %s for stop time.", b));
    }

    private Result<String> getRecordingName() {
        return Result.of(this.recordingName, String.format("Invalid recording name %s", this.recordingName));
    }

    //private Result<String> useBookingDerivedRecordingName(Result<OlatBooking> b) {
    //    return b.map(bo -> String.format("%s - %s", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), bo.getCourseName())).mapFailure("Invalid default recording name from booking.");
    //}

    private Result<RecorderRunningStatesEnum> getRecordingRunningState() {
        return Result.of(this.recordingRunningState).map(RecorderRunningStatesEnum::of).mapFailure(String.format("Invalid set recording running state %s", this.recordingRunningState));
    }

    private Result<RecorderRunningStatesEnum> useDefaultRecordingRunningState() {
        return Result.of(RecorderRunningStatesEnum.STOPPED);
    }

    private static Result<RecordingInstance> getExistingRecordingInstance(Room room) {
        Result<Room> rRoom = Result.of(room);
        return rRoom.flatMap(r -> {
            return r.getRecordingInstance().isPresent()
                ? Result.success(r.getRecordingInstance().get())
                : Result.failure("No previous recording instance exists to fall back on.");
        });
    }

}
