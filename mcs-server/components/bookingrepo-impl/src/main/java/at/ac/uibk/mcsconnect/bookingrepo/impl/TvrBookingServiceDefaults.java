package at.ac.uibk.mcsconnect.bookingrepo.impl;

import java.time.temporal.ChronoUnit;

public abstract class TvrBookingServiceDefaults {

    public static final Long DEFAULT_BOOKING_TIME_THRESHOLD = 30L;
    public static final ChronoUnit DEFAULT_BOOKING_TIME_THRESHOLD_UNIT = ChronoUnit.MINUTES;

}
