package at.ac.uibk.mcsconnect.publichttp.impl.unit;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.bookingrepo.api.BookingRepo;
import at.ac.uibk.mcsconnect.bookingrepo.impl.integration.FakeBooking;
import at.ac.uibk.mcsconnect.bookingrepo.impl.integration.FakeBookingRepo;
import at.ac.uibk.mcsconnect.common.impl.integration.FakeNetworkTarget;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.http.impl.hidden.filter.Utility;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.person.api.UserFactory;
import at.ac.uibk.mcsconnect.person.impl.integration.FakeUserFactory;
import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstance;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstanceFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomRepo;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeRecorder;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeRecordingInstanceFactory;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeRoom;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeRoomFactory;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeRoomRepo;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeTerminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * On the road to a message-based application layer, this is the only way
 * to test the critical functionality of setting recording instances.
 */
public class RequestUtilitiesTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestUtilitiesTest.class);

    RoomFactory roomFactory;
    RoomRepo roomRepo;
    Room roomWithoutRecordingInstance;
    Room roomWithRecordingInstance;
    User finiganSmithers;
    UserFactory userFactory;
    Set<Recorder> recorders;
    Set<Terminal> terminals;
    BookingRepo bookingRepo;
    RecordingInstanceFactory recordingInstanceFactory;


    @BeforeEach
    void setupRoomService() {
        Terminal terminal = FakeTerminal.create("test01", "Test Machine", "192.168.0.2");
        Set<Terminal> terminals = new HashSet<>();
        terminals.add(terminal);
        this.terminals = terminals;

        Set<Recorder> recorders = new HashSet<>();
        recorders.add(FakeRecorder.create("r", "main", FakeNetworkTarget.create("192.168.0.1", 22023, "test", "test"), RecorderRunningStatesEnum.STOPPED));
        this.recorders = recorders;

        this.roomFactory = new FakeRoomFactory();

        this.roomWithoutRecordingInstance = FakeRoom.create("a_id", "a_name", recorders, terminals, Optional.empty());
        this.roomWithRecordingInstance = FakeRoom.create("a_id", "a_name", recorders, terminals, Optional.empty());
        this.roomRepo = new FakeRoomRepo(roomWithoutRecordingInstance);

        UserFactory userFactory = new FakeUserFactory();
        this.userFactory = userFactory;
        this.finiganSmithers = userFactory.create("c000000", "Finigan Smithers", "smithers@uibk.ac.at");

        Booking booking = new FakeBooking(
                12345,
                LocalDateTime.parse("1900-10-20T09:00:00"),
                LocalDateTime.parse("1900-10-20T10:00:00"),
                1234,
                "innsbruck",
                "HSA",
                789123,
                "Introduction to Testing",
                456,
                "WS1900",
                93748,
                0
        );

        Set<Booking> fakeBookingSet = new HashSet<>();
        fakeBookingSet.add(booking);
        Map<User, Set<Booking>> fakeBookingRepo = new HashMap<>();
        fakeBookingRepo.put(finiganSmithers, fakeBookingSet);
        BookingRepo bookingRepo = new FakeBookingRepo(fakeBookingRepo);
        this.bookingRepo = bookingRepo;

        this.recordingInstanceFactory = new FakeRecordingInstanceFactory();

        RecordingInstance fakeRecordingInstance = this.recordingInstanceFactory.create(
                this.roomWithRecordingInstance,
                this.finiganSmithers,
                booking,
                LocalDateTime.parse("1900-10-20T10:00:00"),
                "A Fake Recording",
                RecorderRunningStatesEnum.STOPPED
                );
        roomWithRecordingInstance.setRecordingInstance(Optional.of(fakeRecordingInstance));
    }



    @Test
    @DisplayName("Get room for registered host succeeds.")
    void getRoomForRegisteredHost() {
        Result<Room> rRoom = roomRepo.getRoomForHost("192.168.0.2");
        rRoom.forEachOrFail(r -> assertThat(r.getId()).isEqualTo("a_id"));
    }

    @Test
    @DisplayName("Get room for non-registered host fails.")
    void getRoomForNonregisteredHost() {
        Result<Room> rRoomFailure = roomRepo.getRoomForHost("10.0.0.58");
        assertThat(rRoomFailure.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("Test getHeaderValue")
    void testGetHeaderValue() {
        MultivaluedMap<String, String> simulatedHeaders = new MultivaluedHashMap<>();
        simulatedHeaders.add("x-forwarded-for", "127.0.0.1");
        simulatedHeaders.add("host", "127.0.0.2");

        Result<String> rString01 = Utility.getHeaderValue("x-forwarded-for", simulatedHeaders);
        Result<String> rString02 = Utility.getHeaderValue("host", simulatedHeaders);

        assertThat(rString01.isSuccess()).isTrue();
        assertThat(rString01.successValue()).isEqualTo("127.0.0.1");

        assertThat(rString02.isSuccess()).isTrue();
        assertThat(rString02.successValue()).isEqualTo("127.0.0.2");
    }

    @Test
    @DisplayName("Almost considered testing something I do not own.")
    void testExtractHostFromCxfMessageContext() {
        //MessageContext messageContext =
        //RequestUtilities.extractHostFromContext(messageContext);
    }

    private static void logInfo(String msg) {
        LOGGER.info(msg);
    }
    private static void logError(String msg) {
        LOGGER.error(msg);
    }
}
