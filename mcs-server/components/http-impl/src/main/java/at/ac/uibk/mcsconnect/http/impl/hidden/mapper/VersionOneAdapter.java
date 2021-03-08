package at.ac.uibk.mcsconnect.http.impl.hidden.mapper;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstance;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;

import java.util.Optional;
import java.util.Set;

public class VersionOneAdapter {

    public static class OldUser {
        private String userId;
        private String email;
        private String displayName;
        private Set<Booking> bookings; // List<Booking>, depending on how deserialized
        public OldUser(User user, Set<Booking> bookings) {
            this.userId = user.getUserId();
            this.email = user.getEmail();
            this.displayName = user.getDisplayName();
            this.bookings = bookings;
        };

        public String getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Set<Booking> getBookings() {
            return bookings;
        }
    }
    public static class OldRoom {
        private String id;
        private String name;
        private OldUser user;
        private RecordingInstance recordingInstance;
        public OldRoom(Room room, Set<Booking> bookings, User user, Optional<RecordingInstance> recordingInstance) {
            this.id = room.getId();
            this.name = room.getName();
            this.user = new OldUser(user, bookings); // recordingInstance and user can be null
            this.recordingInstance = recordingInstance.isPresent() ? recordingInstance.get() : null;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public OldUser getUser() {
            return user;
        }

        public RecordingInstance getRecordingInstance() {
            return recordingInstance;
        }
    }
    /** Probably my biggest mistake was letting this domain object out of the room aggregate in the first design */
    public static class OldTerminal {
        private String terminalId;
        private NetworkTarget networkTarget;
        private OldRoom room;

        public OldTerminal(Terminal terminal, User user, Room room, Set<Booking> bookings, Optional<RecordingInstance> recordingInstance) {
            this.terminalId = terminal.getId();
            this.networkTarget = terminal.getNetworkTarget();
            this.room = new OldRoom(room, bookings, user, recordingInstance);
        }

        public String getTerminalId() {
            return terminalId;
        }

        public NetworkTarget getNetworkTarget() {
            return networkTarget;
        }

        public OldRoom getRoom() {
            return room;
        }

        public String toString() {
            return String.format("%s(%s, %s)", this.getClass().getSimpleName(), this.terminalId, this.networkTarget);
        }
    }

}
