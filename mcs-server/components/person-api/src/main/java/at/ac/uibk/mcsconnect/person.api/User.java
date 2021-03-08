package at.ac.uibk.mcsconnect.person.api;

import at.ac.uibk.mcsconnect.common.api.Views;
import com.fasterxml.jackson.annotation.JsonView;

public interface User {

    @JsonView(Views.Public.class)
    String getUserId();

    @JsonView(Views.Public.class)
    String getEmail();

    @JsonView(Views.Public.class)
    String getDisplayName();

    //Set<Booking> getBookings();

    //Optional<Booking> getBookingById(String id);
}
