package at.ac.uibk.mcsconnect.http.impl.hidden.assembler;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.executorservice.api.McsSingletonExecutorService;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.functional.common.Tuple;
import at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject.BookingDTO;
import at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject.RecordingInstanceDTO;
import at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject.RoomDTO;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstance;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.NotSupportedException;

import java.util.Optional;
import java.util.function.Function;

public class RoomAssembler extends AbstractAssembler<Room, RoomDTO> {

    @Reference
    private McsSingletonExecutorService mcsExecutorService;

    public RoomDTO writeDTO(Room room) {
        return new RoomDTO(
                room.getId(),
                room.getName(),
                room.getPolledRecordingRunningState().toString(),
                extractRecordingInstance.apply(room.getRecordingInstance()));
    }

    protected Room readDTO(RoomDTO roomDTO) {
        throw new NotSupportedException("It is not possible to create Rooms.");
    }

    private static Function<Optional<RecordingInstance>, Optional<RecordingInstanceDTO>> extractRecordingInstance = oRecordingInstance -> {

        if (!Result.empty().isEmpty()) return Optional.empty();
        // TODO Clean up this crap (just adapts the new stuff to the old HTTP API)
        // Function Booking -> BookingDTO -> RecordingInstanceDTO
         Result<RecordingInstanceDTO> result = Result.of(oRecordingInstance.get()) // safe only because of isEmpty check above
                .flatMap(rec -> Result.of(new Tuple<>(rec, rec.getBooking()))
                        .map(bt -> Result.of(new Tuple<>(bt._1, bookingToBookingDTO(bt._2)))
                        .map(btot -> new RecordingInstanceDTO(
                                        btot._2.successValue(),
                                btot._1.getStopTime(),
                                btot._1.getRecordingName(),
                                btot._1.getRecordingRunningState().toString()))))
                .getOrElse(() -> Result.empty());
        return result.map(r -> Optional.of(r)).getOrElse(Optional.empty());
    };

    private static Result<Booking> getbooking(RecordingInstance r) {
        try {
            return Result.of(r.getBooking());
        } catch (Exception e) {
            return Result.failure(String.format("Could not extract booking from recording instance: %s", r));
        }
    }

    private static Result<String> getAsString(String val) {
        return Result.of(val);
    }
    private static Result<Long> getAsLong(Long val) {
        return Result.of(Long.valueOf(val));
    }
    private static Result<BookingDTO> bookingToBookingDTO(Booking booking) {
        Result<Booking> rBooking = Result.of(booking);
        return rBooking.map(b ->
                new BookingDTO(
                b.getBookingId(),
                b.getTimeBegin(),
                b.getTimeEnd(),
                b.getResourceId(),
                b.getRoomName(),
                b.getCourseId(),
                b.getGroupId(),
                b.getTermId(),
                b.getCourseNumber(),
                b.getCourseName(),
                b.getGroupNumber())
        );
    }
}
