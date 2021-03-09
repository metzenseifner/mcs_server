package at.ac.uibk.mcsconnect.http.impl.hidden.assembler;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.functional.common.Tuple;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.recorderservice.api.Metadata;
import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstance;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstanceConfiguration;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import org.osgi.service.component.annotations.Reference;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RecordingInstanceAssertions {


    @Reference
    static RecordingInstanceConfiguration recordingInstanceConfiguration;

    // TODO: Fix this test (not high priority because the room is set by the resolver)
    public static boolean isValidLocation(Room l) {
        return l != null;
    }

    public static boolean isValidUser(User u) {
        return u != null;
    }

    public static boolean isValidBookingId(String b) { return b != null; }

    public static boolean isValidBooking(Booking b) {
        return b != null;
    }

    /** Threshold configurable in {@link RecordingInstanceConfiguration} */
    public static boolean isValidStopTime(LocalDateTime s, Long threshold, TimeUnit unit, Booking booking) {
        Result<LocalDateTime> rLocalDateTime = Result.of(s);
        Result<Booking> rBooking = Result.of(booking);
        return rBooking
                .map(Booking::getTimeEnd)
                .map(bookingStopTime -> bookingStopTime.plusMinutes(unit.toMinutes(threshold)))
                .flatMap(bookingStopTimeThreshold -> rLocalDateTime.map( newStopTime -> (newStopTime.isBefore(bookingStopTimeThreshold) || newStopTime.isEqual(bookingStopTimeThreshold)) )).getOrElse(false);
    }

    /**
     * This check must ensure that
     *
     * character set for the SMP351 & not null & n.size < 128
     *
     * @param n
     * @return
     */
    public static boolean isValidRecordingName(String n) {
        return n != null && n.length() <= 127;
    }

    public static boolean isNameChangeAllowed(RecorderRunningStatesEnum state, String n) {
        return state.equals(RecorderRunningStatesEnum.RECORDING) ? false : true;
    }

    public static boolean isValidMetadata(Metadata c) {
        return c != null;
    }

    public static boolean areValidObservers(Set<Recorder> r) {
        return r != null;
    }

    public static boolean isValidRecordingRunningState(RecorderRunningStatesEnum state) {
        return state.equals(RecorderRunningStatesEnum.STOPPED) || state.equals(RecorderRunningStatesEnum.RECORDING);
    }

    public static boolean isRecordingInstanceBookingTheSameBooking(RecordingInstance recordingInstance, String bookingId) {
        return recordingInstance.getBooking().getBookingId() == Long.valueOf(bookingId);
    }

    public static Result<Room> assertValidRoom(Room l, String failureMessage) {
        return Result.of(RecordingInstanceAssertions::isValidLocation, l, failureMessage);
    }

    public static Result<Set<Recorder>> assertValidObservers(Set<Recorder> r, String failureMessage) {
        return Result.of(RecordingInstanceAssertions::areValidObservers, r, failureMessage);
    }

    public static Result<User> assertValidUser(User u, String failureMessage) {
        return Result.of(RecordingInstanceAssertions::isValidUser, u, failureMessage);
    }

    public static Result<String> assertValidBookingId(String b, String failureMessage) {
        return Result.of(RecordingInstanceAssertions::isValidBookingId, b, failureMessage);
    }

    public static Result<Booking> assertValidBooking(Booking b, String failureMessage) {
        return Result.of(RecordingInstanceAssertions::isValidBooking, b, failureMessage);
    }

    public static Result<LocalDateTime> assertValidStopTime(LocalDateTime s, Booking b, Long stopTimeThreshold, TimeUnit stopTimeThresholdUnit,String failureMessage) {
        return RecordingInstanceAssertions.isValidStopTime(s, stopTimeThreshold, stopTimeThresholdUnit, b)
            ? Result.success(s)
            : Result.failure(String.format("Invalid stop time provided: %s", s));
    }

    public static Result<String> assertValidRecordingName(String n, String failureMessage) {
        return Result.of(RecordingInstanceAssertions::isValidRecordingName, n, failureMessage);
    }

    public static Result<Metadata> assertValidMetadata(Metadata c, String failureMessage) {
        return Result.of(RecordingInstanceAssertions::isValidMetadata, c, failureMessage);
    }

    public static Result<RecorderRunningStatesEnum> assertValidRecordingRunningState(RecorderRunningStatesEnum s, String failureMessage) {
        return Result.of(RecordingInstanceAssertions::isValidRecordingRunningState, s, failureMessage);
    }

    public static Result<RecordingInstance> assertSameBooking(RecordingInstance recordingInstance, String bookingId, String failureMessage) {
        return RecordingInstanceAssertions.isRecordingInstanceBookingTheSameBooking(recordingInstance, bookingId)
                ? Result.success(recordingInstance)
                : Result.failure(failureMessage);
    }

    public static Result<String> assertNameChangeAllowed(RecordingInstance recordingInstance, String newName, String failureMessage) {
        return RecordingInstanceAssertions.isNameChangeAllowed(recordingInstance.getRecordingRunningState(), newName)
                ? Result.success(newName)
                : Result.failure(failureMessage);
    }
}
