package at.ac.uibk.mcsconnect.bookingrepo.api;

import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.person.api.User;

import java.util.Set;

/**
 * This repository is read-only, because it is a source
 * of truth.
 */
public interface BookingRepo {

    Set<Booking> getBookings(User user);
    Result<Booking> getBookingById(User user, String id);

}
