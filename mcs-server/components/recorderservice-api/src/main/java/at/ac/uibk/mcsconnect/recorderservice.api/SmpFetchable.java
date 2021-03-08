package at.ac.uibk.mcsconnect.recorderservice.api;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public interface SmpFetchable {

    RecorderRunningStatesEnum fetchRunningState();
    String fetchFirmwareVersion();
    String fetchUnitName();
    String fetchTelnetPort();
    String fetchSshPort();
    String fetchHttpPort();
    String fetchSnmpPort();
    String fetchHttpsPort();
    String fetchSnmpUnitLocation();
    String fetchSnmpUnitContact();
    String fetchSnmpPrivateCommunityString();
    String fetchSnmpPublicCommunityString();
    String fetchSnmpState();
    String fetchTimezone();
    String fetchDhcpMode();
    String fetchMacAddress();
    String fetchPortTimeout();
    String fetchGlobalPortTimeout();
    String fetchModelName();
    String fetchModelDescription();
    String fetchActiveAlarms();
    String fetchPartNumber();
    String fetchRegisterCoverage();
    String fetchRegisterPresenter();
    String fetchRegisterRelation();
    String fetchRegisterSource();
    String fetchRegisterSubject();
    String fetchRegisterTitle();

    Set<Consumer<SmpFetchable>> enabledMethods = new HashSet<Consumer<SmpFetchable>>(
            Arrays.asList( // TODO Compiler does not catch multiple entries
                    SmpFetchable::fetchRunningState
                    //SmpFetchable::fetchFirmwareVersion,
                    //SmpFetchable::fetchUnitName,
                    //SmpFetchable::fetchTelnetPort,
                    //SmpFetchable::fetchSshPort,
                    //SmpFetchable::fetchHttpPort,
                    //SmpFetchable::fetchSnmpPort,
                    //SmpFetchable::fetchHttpsPort,
                    //SmpFetchable::fetchSnmpUnitLocation,
                    //SmpFetchable::fetchSnmpUnitContact,
                    //SmpFetchable::fetchSnmpPrivateCommunityString,
                    //SmpFetchable::fetchSnmpPublicCommunityString,
                    //SmpFetchable::fetchSnmpState,
                    //SmpFetchable::fetchTimezone,
                    //SmpFetchable::fetchDhcpMode,
                    //SmpFetchable::fetchMacAddress,
                    //SmpFetchable::fetchPortTimeout,
                    //SmpFetchable::fetchGlobalPortTimeout,
                    //SmpFetchable::fetchModelName,
                    //SmpFetchable::fetchModelDescription,
                    //SmpFetchable::fetchActiveAlarms
                    //SmpFetchable::fetchPartNumber,
                    //SmpFetchable::fetchRegisterCoverage,
                    //SmpFetchable::fetchRegisterPresenter,
                    //SmpFetchable::fetchRegisterRelation,
                    //SmpFetchable::fetchRegisterSource,
                    //SmpFetchable::fetchRegisterSubject,
                    //SmpFetchable::fetchRegisterTitle
            )
    );
}
