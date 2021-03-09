package at.ac.uibk.mcsconnect.publichttp.impl.unit;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.bookingrepo.api.BookingRepo;
import at.ac.uibk.mcsconnect.bookingrepo.impl.integration.FakeBooking;
import at.ac.uibk.mcsconnect.bookingrepo.impl.integration.FakeBookingRepo;
import at.ac.uibk.mcsconnect.common.impl.integration.FakeNetworkTarget;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.http.api.RecordingInstanceIntermediatePost;
import at.ac.uibk.mcsconnect.http.api.hidden.datatransferobjects.RecordingInstanceIntermediateDTO;
import at.ac.uibk.mcsconnect.http.impl.hidden.mapper.RequestUtilities;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.person.api.UserFactory;
import at.ac.uibk.mcsconnect.person.impl.integration.FakeUserFactory;
import at.ac.uibk.mcsconnect.recorderservice.api.DublinCore;
import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstance;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstanceConfiguration;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstanceFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomRepo;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeRecorder;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeRecordingInstanceConfiguration;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeRecordingInstanceFactory;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeRoom;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeRoomFactory;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeRoomRepo;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeTerminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Quick and dirty tests using the new modular design of MCSv2.
 *
 * The most important rules are contained within
 * {@link at.ac.uibk.mcsconnect.http.impl.hidden.assembler.RecordingInstanceIntermediate#apply(Room, User)}.
 * These tests test this algorithm with a level of indirection.
 *
 * Hybrid between unit and integration tests: integration because of the other components involved
 */
@DisplayName("Contains mostly integration tests")
public class RecordingInstanceIntermediateTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordingInstanceIntermediateTests.class);

    RoomFactory roomFactory;
    RoomRepo roomRepo;
    Room roomAWithoutRecordingInstance;
    Room roomBWithStoppedRecordingInstanceForBooking12345;
    Room roomCWithRecordingRecordingInstanceForBooking12345;
    User finiganSmithers;
    User joseppiHanighan;
    UserFactory userFactory;
    Set<Recorder> recorders;
    Set<Terminal> terminals;
    BookingRepo bookingRepo;
    RecordingInstanceFactory recordingInstanceFactory;
    RecordingInstanceConfiguration recordingInstanceConfiguration;

    private static Set<Recorder> recordersHelper() {
        Set<Recorder> recorders = new HashSet<>();
        recorders.add(FakeRecorder.create("r", "main", FakeNetworkTarget.create("192.168.0.1", 22023, "test", "test"), RecorderRunningStatesEnum.STOPPED));
        return recorders;
    }

    private static Set<Terminal> terminalsHelper() {
        Terminal terminal = FakeTerminal.create("test01", "Test Machine", "192.168.0.2");
        Set<Terminal> terminals = new HashSet<>();
        terminals.add(terminal);
        return terminals;
    }


    @BeforeEach
    void setupRoomService() {

        this.recordingInstanceConfiguration = new FakeRecordingInstanceConfiguration(30, TimeUnit.MINUTES);

        this.terminals = terminalsHelper();

        this.recorders = recordersHelper();

        this.roomFactory = new FakeRoomFactory();

        this.roomAWithoutRecordingInstance = FakeRoom.create("room_a", "Room A", recorders, terminals, Optional.empty());
        // TODO A circular dependency between the Room and RecordingInstance prevents simply setting the RecordingInstance early. Replace Room ref with roomId in RecordingInstance
        this.roomBWithStoppedRecordingInstanceForBooking12345 = FakeRoom.create("room_b", "Room B", recorders, terminals, Optional.empty());
        this.roomCWithRecordingRecordingInstanceForBooking12345 = FakeRoom.create("room_c", "Room C", recorders, terminals, Optional.empty());

        this.roomRepo = new FakeRoomRepo(roomAWithoutRecordingInstance);

        UserFactory userFactory = new FakeUserFactory();
        this.userFactory = userFactory;
        this.finiganSmithers = userFactory.create("c000000", "Finigan Smithers", "smithers@uibk.ac.at");
        this.joseppiHanighan = userFactory.create("c000001", "Joseppi Hanighan", "hanighan@uibk.ac.at");


        Booking booking12345 = new FakeBooking(
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
                1
        );
        Booking booking6789 = new FakeBooking(
                6789,
                LocalDateTime.parse("1986-04-26T12:10:00"),
                LocalDateTime.parse("1986-04-26T13:00:00"),
                1234,
                "innsbruck",
                "HSA",
                198604,
                "A Different Booking",
                22,
                "SS1986",
                18273,
                2
        );
        Booking booking5555 = new FakeBooking(
                5555,
                LocalDateTime.parse("1592-06-14T16:00:00"),
                LocalDateTime.parse("1592-06-14T17:00:00"),
                999999999,
                "innsbruck",
                "Non-existent Room",
                998274,
                "A Different Booking",
                195,
                "SS1986",
                93958,
                1
        );

        Set<Booking> fakeBookingSet = new HashSet<>();
        fakeBookingSet.add(booking12345);
        fakeBookingSet.add(booking6789);
        fakeBookingSet.add(booking5555);
        Map<User, Set<Booking>> fakeBookingRepo = new HashMap<>();
        fakeBookingRepo.put(finiganSmithers, fakeBookingSet);
        fakeBookingRepo.put(joseppiHanighan, fakeBookingSet);
        BookingRepo bookingRepo = new FakeBookingRepo(fakeBookingRepo);
        this.bookingRepo = bookingRepo;

        this.recordingInstanceFactory = new FakeRecordingInstanceFactory();

        RecordingInstance fakeStoppedRecordingInstance = this.recordingInstanceFactory.create(
                this.roomBWithStoppedRecordingInstanceForBooking12345,
                this.finiganSmithers,
                booking12345,
                LocalDateTime.parse("1900-10-20T10:00:00"),
                "A Fake Recording has not started yet",
                RecorderRunningStatesEnum.STOPPED
        );

        RecordingInstance fakeRecordingRecordingInstance = this.recordingInstanceFactory.create(
                this.roomBWithStoppedRecordingInstanceForBooking12345,
                this.finiganSmithers,
                booking12345,
                LocalDateTime.parse("1900-10-20T10:00:00"),
                "A Fake Recording in progress",
                RecorderRunningStatesEnum.RECORDING
        );


        roomBWithStoppedRecordingInstanceForBooking12345.setRecordingInstance(Optional.of(fakeStoppedRecordingInstance));
        roomCWithRecordingRecordingInstanceForBooking12345.setRecordingInstance(Optional.of(fakeRecordingRecordingInstance));

    }

    @Test
    @DisplayName("Set recording instance in a room that has no recording instance.")
    void setRecordingInstance_in_empty_room() {
        Result<RecordingInstanceIntermediatePost> rPost = Result.of(new RecordingInstanceIntermediateDTO(
                "12345", "", "", ""));

        Result<Room> rRoom = Result.of(this.roomAWithoutRecordingInstance, "Room may not be null");
        Result<User> rUser = Result.of(this.finiganSmithers, "User may not be null");
        Result<BookingRepo> rBookingRepo = Result.of(this.bookingRepo, "Booking repo may not be null");
        Result<RecordingInstanceFactory> rRecordingFactory = Result.of(this.recordingInstanceFactory, "Recording Factory may not be null");
        Result<RecordingInstanceConfiguration> rRecordingInstanceConfiguration = Result.of(this.recordingInstanceConfiguration, "Recording Instance Configuration may not be null");

        Result<RecordingInstance> sut =
                rPost
                        .flatMap(post -> rRoom
                                .flatMap(room -> rUser
                                        .flatMap(user -> rBookingRepo
                                                .flatMap(bookRepo -> rRecordingFactory
                                                        .flatMap(recFactory -> rRecordingInstanceConfiguration
                                                            .flatMap(recInsConfig ->
                                                                    /** input complex due to asserts in RecordingInstanceIntermediate */
                                                                    RequestUtilities
                                                                            .recordingInstanceDTOToRecordingInstance(
                                                                                    post,
                                                                                    room,
                                                                                    user,
                                                                                    bookRepo,
                                                                                    recFactory,
                                                                                    recInsConfig)
                                                            ))))));

        sut.forEachOrFail(s -> logInfo(s.toString())).forEach(RecordingInstanceIntermediateTests::logError);

        sut.map(s -> assertThat(s.getBooking().getBookingId()).isEqualTo(Long.valueOf("12345")));
    }

    @Test
    @DisplayName("Set recording instance in a room that has a recording instance.")
    void setRecordingInstance_in_room_with_existing() {
        RecordingInstanceIntermediatePost post = new RecordingInstanceIntermediateDTO(
                "12345", "", "", "");

        /** input complex due to asserts in RecordingInstanceIntermediate */
        Result<RecordingInstance> sut = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        post,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);

        sut.forEachOrFail(s -> logInfo(s.toString())).forEach(RecordingInstanceIntermediateTests::logError);
        sut.map(s -> assertThat(s.getBooking().getBookingId()).isEqualTo(Long.valueOf("12345")));
    }

    @Test
    @DisplayName("Change stop time on recording instance that does not exist yet (room empty).")
    void change_stop_time_on_new_recording_instance() {
        // NOTE THAT I USE GLOBAL BOOK WITH endTime of 1900-10-20T10:00:00
        Result<RecordingInstanceIntermediatePost> rPost = Result.of(new RecordingInstanceIntermediateDTO(
                "12345", "1900-10-20T10:10:00", "", ""), "Post may not b e null.");

        /** For fun I went all-out functional on this test */
        Result<Room> rRoom = Result.of(this.roomAWithoutRecordingInstance, "Room may not be null");
        Result<User> rUser = Result.of(this.finiganSmithers, "User may not be null");
        Result<BookingRepo> rBookingRepo = Result.of(this.bookingRepo, "Booking repo may not be null");
        Result<RecordingInstanceFactory> rRecordingFactory = Result.of(this.recordingInstanceFactory, "Recording Factory may not be null");
        Result<RecordingInstanceConfiguration> rRecordingInstanceConfiguration = Result.of(this.recordingInstanceConfiguration, "Recording Instance Configuration may not be null");

        Result<RecordingInstance> sut =
                rPost
                        .flatMap(post -> rRoom
                                .flatMap(room -> rUser
                                        .flatMap(user -> rBookingRepo
                                                .flatMap(bookRepo -> rRecordingFactory
                                                        .flatMap(recFactory -> rRecordingInstanceConfiguration
                                                            .flatMap(recInsConfig ->
                                                                    /** input complex due to asserts in RecordingInstanceIntermediate */
                                                                    RequestUtilities
                                                                            .recordingInstanceDTOToRecordingInstance(
                                                                                    post,
                                                                                    room,
                                                                                    user,
                                                                                    bookRepo,
                                                                                    recFactory,
                                                                                    recInsConfig)
                                                            ))))));

        sut.forEachOrFail(s -> logInfo(s.toString())).forEach(err -> RecordingInstanceIntermediateTests.logError(String.format("%s", err)));
        sut.map(s -> assertThat(s.getStopTime()).isEqualTo(LocalDateTime.parse("1900-10-20T10:10:00")));
    }

    @Test
    @DisplayName("Add 10 minutes to stop time on recording instance when room has recording instance and stop time within threshold.")
    void change_stop_time_on_existing_recording_instance() {
        RecordingInstanceIntermediatePost post = new RecordingInstanceIntermediateDTO(
                "12345", "1900-10-20T10:10:00", "", "");

        /** input complex due to asserts in RecordingInstanceIntermediate */
        Result<RecordingInstance> sut = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        post,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);

        sut.forEachOrFail(s -> logInfo(s.toString())).forEach(RecordingInstanceIntermediateTests::logError);
        sut.map(s -> assertThat(s.getStopTime()).isEqualTo(LocalDateTime.parse("1900-10-20T10:10:00")));
    }

    @Test
    @DisplayName("Add 10 minutes to stop time on recording instance when room is empty and stop time is within threshold")
    void change_stop_time_on_empty_room() {
        RecordingInstanceIntermediatePost post = new RecordingInstanceIntermediateDTO(
                "12345", "1900-10-20T10:10:00", "", "");

        /** input complex due to asserts in RecordingInstanceIntermediate */
        Result<RecordingInstance> sut = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        post,
                        this.roomAWithoutRecordingInstance,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);

        sut.forEachOrFail(s -> logInfo(s.toString())).forEach(RecordingInstanceIntermediateTests::logError);
        sut.map(s -> assertThat(s.getStopTime()).isEqualTo(LocalDateTime.parse("1900-10-20T10:10:00")));
    }

    @Test
    @DisplayName("Set stop time to new time does not affect current stop time when new stop time exceeds threshold.")
    void change_stop_time_fails_on_existing_recording_instance_when_exceeds_max() {
        RecordingInstanceIntermediatePost extendStopTimeBy10 = new RecordingInstanceIntermediateDTO(
                "12345", "1900-10-20T10:10:00", "", "");

        RecordingInstanceIntermediatePost extendStopTimeByTooMuch = new RecordingInstanceIntermediateDTO(
                "12345", "1900-10-20T10:35:00", "", "");

        Result<RecordingInstance> firstPost = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        extendStopTimeBy10,
                        this.roomAWithoutRecordingInstance,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);

        firstPost.forEachOrFail(ri -> this.roomAWithoutRecordingInstance.setRecordingInstance(Optional.of(ri))).forEach(RecordingInstanceIntermediateTests::logError);
        firstPost.map(s -> assertThat(s.getStopTime()).isEqualTo(LocalDateTime.parse("1900-10-20T10:10:00")));

        Result<RecordingInstance> sut = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        extendStopTimeByTooMuch,
                        this.roomAWithoutRecordingInstance,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);

        sut.forEachOrFail(s -> logInfo(s.toString())).forEach(RecordingInstanceIntermediateTests::logError);
        sut.map(s -> assertThat(s.getStopTime()).isEqualTo(LocalDateTime.parse("1900-10-20T10:10:00")));
    }

    @Test
    @DisplayName("Can switch booking when recording instance exists for a different booking.")
    void change_booking_when_recording_instance_exists() {
        RecordingInstanceIntermediatePost changeBookingPost = new RecordingInstanceIntermediateDTO(
                "6789", "", "", "");

        Result<RecordingInstance> sut = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        changeBookingPost,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);

        sut.forEachOrFail(s -> logInfo(s.toString())).forEach(RecordingInstanceIntermediateTests::logError);
        sut.map(s -> assertThat(s.getBooking().getBookingId()).isEqualTo(6789L));
        sut.map(s -> assertThat(s.getRecordingRunningState()).isEqualTo(RecorderRunningStatesEnum.STOPPED));
    }

    @Test
    @DisplayName("Stop time is correct when changing booking of an existing recording instance (the new stop time should overwrite the existing stop time).")
    void ensure_stop_time_correct_when_switching_bookings_when_multiple_bookings_exist_for_user() {
        RecordingInstanceIntermediatePost changeBookingPost = new RecordingInstanceIntermediateDTO(
                "6789", "", "", "");

        Result<RecordingInstance> sut = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        changeBookingPost,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);

        sut.forEachOrFail(s -> logInfo(s.toString())).forEach(RecordingInstanceIntermediateTests::logError);
        sut.map(s -> assertThat(s.getStopTime()).isEqualTo(LocalDateTime.parse("1986-04-26T13:00:00")));
    }

    @Test
    @DisplayName("Create recording instance in a room different from that of the booking")
    void can_record_in_a_different_room_than_in_booking() {
        RecordingInstanceIntermediatePost post = new RecordingInstanceIntermediateDTO(
                "5555", "", "", "");

        Result<RecordingInstance> sut = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        post,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);

        sut.map(s -> assertThat(s.getBooking().getBookingId()).isEqualTo(Long.valueOf("5555")));
    }

    @Test
    @DisplayName("Start and stop recording instance. Start again with the same booking.")
    void can_start_stop_start_stop_on_same_booking() {
        // START WITH A ROOM IN STOPPED STATE WITH RECORDINGINSTANCE for booking 12345

        RecordingInstanceIntermediatePost recordPost1 = new RecordingInstanceIntermediateDTO(
                "12345", "", "", "RECORDING");

        Result<RecordingInstance> sut1 = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        recordPost1,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);

        sut1.map(s -> assertThat(s.getRecordingRunningState().toString()).isEqualTo("RECORDING"));

        RecordingInstanceIntermediatePost stopPost1 = new RecordingInstanceIntermediateDTO(
                "12345", "", "", "STOPPED");

        Result<RecordingInstance> sut2 = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        stopPost1,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);

        sut2.map(s -> assertThat(s.getRecordingRunningState().toString()).isEqualTo("STOPPED"));

        RecordingInstanceIntermediatePost recordPost2 = new RecordingInstanceIntermediateDTO(
                "12345", "", "", "RECORDING");

        Result<RecordingInstance> sut3 = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        recordPost2,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);

        sut3.map(s -> assertThat(s.getRecordingRunningState().toString()).isEqualTo("RECORDING"));
    }

    @Test
    @DisplayName("Start and stop recording instance and start again with a different booking.")
    void can_start_stop_start_stop_on_different_booking() {
        // START WITH A ROOM IN STOPPED STATE WITH RECORDINGINSTANCE for booking 12345

        RecordingInstanceIntermediatePost startRecording12345Post = new RecordingInstanceIntermediateDTO(
                "12345", "", "", "RECORDING");

        Result<RecordingInstance> startRecordingRequest = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        startRecording12345Post,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);

        startRecordingRequest.map(s -> assertThat(s.getRecordingRunningState().toString()).isEqualTo("RECORDING"));

        RecordingInstanceIntermediatePost stopRecording12345Post = new RecordingInstanceIntermediateDTO(
                "12345", "", "", "STOPPED");

        Result<RecordingInstance> stopRecordingRequest = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        stopRecording12345Post,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);

        stopRecordingRequest.map(s -> assertThat(s.getRecordingRunningState().toString()).isEqualTo("STOPPED"));

        // Different booking here:
        RecordingInstanceIntermediatePost newBooking6789Post = new RecordingInstanceIntermediateDTO(
                "6789", "", "", "");

        Result<RecordingInstance> sut = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        newBooking6789Post,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);

        sut.map(s -> assertThat(s.getBooking().getBookingId()).isEqualTo(Long.valueOf(6789)));
        sut.map(s -> assertThat(s.getRecordingRunningState().toString()).isEqualTo("STOPPED"));

    }

    @Test
    @Tag("disabled")
    @DisplayName("Can start recording 15 minutes prior to booking begin time.")
    /**
     * The problem with time tests are that MCS does does not implement checking of begin time.
     * If a booking exists, it may be started and stopped. The logic to enforce times is in the ESB query (@link BookingRepo}
     */
    void can_start_recording_early() {
        logError("test not implemented");
    }

    @Test
    @Tag("disabled")
    @DisplayName("Cannot start recording more than 15 minutes prior to booking begin time.")
    void cannot_start_recording_too_early() {
        logError("test not implemented");
    }

    @Test
    @DisplayName("When a new booking id is posted, the any old recording instance data is disregarded")
    void new_booking_invalidates_existing_recording_instance() {
        RecordingInstanceIntermediatePost startRecording12345Post = new RecordingInstanceIntermediateDTO(
                "6789", "", "", "RECORDING");

        Result<RecordingInstance> newRecordingInstance = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        startRecording12345Post,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);

        newRecordingInstance.map(s -> assertThat(s.getBooking().getBookingId()).isEqualTo(Long.valueOf(6789L)));
        newRecordingInstance.map(Optional::of).forEachOrFail(r -> this.roomBWithStoppedRecordingInstanceForBooking12345.setRecordingInstance(r));

        // TODO This test's SUT is not the RecordingInstanceIntermediate
        RecordingInstance sut = this.roomBWithStoppedRecordingInstanceForBooking12345.getRecordingInstance().get();

        assertThat(sut.getBooking().getBookingId()).isEqualTo(Long.valueOf("6789"));
        assertThat(sut.getStopTime()).isEqualTo(LocalDateTime.parse("1986-04-26T13:00:00"));
    }

    @Test
    @Tag("disabled")
    @DisplayName("Starting a recording creates an autostop thread")
    void starting_a_recording_creates_autostop_thread() {
        logError("test not implemented because it can't be tested yet");
    }

    @Test
    @DisplayName("Can start and stop with one user (lecturer), and start and stop with a different user (lecturer) in the same booking.")
    void can_switch_users_within_one_booking() {
        RecordingInstanceIntermediatePost startRecording12345Post = new RecordingInstanceIntermediateDTO(
                "6789", "", "", "RECORDING");

        Result<RecordingInstance> startRecordingRequest = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        startRecording12345Post,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);

        startRecordingRequest.map(s -> assertThat(s.getOwner().toString()).isEqualTo("FakeUser(c000000, Finigan Smithers)"));

        RecordingInstanceIntermediatePost stopRecording12345Post = new RecordingInstanceIntermediateDTO(
                "6789", "", "", "RECORDING");
        startRecordingRequest.map(Optional::of).forEach(r -> this.roomBWithStoppedRecordingInstanceForBooking12345.setRecordingInstance(r));


        RecordingInstanceIntermediatePost startRecording12345AgainPost = new RecordingInstanceIntermediateDTO(
                "6789", "", "", "RECORDING");

        Result<RecordingInstance> sut = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        startRecording12345AgainPost,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.joseppiHanighan,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);
    }

    @Test
    @DisplayName("Change name of recording instance once recording starts has no effect")
    void cannot_change_name_of_recording_instance_when_running_state_is_already_recording() {
        RecordingInstanceIntermediatePost startRecordingWithName = new RecordingInstanceIntermediateDTO(
                "12345", "", "A", "RECORDING");

        Result<RecordingInstance> startRecordingRequest = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        startRecordingWithName,
                        this.roomAWithoutRecordingInstance,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);
        startRecordingRequest.forEachOrFail(ri -> assertThat(ri.getRecordingName()).isEqualTo("A"));
        startRecordingRequest.forEachOrFail(ri -> roomAWithoutRecordingInstance.setRecordingInstance(Optional.of(ri)));



        RecordingInstanceIntermediatePost changeRecordingName = new RecordingInstanceIntermediateDTO(
                "12345", "", "B", "RECORDING");
        Result<RecordingInstance> changeNameRequest = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        changeRecordingName,
                        this.roomAWithoutRecordingInstance,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);

        changeNameRequest.forEachOrFail(ri -> assertThat(ri.getRecordingName()).isEqualTo("A"));


    }

    @Test
    @DisplayName("Change name of recording instance after start and stop and start and stop on the same booking.")
    void can_change_recording_instance_name_after_start_stop() {
        RecordingInstanceIntermediatePost post1 = new RecordingInstanceIntermediateDTO(
                "12345", "", "A", "RECORDING");

        Result<RecordingInstance> startRecordingRequest1 = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        post1,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);
        startRecordingRequest1.forEachOrFail(ri -> this.roomBWithStoppedRecordingInstanceForBooking12345.setRecordingInstance(Optional.of(ri))).forEach(RecordingInstanceIntermediateTests::logError);

        RecordingInstanceIntermediatePost post2 = new RecordingInstanceIntermediateDTO(
                "12345", "", "", "STOPPED");
        Result<RecordingInstance> stopRecordingRequest1 = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        post2,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);
        stopRecordingRequest1.forEachOrFail(ri -> this.roomBWithStoppedRecordingInstanceForBooking12345.setRecordingInstance(Optional.of(ri))).forEach(RecordingInstanceIntermediateTests::logError);
        String sut1 = this.roomBWithStoppedRecordingInstanceForBooking12345.getRecordingInstance().get().getRecordingName();
        assertThat(sut1).isEqualTo("A");

        RecordingInstanceIntermediatePost post3 = new RecordingInstanceIntermediateDTO(
                "12345", "", "B", "RECORDING");
        Result<RecordingInstance> startRecordingRequest2 = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        post3,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);
        startRecordingRequest2.forEachOrFail(ri -> this.roomBWithStoppedRecordingInstanceForBooking12345.setRecordingInstance(Optional.of(ri))).forEach(RecordingInstanceIntermediateTests::logError);

        RecordingInstanceIntermediatePost post4 = new RecordingInstanceIntermediateDTO(
                "12345", "", "", "STOPPED");
        Result<RecordingInstance> stopRecordingRequest2 = RequestUtilities
                .recordingInstanceDTOToRecordingInstance(
                        post4,
                        this.roomBWithStoppedRecordingInstanceForBooking12345,
                        this.finiganSmithers,
                        this.bookingRepo,
                        this.recordingInstanceFactory,
                        this.recordingInstanceConfiguration);
        stopRecordingRequest2.forEachOrFail(ri -> this.roomBWithStoppedRecordingInstanceForBooking12345.setRecordingInstance(Optional.of(ri))).forEach(RecordingInstanceIntermediateTests::logError);

        String sut2 = this.roomBWithStoppedRecordingInstanceForBooking12345.getRecordingInstance().get().getRecordingName();
        assertThat(sut2).isEqualTo("B");
    }



    private static void logInfo(String msg) {
        LOGGER.info(msg);
    }
    private static void logWarn(String msg) {
        LOGGER.warn(msg);
    }
    private static void logError(String msg) {
        fail(msg);
        LOGGER.error(msg);
    }

}
