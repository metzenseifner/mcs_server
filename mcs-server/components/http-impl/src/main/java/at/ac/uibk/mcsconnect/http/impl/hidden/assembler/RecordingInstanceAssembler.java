package at.ac.uibk.mcsconnect.http.impl.hidden.assembler;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.bookingrepo.api.BookingRepo;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.http.api.RecordingInstanceIntermediatePost;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstance;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstanceFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static at.ac.uibk.mcsconnect.common.api.StrUtils.parseAsEnum;
import static at.ac.uibk.mcsconnect.common.api.StrUtils.parseAsLocalDateTime;

public class RecordingInstanceAssembler {//implements Assembler<RecordingInstanceIntermediate, RecordingInstanceIntermediateDTO> {

    BookingRepo bookingRepo;

    RecordingInstanceFactory recordingInstanceFactory;

    /** New (semi(-) Functional Factory that takes missing info and makes are new recording instance */
    //public static Result<RecordingInstance> create(RecordingInstanceIntermediatePost post, Room room, User user, BookingRepo bookingRepo, RecordingInstanceFactory recordingInstanceFactory) {
//
    //    Result<RecordingInstanceIntermediatePost> rPost = Result.of(post, "RecordingInstanceIntermediatePost may not be null");
    //    Result<Room> rRoom = Result.of(room, "Room may not be null.");
    //    Result<User> rUser = Result.of(user, "User may not be null.");
    //    Result<BookingRepo> rBookingService = Result.of(bookingRepo, "Booking Service may not be null.");
    //    Result<RecordingInstanceFactory> rRecordingInstanceFactory = Result.of(recordingInstanceFactory, "Recording Instance Factory may not be null");
    //    Result<String> rBookingId = rPost.flatMap(p -> Result.of(p.getBookingId(), "Booking id may not be null.").flatMap(bookingId -> RecordingInstanceAssertion.assertValidBookingId(bookingId, String.format("Invalid booking id: %s", bookingId))));
    //    Result<Booking> rBooking = rBookingId.flatMap(bookingId -> rBookingService.flatMap(bokingService -> bokingService.getBookingById(user, bookingId)));
    //    Result<LocalDateTime> rStopTime = rPost.flatMap(p -> parseAsLocalDateTime(p.getStopTime()).flatMap(stopTime -> rBooking.flatMap(booking -> RecordingInstanceAssertion.assertValidStopTime(stopTime, booking, String.format("Invalid stop time \"%s\" for booking: \"%s\"", stopTime, booking)))));
    //    Result<String> rRecordingName = rPost.flatMap(p -> Result.of(p.getRecordingName()).flatMap(recordingName -> RecordingInstanceAssertion.assertValidRecordingName(recordingName, String.format("Invalid recording name: %s", recordingName))));
    //    Result<RecorderRunningStatesEnum> rRecordingRunningState = parseAsEnum(post.getRecordingRunningState(), RecorderRunningStatesEnum.class).flatMap(recordingRunningState -> RecordingInstanceAssertion.assertValidRecordingRunningState(recordingRunningState, String.format("Invalid recording running state: %s", recordingRunningState)));
    //    Result<RecordingInstance> existingRecordingInstance = rRoom.flatMap(r -> detectExistingRecordingInstance(r));
//
    //    // TODO Critical part of algorithm here. Should probably be abstracted.
    //    Result<RecordingInstance> rRecordingInstance = rRoom.flatMap(rom -> rUser
    //            .flatMap(usr -> rBooking
    //                    .flatMap(booking -> rStopTime.orElse(() -> existingRecordingInstance.map(existing -> existing.getStopTime()).orElse(() -> rBooking.map(Booking::getTimeEnd))) // TODO without orElse, fails with null value
    //                            .flatMap(stopTime -> rRecordingName.orElse(() -> rBooking.map(Booking::getTimeBegin).map(startTime -> String.format("%s - %s", startTime.format(DateTimeFormatter.ISO_LOCAL_DATE), booking.getCourseName()))) // TODO without orElse, fails with null value
    //                                    .flatMap(recordingName -> rRecordingRunningState.orElse(() -> useDefaultRecordingRunningState()) // TODO without orElse, fails with null value
    //                                            .flatMap(recordingRunningState -> rRecordingInstanceFactory
    //                                                    .map(recFactory -> recordingInstanceFactory.create(rom, usr, booking, stopTime, recordingName, recordingRunningState))))))));
//
    //    return rRecordingInstance;
    //}

    public static Result<RecordingInstance> assemble(Room room,
                                                     User user,
                                                     RecordingInstanceIntermediatePost dto,
                                                     BookingRepo bookingRepo,
                                                     RecordingInstanceFactory recordingInstanceFactory) {
        RecordingInstanceIntermediate recordingInstanceIntermediate = new RecordingInstanceIntermediate(bookingRepo,
                recordingInstanceFactory,
                dto.getBookingId(),
                dto.getStopTime(),
                dto.getRecordingName(),
                dto.getRecordingRunningState()
                );

        return recordingInstanceIntermediate.apply(room, user);
    }

    private static Result<RecorderRunningStatesEnum> useDefaultRecordingRunningState() {
        return Result.of(RecorderRunningStatesEnum.STOPPED);
    }
    private static Result<RecordingInstance> detectExistingRecordingInstance(Room room) {
        Result<Room> rRoom = Result.of(room);
        return rRoom.flatMap(r -> {
            return r.getRecordingInstance().isPresent()
                    ? Result.success(r.getRecordingInstance().get())
                    : Result.failure("No recording instance exists.");
        });
    }
}
