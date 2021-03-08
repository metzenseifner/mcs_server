package at.ac.uibk.mcsconnect.http.impl.hidden.assembler;

import at.ac.uibk.mcsconnect.bookingrepo.api.BookingRepo;
import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.person.api.UserFactory;
import at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject.BookingDTO;
import at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject.UserDTO;
import org.osgi.service.component.annotations.Reference;

import java.util.Set;
import java.util.stream.Collectors;

public class UserAssembler implements Assembler<User, UserDTO> {

    @Reference
    UserFactory userFactory;

    @Reference
    BookingRepo bookingRepo;

    public UserDTO writeDTO(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getEmail(),
                user.getDisplayName(),
                writeBookings(bookingRepo.getBookings(user))
        );
    }

    public void createUser(UserDTO userDTO) {
    }

    protected User readDTO(UserDTO userDTO) {
        return userFactory.create(
                userDTO.getUserId(),
                userDTO.getDisplayName(),
                userDTO.getEmail());
    }

    private Set<BookingDTO> writeBookings(Set<Booking> bookings) {
        return bookings.stream().map(b -> new BookingAssembler().writeDTO(b)).collect(Collectors.toSet());
    }
}
