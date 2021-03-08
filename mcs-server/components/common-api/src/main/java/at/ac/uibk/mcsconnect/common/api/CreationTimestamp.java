package at.ac.uibk.mcsconnect.common.api;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Easily add creation timestamp to any class.
 */
public abstract class CreationTimestamp {

    final LocalDateTime creationTimestamp = LocalDateTime.now();

    public CreationTimestamp() { };

    public LocalDateTime getCreationTimestamp() {
        return LocalDateTime.from(creationTimestamp);
    }

    public boolean isNotOlderThan(long duration, ChronoUnit unit) {
        LocalDateTime endTimeStamp = creationTimestamp.plus(duration, unit);
        return creationTimestamp.isBefore(endTimeStamp);
    }
}
