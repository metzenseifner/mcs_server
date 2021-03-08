package at.ac.uibk.mcsconnect.recorderservice.api;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.common.api.Views;
import at.ac.uibk.mcsconnect.functional.common.Result;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * Interface class to communicate with recorder objects
 *
 * By implementing the {@link RecordingInstanceObserver},
 * the recorders can be notified of changes to the recording instance
 * state. This could be replaced by a message bus in the future.
 * This means that recorders, despite belonging to a room,
 * they bind to a single recording instance.
 *
 * TODO Make this interface vendor independent.
 *
 * @author Jonathan Komar
 * @since 28.01.2019
 */
public interface Recorder extends RecordingInstanceObserver {

    /**
     * Gets id of device.
     */
    @JsonView(Views.Admin.class)
    String getId();

    /**
     * Gets human-readable name of the recorder.
     *
     * @return
     */
    @JsonView(Views.Admin.class)
    String getName();

    /**
     * Gets wrapper for network target information.
     *
     * @return
     */
    @JsonView(Views.Admin.class)
    NetworkTarget getNetworkTarget();

    //@JsonView(Views.Admin.class)
    //String getFirmwareVersion();

    //@JsonView(Views.Admin.class)
    //String getUnitName();
//
    //@JsonView(Views.Admin.class)
    //String getTelnetPort();
//
    //@JsonView(Views.Admin.class)
    //String getSshPort();
//
    //@JsonView(Views.Admin.class)
    //String getHttpPort();
//
    //@JsonView(Views.Admin.class)
    //String getSnmpPort();
//
    //@JsonView(Views.Admin.class)
    //String getHttpsPort();
//
    //@JsonView(Views.Admin.class)
    //String getSnmpUnitLocation();
//
    //@JsonView(Views.Admin.class)
    //String getSnmpUnitContact();
//
    //@JsonView(Views.Admin.class)
    //String getSnmpPrivateCommunityString();
//
    //@JsonView(Views.Admin.class)
    //String getSnmpPublicCommunityString();
//
    //@JsonView(Views.Admin.class)
    //String getSnmpState();
//
    //@JsonView(Views.Admin.class)
    //String getTimezone();
//
    //@JsonView(Views.Admin.class)
    //String getDhcpMode();
//
    //@JsonView(Views.Admin.class)
    //String getMacAddress();
//
    //@JsonView(Views.Admin.class)
    //String getPortTimeout();
//
    //@JsonView(Views.Admin.class)
    //String getGlobalPortTimeout();
//
    //@JsonView(Views.Admin.class)
    //String getModelName();
//
    //@JsonView(Views.Admin.class)
    //String getModelDescription();
//
    //@JsonView(Views.Admin.class)
    //String getActiveAlarms();
//
    //@JsonView(Views.Admin.class)
    //String getPartNumber();
//
    //@JsonView(Views.Admin.class)
    //String getRegisterCoverage();
//
    //@JsonView(Views.Admin.class)
    //String getRegisterPresenter();
//
    //@JsonView(Views.Admin.class)
    //String getRegisterRelation();
//
    //@JsonView(Views.Admin.class)
    //String getRegisterSource();
//
    //@JsonView(Views.Admin.class)
    //String getRegisterSubject();
//
    //@JsonView(Views.Admin.class)
    //String getRegisterTitle();
//
    @JsonView(Views.Public.class)
    RecorderRunningStatesEnum getRecorderRunningState();
//
    @JsonView(Views.Public.class)
    void setRecorderRunningState(RecorderRunningStatesEnum recorderRunningState);

    /**
     *
     * @param recorderCommandsEnum
     * @return A StrUtil containing the data extracted according to the {@link SisProtocol.Gettable#meaningfulResponsePatternAsGroup}
     * @throws InterruptedException
     */
    @JsonIgnore
    Result<String> sendMessage(SisProtocol.Gettable recorderCommandsEnum);
    @JsonIgnore
    void sendMessage(SisProtocol.Settable recorderCommandsEnum, String value); // TODO: Ideally input is DublinCoreGetters property. Fails if not supported.
    @JsonIgnore
    void sendMessage(SisProtocol.Command recorderCommandEnum);
}