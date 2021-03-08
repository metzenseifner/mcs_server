package at.ac.uibk.mcsconnect.bookingrepo.impl;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.bookingrepo.api.BookingRepo;
import at.ac.uibk.mcsconnect.bookingrepo.impl.hidden.BookingsCache;
import at.ac.uibk.mcsconnect.bookingrepo.impl.hidden.EsbClientCalls;
import at.ac.uibk.mcsconnect.bookingrepo.impl.hidden.EsbRolesEnum;
import at.ac.uibk.mcsconnect.bookingrepo.impl.hidden.TvrBooking;
import at.ac.uibk.mcsconnect.bookingrepo.impl.hidden.TvrBookingImpl;
import at.ac.uibk.mcsconnect.bookingrepo.impl.hidden.TvrBookings;
import at.ac.uibk.mcsconnect.common.api.StrUtils;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.functional.osgi.OsgiPropertyReader;
import at.ac.uibk.mcsconnect.person.api.User;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component(
        name = "at.ac.uibk.mcsconnect.bookingservice.impl.TvrBookingServiceImpl",
        immediate = true,
        scope = ServiceScope.SINGLETON
)
public class TvrBookingRepoImpl implements BookingRepo {

    private static final Logger LOGGER = LoggerFactory.getLogger(TvrBookingRepoImpl.class);

    // config keys
    private static final String CFG_BOOKING_TIME_THRESHOLD = "booking.time.threshold";
    private static final String CFG_BOOKING_TIME_THRESHOLD_UNIT = "booking.time.threshold.unit";

    // current values
    private Long bookingTimeThreshold;
    private ChronoUnit bookingTimeThresholdUnit;

    @Reference // dynamic ref
    EsbClientCalls esbClient;

    BookingsCache bookingsCache;

    @Activate
    public TvrBookingRepoImpl(final Map<String,?> properties) {
        handleProperties(properties);
    }

    @Modified
    public void modified(final Map<String,?> properties) {
        handleProperties(properties);
    }

    private void handleProperties(final Map<String,?> properties) {
        OsgiPropertyReader reader = OsgiPropertyReader.create(properties);
        this.bookingTimeThreshold = reader.getAsLong(CFG_BOOKING_TIME_THRESHOLD).getOrElse(TvrBookingServiceDefaults.DEFAULT_BOOKING_TIME_THRESHOLD);
        this.bookingTimeThresholdUnit = reader.getAsChronoUnit(CFG_BOOKING_TIME_THRESHOLD_UNIT).getOrElse(TvrBookingServiceDefaults.DEFAULT_BOOKING_TIME_THRESHOLD_UNIT);
    }

    public Set<Booking> getBookings(User user) {
        // TODO Get bookingsCache working again using new version
        //if (bookingsCache == null || bookingsCache.isExpired()) {
        //    bookingsCache = new BookingsCache(fetchBookings(), BOOKING_CACHE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        //}

        TvrBookings tvrBookingsDeserialized = esbClient.fetchBookingsForUserId(
                user.getUserId(),
                EsbRolesEnum.VORTRAGENDER.toString(),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString(),
                LocalDateTime.now().plus(this.bookingTimeThreshold, this.bookingTimeThresholdUnit).truncatedTo(ChronoUnit.SECONDS).toString());// TODO make configurable

        List<TvrBookingImpl> tvrBookings = tvrBookingsDeserialized.getBookings();
        return convertListOfTvrBookingIntoSetOfBooking(tvrBookings);
    }

    private Set<Booking> convertListOfTvrBookingIntoSetOfBooking(List<TvrBookingImpl> tvrBookings) {
        Set<Booking> bookings = new HashSet<>();

        for (TvrBooking b : tvrBookings) {
            Result<String> rBookingId = Result.of(b.getExternalBookingId(), "Failed to parse external booking id.");
            Result<String> rTimeStart = Result.of(b.getDateFrom(), "Failed to parse dateFrom");
            Result<String> rTimeEnd = Result.of(b.getDateUntil(), "Failed to parse dateUntil");
            Result<String> rResourceId = Result.of(b.getResourceId(), "Failed to parse resourceId");
            Result<String> rResourceName = Result.of(b.getResourceName(), "Failed to parse resourceName");
            Result<String> rRoomName = Result.of(b.getLocationName(), "Failed to parse locationName");
            Result<String> rCourseId = Result.of(b.getExternalCourseId(), "Failed to parse externalCourseId");
            Result<String> rCourseName = Result.of(b.getLvTitle(), "Failed to parse lvTitle");
            Result<String> rCourseNumber = Result.of(b.getLvNr(), "Failed to parse lvNr");
            Result<String> rTermName = Result.of(b.getTermId(), "Failed to parse termId");
            Result<String> rGroupId = Result.of(b.getExternalGroupId(), "Failed to parse groupId");
            Result<String> rGroupIndex = Result.of(b.getGroupId(), "Failed to parse groupIndex");

            Result<Booking> booking =
                    rBookingId.flatMap(StrUtils::parseAsLong)
                            .flatMap(bId -> rTimeStart.flatMap(StrUtils::parseAsLocalDateTime)
                                    .flatMap(timeS -> rTimeEnd.flatMap(StrUtils::parseAsLocalDateTime)
                                            .flatMap(timeE -> rResourceId.flatMap(StrUtils::parseAsLong)
                                                    .flatMap(resId -> rResourceName
                                                            .flatMap(resName -> rRoomName
                                                                    .flatMap(roomName -> rCourseId.flatMap(StrUtils::parseAsLong)
                                                                            .flatMap(crsId -> rCourseName
                                                                                    .flatMap(crsName -> rCourseNumber.flatMap(StrUtils::parseAsLong)
                                                                                            .flatMap(crsNum -> rTermName
                                                                                                    .flatMap(term -> rGroupId.flatMap(StrUtils::parseAsLong)
                                                                                                            .flatMap(grpId -> rGroupIndex.flatMap(StrUtils::parseAsInteger)
                                                                                                                    .map(grpIdx -> new BookingImpl(bId, timeS, timeE, resId, resName, roomName, crsId, crsName, crsNum, term, grpId, grpIdx)))))))))))));


            booking.forEachOrFail(bo -> LOGGER.info(String.format("Converted booking: %s", bo))).forEach(msg -> LOGGER.error("Could not convert TvrBooking into Booking. " + msg));
            booking.forEach(x -> bookings.add(x));
        }
        return Collections.unmodifiableSet(bookings);
    }

    public Result<Booking> getBookingById(User user, String id) {
        Optional<Booking> oBooking = getBookings(user).stream().filter(u -> u.getBookingId() == Long.valueOf(id)).findFirst();
        return oBooking.isPresent()
                ? Result.success(oBooking.get())
                : Result.failure(String.format("Could not find booking with id \"%s\" for user \"%s\"", id, user));
    }

}
