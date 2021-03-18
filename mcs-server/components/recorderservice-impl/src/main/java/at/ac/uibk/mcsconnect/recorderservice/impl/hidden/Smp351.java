package at.ac.uibk.mcsconnect.recorderservice.impl.hidden;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.common.api.Preparable;
import at.ac.uibk.mcsconnect.common.api.StrUtils;
import at.ac.uibk.mcsconnect.executorservice.api.McsScheduledExecutorService;
import at.ac.uibk.mcsconnect.executorservice.api.McsSingletonExecutorService;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.functional.common.Tuple;
import at.ac.uibk.mcsconnect.recorderservice.api.DublinCore;
import at.ac.uibk.mcsconnect.recorderservice.api.Metadata;
import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import at.ac.uibk.mcsconnect.recorderservice.api.SisProtocol;
import at.ac.uibk.mcsconnect.recorderservice.api.SmpFetchable;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstance;
import at.ac.uibk.mcsconnect.recorderservice.api.RecordingInstanceObserver;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.sshsessionmanager.api.SshSessionManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;


/**
 * SMP 351 object model representation.
 *
 * This model is currently responsible for managing its own communication (hence the
 * reliance on an executor service and scheduled executor service. It uses the {@link ScheduledExecutorService}
 * to schedule checks with itself by submitting jobs with the {@link ExecutorService}.
 *
 * This
 * keeps the complicated code isolated here, but also complicates this class.
 *
 * <p>
 * The {@link this#enabledSettableFields} is merely an abstraction of {@link SisProtocol.Settable} to allow
 * separation between what is "possible" and what is currently "enabled". This separation could be easily
 * removed later to reduce the maintenance costs of adding new {@link SisProtocol.Settable}.
 */
