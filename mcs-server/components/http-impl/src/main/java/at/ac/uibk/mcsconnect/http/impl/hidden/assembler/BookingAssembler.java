package at.ac.uibk.mcsconnect.http.impl.hidden.assembler;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject.BookingDTO;

public class BookingAssembler implements Assembler<Booking, BookingDTO> {

    public BookingDTO writeDTO(Booking booking) {
        return new BookingDTO(
                booking.getBookingId(),
                booking.getTimeBegin(),
                booking.getTimeEnd(),
                booking.getResourceId(),
                booking.getRoomName(),
                booking.getCourseId(),
                booking.getGroupId(),
                booking.getTermId(),
                booking.getCourseNumber(),
                booking.getCourseName(),
                booking.getGroupNumber()
        );
    }

    //public Set<BookingDTO> getBookings(UserDTO userDTO) {
//
    //    User user = new UserAssembler().readDTO(userDTO);
//
    //    GetBookingsProcedure program = new GetBookingsProcedure(user);
    //    Set<Booking> bookings = program.exec();
//
    //    return bookings.stream().map(b -> new BookingAssembler().writeDTO(b)).collect(Collectors.toSet());
    //}
}
