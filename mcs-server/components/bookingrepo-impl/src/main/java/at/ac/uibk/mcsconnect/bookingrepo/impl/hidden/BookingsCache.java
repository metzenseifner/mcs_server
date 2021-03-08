package at.ac.uibk.mcsconnect.bookingrepo.impl.hidden;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BookingsCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingsCache.class);

    private static long expirationTime;

    @Reference
    private EsbClientCalls esb;

    private final List<Booking> bookings;

    /**
     * A value cache.
     */
    public BookingsCache(List<Booking> bookings, long expirationTime, TimeUnit timeunit) {
        LOGGER.debug(String.format("%s created with time limit of %s %s", getClass().getSimpleName(), expirationTime, timeunit.toString()));
        this.bookings = Collections.unmodifiableList(bookings);
        this.expirationTime = System.currentTimeMillis() + timeunit.toMillis(expirationTime);
    }

    /**
     * Must not return the original reference to allow
     * garbage collector to do its job.
     *
     * @return A copy of the bookings.
     */
    public Set<Booking> getBookings() throws TimeoutException {
        if (isExpired()) { // TODO implement timeout
            String warnMessage = String.format("%s expired.", this);
            throw new TimeoutException(warnMessage);
            // it is possible to return an empty list here, but that could be misleading for the caller
        }
        return new HashSet<>(bookings);

    }

    public boolean isExpired() {
        LOGGER.debug(String.format("%s: expirationTime: %s", this, expirationTime));
        return (System.currentTimeMillis() < expirationTime) ? false : true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(1024);
        for (Booking b : bookings) {
            sb.append(b);
        }
        return String.format("%s: %s", getClass().getSimpleName(), sb.toString());
    }
}