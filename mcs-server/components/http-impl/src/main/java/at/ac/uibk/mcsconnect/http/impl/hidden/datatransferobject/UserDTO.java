package at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject;

import at.ac.uibk.mcsconnect.common.api.Immutable;
import at.ac.uibk.mcsconnect.common.api.Views;
import at.ac.uibk.mcsconnect.http.api.DTO;
import com.fasterxml.jackson.annotation.JsonView;

import java.util.Set;

@Immutable
public class UserDTO implements DTO {

    @JsonView(Views.Public.class)
    private String userId;
    @JsonView(Views.Public.class)
    private String email;
    @JsonView(Views.Public.class)
    private String displayName;
    @JsonView(Views.Public.class)
    private Set<BookingDTO> bookings;

    public UserDTO(String userId, String email, String displayName, Set<BookingDTO> bookings) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.bookings = bookings;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<BookingDTO> getBookings() {
        return bookings;
    }
}