public class Smp351 implements Recorder, SmpFetchable, RecordingInstanceObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Smp351.class);

    private final NetworkTarget networkTargetUserPass;
    private final String id;
    private final String name;
    private volatile RecorderRunningStatesEnum recorderRunningState = RecorderRunningStatesEnum.UNKNOWN;
    private final SshSessionManagerService sshSessionManagerService;
    private String firmwareVersion = "?";
    private String unitName = "?";
    private String telnetPort = "?";
    private String sshPort = "?";
    private String httpPort = "?";
    private String snmpPort = "?";
    private String httpsPort = "?";
    private String snmpUnitLocation = "?";
    private String snmpUnitContact = "?";
    private String snmpPrivateCommunityString = "?";
    private String snmpPublicCommunityString = "?";
    private String snmpState = "?";
    private String timezone = "?";
    private String dhcpMode = "?";
    private String macAddress = "?";
    private String portTimeout = "?";
    private String globalPortTimeout = "?";
    private String modelName = "?";
    private String modelDescription = "?";
    private String activeAlarms = "?";
    private String partNumber = "?";
    private String registerCoverage = "?";
    private String registerPresenter = "?";
    private String registerRelation = "?";
    private String registerSource = "?";
    private String registerSubject = "?";
    private String registerTitle = "?";

    private List<SisProtocol.Settable> enabledSettableFields = new ArrayList<>(Arrays.asList(
            SisProtocol.Settable.PRESENTER,
            SisProtocol.Settable.RELATION,
            SisProtocol.Settable.SOURCE,
            SisProtocol.Settable.SUBJECT,
            SisProtocol.Settable.TITLE
    ));
    private final ScheduledExecutorService scheduledExecutorService;
    private final ExecutorService executorService;
    private List<ScheduledFuture<?>> threadHandles;


    /**
     * Factory that initializes SMP with the minimum requirements.
     *
     * @return
     */
    public static Smp351 create(String id,
                                String name,
                                NetworkTarget networkTargetUserPass,
                                SshSessionManagerService sshSessionManager,
                                McsScheduledExecutorService scheduledExecutorService,
                                McsSingletonExecutorService singletonExecutorService) {
        return new Smp351(id, name, networkTargetUserPass, sshSessionManager, scheduledExecutorService, singletonExecutorService);
    }

    /**
     * Returns a minimal SMP recorder object.
     */
    private Smp351(String id,
                   String name,
                   NetworkTarget target,
                   SshSessionManagerService sshSessionManagerService,
                   McsScheduledExecutorService scheduledExecutiveService,
                   McsSingletonExecutorService singletonExecutorService) {
        this.networkTargetUserPass = target;
        this.id = id;
        this.name = name;
        this.sshSessionManagerService = sshSessionManagerService;
        this.scheduledExecutorService = scheduledExecutiveService.getScheduledExecutorService();
        this.executorService = singletonExecutorService.getExecutorService();
        this.init(); // THIS METHOD STARTS UP THE SYNC THREADS (SMP OBJS SHOULD MANAGE THEIR OWN COMPLICATED CONNECTIVITY)
    }

    public void init() {
        LOGGER.info(String.format("%s.init() called.", this.getClass().getSimpleName()));
        // The SmpFetchable.enabledMethods are fetched to sync with the in-mem repr.
        // TODO should be pushed into a priority queue with lower priority than starting/stopping.
        for (Consumer<SmpFetchable> c : SmpFetchable.enabledMethods) {
            //this.threadHandles.add(); // TODO; Causes constructor to not return
            scheduleThread(new TaskScheduleFetchRecorderData(this, c, executorService), 5L, 15L, TimeUnit.SECONDS);
        }
        LOGGER.info(String.format("%s.init() returning.", this.getClass().getSimpleName()));
        return;
    }

    /**
     *  TODO IMPORTANT: This it not the best place to schedule threads because
     *  if this object is removed, the reference to the thread gets lost but is
     *  still scheduled.
     */
    private ScheduledFuture<?> scheduleThread(Runnable task, long delay, long interval, TimeUnit timeUnit) throws RejectedExecutionException {
        try {
            ScheduledFuture<?> future = scheduledExecutorService.scheduleAtFixedRate(
                    task,
                    delay,
                    interval,
                    timeUnit);
            return future;
        } catch (RejectedExecutionException e) {
            LOGGER.error(String.format("Could not schedule thread: %s", task));
            throw new RejectedExecutionException(e);
        }
    }

    public void destruct() {
        this.threadHandles.stream().forEach(
                t -> {
                    boolean isCancelled = t.cancel(false);// ScheduledFuture.cancel() ensures is that isDone method always return true
                    LOGGER.info(String.format("Task %s cancelled? %s", t, isCancelled));
                }
        );
    }

    public static <T> Callable<Result<T>> makeCallable(Recorder recorder, Preparable<Recorder, T> preparable) {
        return () -> {
            try {
                return preparable.prepare(recorder);
            } catch (Exception e) {
                return Result.failure(e);
            }
        };
    }

    //private static <Z> final Function<Recorder, Preparable<Recorder, Callable<Result<Z>>> recorderToCallable = r -> {};
    //private static <A, B> Function<Callable<A>, FutureTask<Result<B>>> callableToFutureTask = c -> new FutureTask<Result<B>>(c);


    @Override
    public String toString() {
        return String.format("%s(%s, %s, %s)", getClass().getSimpleName(), getId(), getName(), getNetworkTarget());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public NetworkTarget getNetworkTarget() {
        return networkTargetUserPass;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public RecorderRunningStatesEnum getRecorderRunningState() {
        return recorderRunningState;
    }

    /**
     * This does NOT send any commands to the actual SMP. It sets the memory
     * representation of the polled running state.
     *
     * @param recorderRunningState
     */
    public void setRecorderRunningState(RecorderRunningStatesEnum recorderRunningState) {
        this.recorderRunningState = recorderRunningState;
    }

    @Override
    public RecorderRunningStatesEnum fetchRunningState() {
        LOGGER.debug("{}.fetchRunningState() called.", this); // TODO enum toString not called if class not made final
        Result<String> rString = this.sendMessage(SisProtocol.Gettable.RUNNING_STATE);
        return rString.flatMap(StrUtils::parseAsInteger)
                .map(RecorderRunningStatesEnum::of).orElse(() -> Result.success(RecorderRunningStatesEnum.UNKNOWN))
                .map(s -> this.recorderRunningState = s)
                .getOrElse(() -> RecorderRunningStatesEnum.UNKNOWN);
    }

    @Override
    public String fetchFirmwareVersion() {
        Result<String> rResult = this.sendMessage(SisProtocol.Gettable.FIRMWARE);
        return rResult.map(p -> this.firmwareVersion = p).getOrElse(() -> "?");
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchFirmwareVersion(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchFirmwareVersion() could not fetch. %s", this, e)));
    }

    @Override
    public String fetchUnitName() {
        Result<String> result = this.sendMessage(SisProtocol.Gettable.UNIT_NAME);
        return result.map(p -> this.unitName = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchTelnetPort() {
        Result<String> rResult = this.sendMessage(SisProtocol.Gettable.TELNET_PORT);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchTelnetPort(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchTelnetPort() could not fetch. %s", this, e)));
        return rResult.map(p -> this.telnetPort = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchSshPort() {
        Result<String> rResult = this.sendMessage(SisProtocol.Gettable.SSH_PORT);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchSshPort(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchSshPort() could not fetch: %s", this, e)));
        return rResult.map(p -> this.sshPort = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchHttpPort() {
        Result<String> rResult = this.sendMessage(SisProtocol.Gettable.HTTP_PORT);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchHttpPort(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchHttpPort() could not fetch: %s", this, e)));
        return rResult.map(p -> this.httpPort = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchSnmpPort() {
        Result<String> result = this.sendMessage(SisProtocol.Gettable.SNMP_PORT);
        //result.forEachOrFail(s -> {
        //    LOGGER.debug(String.format("%s.fetchSnmpPort(): %s", this, s));
        //    this.snmpPort = s;
        //}).forEach(e -> LOGGER.error(String.format("%s.fetchSnmpPort() could not fetch: %s", this, e)));
        return result.map(p -> this.snmpPort = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchHttpsPort() {
        Result<String> rResult = this.sendMessage(SisProtocol.Gettable.HTTPS_PORT);
        //rResult.forEachOrFail(s -> {
        //    LOGGER.debug(String.format("%s.fetchHttpsPort(): %s", this, s));
        //    this.httpsPort = s;
        //}).forEach(e -> LOGGER.error(String.format("%s.fetchHttpsPort() could not fetch: %s", this, e)));
        return rResult.map(p -> this.httpsPort = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchSnmpUnitLocation() {
        Result<String> rResult = this.sendMessage(SisProtocol.Gettable.SNMP_UNIT_LOCATION);
        //rResult.forEachOrFail(s -> {
        //    LOGGER.debug(String.format("%s.fetchSnmpUnitLocation(): %s", this, s));
        //    this.snmpUnitLocation = s;
        //}).forEach(e -> LOGGER.error(String.format("%s.fetchSnmpUnitLocation() could not fetch: %s", this, e)));
        return rResult.map(p -> this.snmpUnitLocation = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchSnmpUnitContact() {
        Result<String> result = this.sendMessage(SisProtocol.Gettable.SNMP_UNIT_CONTACT);
        //result.forEachOrFail(s -> {
        //    LOGGER.debug(String.format("%s.fetchSnmpUnitContact(): %s", this, s));
        //    this.snmpUnitContact = s;
        //}).forEach(e -> LOGGER.error(String.format("%s.fetchSnmpUnitContact() could not fetch: %s", this, e)));
        return result.map(p -> this.snmpUnitContact = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchSnmpPrivateCommunityString() {
        Result<String> result = this.sendMessage(SisProtocol.Gettable.SNMP_PRIVATE_COMMUNITY_STRING);
        //result.forEachOrFail(s -> {
        //    LOGGER.debug(String.format("%s.fetchSnmpPrivateCommunityString(): %s", this, s));
        //    this.snmpPrivateCommunityString = s;
        //}).forEach(e -> LOGGER.error(String.format("%s.fetchSnmpPrivateCommunityString() could not fetch: %s", this, e)));
        return result.map(p -> this.snmpPrivateCommunityString = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchSnmpPublicCommunityString() {
        Result<String> result = this.sendMessage(SisProtocol.Gettable.SNMP_PUBLIC_COMMUNITY_STRING);
        //result.forEachOrFail(s -> {
        //    LOGGER.debug(String.format("%s.fetchSnmpPublicCommunityString(): %s", this, s));
        //    this.snmpPublicCommunityString = s;
        //}).forEach(e -> LOGGER.error(String.format("%s.fetchSnmpPublicCommunityString() could not fetch: %s", this, e)));
        return result.map(p -> this.snmpPublicCommunityString = p).getOrElse(() -> "?");
    }
//
    @Override
    public String fetchSnmpState() {
        Result<String> rResult = this.sendMessage(SisProtocol.Gettable.SNMP_STATE);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchSnmpState(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchSnmpState() could not fetch: %s", this, e)));
        return rResult.map(p -> this.snmpState = p).getOrElse(() -> "?");
        //this.snmpState = result;
    }

    @Override
    public String fetchTimezone() {
        Result<String> rResult = this.sendMessage(SisProtocol.Gettable.TIMEZONE);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchTimezone(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchTimezone() could not fetch: %s", this, e)));
        return rResult.map(p -> this.timezone = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchDhcpMode() {
        Result<String> rResult = this.sendMessage(SisProtocol.Gettable.DHCP_MODE);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchDhcpMode(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchDhcpMode() could not fetch: %s", this, e)));
        return rResult.map(p -> this.dhcpMode = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchMacAddress() {
        Result<String> rResult = this.sendMessage(SisProtocol.Gettable.MAC_ADDRESS);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchMacAddress(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchMacAddress() could not fetch: %s", this, e)));
        return rResult.map(p -> this.macAddress = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchPortTimeout() {
        Result<String> rResult = this.sendMessage(SisProtocol.Gettable.PORT_TIMEOUT);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchPortTimeout(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchPortTimeout() could not fetch: %s", this, e)));
        return rResult.map(p -> this.portTimeout = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchGlobalPortTimeout() {
        Result<String> rResult = this.sendMessage(SisProtocol.Gettable.GLOBAL_PORT_TIMEOUT);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchGlobalPortTimeout(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchGlobalPortTimeout() could not fetch: %s", this, e)));
        return rResult.map(p -> this.globalPortTimeout = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchModelName() {
        Result<String> rResult = this.sendMessage(SisProtocol.Gettable.MODEL_NAME);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchModelName(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchModelName() could not fetch: %s", this, e)));
        return rResult.map(p -> this.modelName = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchModelDescription() {
        Result<String> rResult = this.sendMessage(SisProtocol.Gettable.MODEL_DESCRIPTION);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchModelDescription(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchModelDescription() could not fetch: %s", this, e)));
        return rResult.map(p -> this.modelDescription = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchActiveAlarms() {
        Result<String> result =  this.sendMessage(SisProtocol.Gettable.ACTIVE_ALARMS);
        //result.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchActiveAlarms(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchActiveAlarms() could not fetch: %s", this, e)));
        return result.map(p -> this.activeAlarms = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchPartNumber() {
        Result<String> rResult =  this.sendMessage(SisProtocol.Gettable.PART_NUMBER);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchPartNumber(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchPartNumber() could not fetch: %s", this, e)));
        return rResult.map(p -> this.partNumber = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchRegisterCoverage() {
        Result<String> rResult =  this.sendMessage(SisProtocol.Gettable.COVERAGE);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchRegisterCoverage(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchRegisterCoverage() could not fetch: %s", this, e)));
        return rResult.map(p -> this.registerCoverage = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchRegisterPresenter() {
        Result<String> rResult =  this.sendMessage(SisProtocol.Gettable.PRESENTER);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchRegisterPresenter(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchRegisterPresenter() could not fetch: %s", this, e)));
        return rResult.map(p -> this.registerPresenter = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchRegisterRelation() {
        Result<String> rResult =  this.sendMessage(SisProtocol.Gettable.COVERAGE);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchRegisterCoverage(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchRegisterCoverage() could not fetch: %s", this, e)));
        return rResult.map(p -> this.registerRelation = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchRegisterSource() {
        Result<String> rResult =  this.sendMessage(SisProtocol.Gettable.COVERAGE);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchRegisterCoverage(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchRegisterCoverage() could not fetch: %s", this, e)));
        return rResult.map(p -> this.registerSource = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchRegisterSubject() {
        Result<String> rResult =  this.sendMessage(SisProtocol.Gettable.COVERAGE);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchRegisterCoverage(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchRegisterCoverage() could not fetch: %s", this, e)));
        return rResult.map(p -> this.registerSubject = p).getOrElse(() -> "?");
    }

    @Override
    public String fetchRegisterTitle() {
        Result<String> rResult =  this.sendMessage(SisProtocol.Gettable.COVERAGE);
        //rResult.forEachOrFail(s -> LOGGER.debug(String.format("%s.fetchRegisterCoverage(): %s", this, s))).forEach(e -> LOGGER.error(String.format("%s.fetchRegisterCoverage() could not fetch: %s", this, e)));
        return rResult.map(p -> this.registerTitle = p).getOrElse(() -> "?");
    }

    //@Override
    //public String getUnitName() {
    //    return this.unitName;
    //}
//
    //@Override
    //public String getTelnetPort() {
    //    return this.telnetPort;
    //}
//
    //@Override
    //public String getSshPort() {
    //    return this.sshPort;
    //}
//
    //@Override
    //public String getHttpPort() {
    //    return this.httpPort;
    //}
//
    //@Override
    //public String getSnmpPort() {
    //    return this.snmpPort;
    //}
//
    //@Override
    //public String getHttpsPort() {
    //    return this.httpsPort;
    //}
//
    //@Override
    //public String getSnmpUnitLocation() {
    //    return this.snmpUnitLocation;
    //}
//
    //@Override
    //public String getSnmpUnitContact() {
    //    return this.snmpUnitContact;
    //}
//
    //@Override
    //public String getSnmpPrivateCommunityString() {
    //    return this.snmpPrivateCommunityString;
    //}
//
    //@Override
    //public String getSnmpPublicCommunityString() {
    //    return this.snmpPublicCommunityString;
    //}
//
    //@Override
    //public String getSnmpState() {
    //    return this.snmpState;
    //}
//
    //@Override
    //public String getTimezone() {
    //    return this.timezone;
    //}
//
    //@Override
    //public String getDhcpMode() {
    //    return this.dhcpMode;
    //}
//
    //@Override
    //public String getMacAddress() {
    //    return this.macAddress;
    //}
//
    //@Override
    //public String getPortTimeout() {
    //    return this.portTimeout;
    //}
//
    //@Override
    //public String getGlobalPortTimeout() {
    //    return this.globalPortTimeout;
    //}
//
    //@Override
    //public String getModelName() {
    //    return this.modelName;
    //}
//
    //@Override
    //public String getModelDescription() {
    //    return this.modelDescription;
    //}
//
    //@Override
    //public String getActiveAlarms() {
    //    return this.activeAlarms;
    //}
//
    //@Override
    //public String getPartNumber() {
    //    return this.partNumber;
    //}
//
    //@Override
    //public String getRegisterCoverage() {
    //    return this.registerCoverage;
    //}
//
    //@Override
    //public String getRegisterPresenter() {
    //    return this.registerPresenter;
    //}
//
    //@Override
    //public String getRegisterRelation() {
    //    return this.registerRelation;
    //}
//
    //@Override
    //public String getRegisterSource() {
    //    return this.registerSource;
    //}
//
    //@Override
    //public String getRegisterSubject() {
    //    return this.registerSubject;
    //}
//
    //@Override
    //public String getRegisterTitle() {
    //    return this.registerTitle;
    //}

    /**
     * Update method that handles
     * synchronizing this recorder's state with the
     * recording instance's metadata.
     *
     * Must deal with 127 character value length limit on the SMP 351.
     */
    //TODO Try to understand why this method succeeds to get the right metadata even then this.location is null. onRecorderRunningStateChange, e.g. requires non-null.
    @Override
    public void onMetadataChange(Metadata metadata) { // TODO CANNOT DEP ON RECORDINGINSTANCE DUE TO CIRCULAR DEP, SOLVE LATER WITH MESSAGE BUS
        LOGGER.debug(String.format("%s.onMetadataChange() called. Detected change in metadata state.", this));

        Arrays.stream(SisProtocol.Settable.values())
                .filter(e -> enabledSettableFields.contains(e))// TODO: Optionally remove "enabled" abstraction by removing this filter and corresponding class member.
                .map(e -> extractMetadataValueFrom(e, metadata))
                .peek(t -> LOGGER.info(String.format("%s.onMetadataChange() detected: (%s, %s)", this, t._1, t._2)))
                .forEach(t -> sendMessage(t._1, t._2));
    }

    private Tuple<SisProtocol.Settable, String> extractMetadataValueFrom(SisProtocol.Settable field, Metadata metadata) {
        Result<SisProtocol.Settable> rSettable = Result.of(field);
        Result<DublinCore> rField = rSettable.map(settable -> settable.dcProperty);
        Result<Metadata> rMetadata = Result.of(metadata);

        Result<Tuple<SisProtocol.Settable, String>> value =
                rMetadata.flatMap(met -> rField
                        .flatMap(fld -> rSettable.map(
                                set -> new Tuple<>(set, met.get(fld)))));

        return value.successValue(); // TODO make functional
    }

    /** Added this to replace old. Uses observer pattern now with push of info instead of pull */
    private Tuple<SisProtocol.Settable, String> newExtractMetadataForValue(SisProtocol.Settable field, RecordingInstance recordingInstance){
        Result<SisProtocol.Settable> rSettable = Result.of(field);
        Result<DublinCore> rField = rSettable.map(settable -> settable.dcProperty);
        Result<RecordingInstance> rRecordingInstance = Result.of(recordingInstance);

        Result<Metadata> rMetadata = rField
                .flatMap(prop -> rRecordingInstance.map(RecordingInstance::getMetadata)).orElse(() -> Result.of(new Metadata.Builder().build()));
        Result<String> rMetadataValue = rMetadata.flatMap(meta -> rField.map(dubcore -> meta.get(dubcore)));
        Result<Tuple<SisProtocol.Settable, String>> rOutput = rMetadataValue.flatMap(met -> rSettable.map(set -> new Tuple<>(set, met)));

        return rOutput.successValue(); // TODO make functional
    }


    /**
     * This function applies necessary changes to input such that
     * it is acceptable to send over the SIS Protocol.
     *
     * This is also defined in {@link SisProtocol} with the foresight of
     * being possible removed due to another model of recorder that possibly
     * shares many aspects of the {@link SisProtocol}.
     *
     * @param str
     * @return
     */
    private static String validateInput(String str) {
        return shortenTo127.apply(str);
    }
    public static Function<String, String> shortenTo127 = StrUtils.shortenStringToLength.apply(127);

    private static Result<Metadata> getRecordingInstanceMetadataAsResult(Room loc) {
        Result<RecordingInstance> rI = getRecordingInstanceAsResult(loc);
        return rI.map(RecordingInstance::getMetadata);
    }

    private static Result<RecordingInstance> getRecordingInstanceAsResult(Room loc) {
        Result<Room> rLocation = Result.of(loc);

        return rLocation.flatMap(r -> {
                    return r.getRecordingInstance().isPresent()
                            ? Result.success(r.getRecordingInstance().get())
                            : Result.failure("Recording instance does not exist.");
                });
        //    try {
        //        return loc.getRecordingInstance();
        //    } catch (NoSuchElementException n) {
        //        return Result.failure(String.format("Could not get metadata for non-existent recording instance: %s", n.getMessage()), n);
        //    }
        //});
        //return rI;
    }

    private static Result<Room> getLocationAsResult(Room loc) {
        return Result.of(loc, "Failed to find reference to location. It was null.");
    }

    private static Result<RecorderRunningStatesEnum> getRecordingRunningStateAsResult(Room loc) {
        Result<RecordingInstance> rI = getRecordingInstanceAsResult(loc);
        return rI.map(RecordingInstance::getRecordingRunningState);
    }

    /**
     * The SMP should handle a booking change because it must know to stop recording.
     * <p>
     * 1. Stopping a running recording.
     * 2. Setting metadata fields (this is handled by the recording instance itself and eventually
     * gets processed by onMetadataChange. Be careful not to trigger onMetadataChange twice
     * by handling this logic here. It must be done on the observed side.
     */
    @Override
    public void onRecorderRunningStateChange(RecorderRunningStatesEnum state) {
        LOGGER.debug(String.format("%s.onRecorderRunningStateChange() detected: %s", this, state));
        Result<RecorderRunningStatesEnum> rRecorderRunningState = Result.of(state);
        rRecorderRunningState
                .forEachOrFail(r -> this.sendMessage(r.command))
                .forEach(msg -> LOGGER.error(String.format("%s.onRecorderRunningStateChange(): %s", this, msg)));
    }

    /**
     * This sends standardized commands to the
     * SMP.
     *
     * <p>
     * This implements the public sendMessage() method,
     * which handles the translation of standardized commands
     * into commands that the SMP device understands.
     * <p>
     * This method is responsible for intepreting responses.
     *
     * TODO Be VERY careful logging ENUMs. If the ENUM.name() does not work, there is a bug in the logger that makes a thread halt without error.
     *
     * @return characters that match the Pattern's, else null.
     */
    @Override
    public Result<String> sendMessage(SisProtocol.Gettable whatToGet) {
        LOGGER.debug(String.format("%s.sendMessage(%s) called", this, whatToGet)); // TODO silently fails to cast enum
        try {
            String rawResponse = sshSessionManagerService.send(
                    this.getNetworkTarget(),
                    whatToGet.payload,
                    s -> p -> p.matcher(s).find(),
                    whatToGet.correspondencePattern);

            String response = recorderRunningStateExtractorFunction.apply(whatToGet).apply(rawResponse);
            return Result.success(response);
        } catch (InterruptedException i) {
           LOGGER.error(String.format("%s.sendMessage(%s, %s) was interrupted.", this, "gettable", whatToGet));
           return Result.failure(String.format("%s.sendMessage(%s, %s) was interrupted.", this, "gettable", whatToGet), i);
        } catch (Exception e) {
            LOGGER.error(String.format("%s.sendMessage(%s, %s) could not send.", this, "gettable", whatToGet), e);
            return Result.failure(String.format("%s.sendMessage(%s, %s) could not send.", this, "gettable", whatToGet), e);
        }
    }

    /**
     * This function uses the {@link SisProtocol.Gettable#meaningfulResponsePatternAsGroup} to
     *
     * <ul>
     *     <li>Create a {@link Matcher}</li>
     *     <li>Execute {@link Matcher#find()}</li>
     *     <li>Extract match of group 1 from {@link Matcher}</li>
     * </ul>
     */
    private final static Function<SisProtocol.Gettable, Function<String, String>> recorderRunningStateExtractorFunction = g -> s -> {
        Matcher m = g.meaningfulResponsePatternAsGroup.matcher(s);
        m.find();
        return m.group(1);
    };

    /**
     * Queries a field and sets it to a given value.
     *
     * <p>
     * The field name strings correspond to enum keys as strings.
     * <p>
     * This command is similar to @see Smp351#sendMessage
     *
     * @param settableRegister
     * @param value
     */
    //TODO Unsure about Propogate error feedback to frontend caller
    @Override
    public void sendMessage(SisProtocol.Settable settableRegister, String value) {
        String input = validateInput(value);
        LOGGER.debug(String.format("%s.sendMessage(%s) expects response: %s",
                this,
                StrUtils.swapNonPrintable(settableRegister.buildPayload(value)),
                StrUtils.swapNonPrintable(settableRegister.correspondencePattern.toString())));
        try {
            sshSessionManagerService.send(this.getNetworkTarget(),
                    settableRegister.buildPayload(input),
                    str -> pat -> pat.matcher(str).find(), // define how EOF is determined
                    settableRegister.correspondencePattern);
        } catch (Exception i) {
            LOGGER.error(String.format("%s.sendMessage(%s, %s) could not send.", this, settableRegister, value), i);
        }
    }

    //TODO Unsure about Propogate error feedback to frontend caller
    @Override
    public void sendMessage(SisProtocol.Command command) {
        LOGGER.debug(String.format("%s.sendMessage(%s) expects response: %s",
                this,
                StrUtils.swapNonPrintable(command.command),
                StrUtils.swapNonPrintable(command.correspondencePattern.toString())));
        try {
            sshSessionManagerService.send(this.getNetworkTarget(),
                    command.command,
                    str -> pat -> pat.matcher(str).find(), // define how EOF is determined
                    command.correspondencePattern);
        } catch (Exception i) {
            LOGGER.error(String.format("%s.sendMessage(%s) could not send.", this, command), i);
        }
    }
}
