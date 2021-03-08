package at.ac.uibk.mcsconnect.bookingrepo.impl.integration;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.bookingrepo.api.BookingRepo;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.person.api.User;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class FakeBookingRepo implements BookingRepo {

    private Map<User, Set<Booking>> repo;

    public FakeBookingRepo(Map<User, Set<Booking>> repo) {
        this.repo = repo;
    }

    @Override
    public Set<Booking> getBookings(User user) {
        return repo.get(user);
    }

    @Override
    public Result<Booking> getBookingById(User user, String id) {
        Optional<Booking> oBooking = getBookings(user).stream().filter(u -> u.getBookingId() == Long.valueOf(id)).findFirst();
        return oBooking.isPresent()
                ? Result.success(oBooking.get())
                : Result.failure(String.format("Could not find booking with id \"%s\" for user \"%s\"", id, user));
    }
}
