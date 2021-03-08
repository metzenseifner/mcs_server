package at.ac.uibk.mcsconnect.common.api;

import java.time.LocalDateTime;
import java.util.Date;

public final class DateTime {

    public static final Date convertLocalDateTimeToDate(final LocalDateTime timestamp) {
        return java.sql.Timestamp.valueOf(timestamp);
    }

}
