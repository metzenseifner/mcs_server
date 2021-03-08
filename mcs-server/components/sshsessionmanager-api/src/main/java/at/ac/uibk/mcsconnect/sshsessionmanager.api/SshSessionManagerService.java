package at.ac.uibk.mcsconnect.sshsessionmanager.api;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;

import java.util.function.Function;
import java.util.regex.Pattern;

public interface SshSessionManagerService {

    // TODO To implement Component Factory to control instantiation with Greeter Pattern
    //String FACTORY_ID = "at.ac.uibk.mcsconnect.sshsessionmanager.api.SshSessionServiceFactory";
    //String GREETER_PATTERN_KEY =

    /**
     * @param networkTargetUserPass How to connect to the target.
     * @param message What characters to send defined as a StrUtil for convenience.
     * @param bufferMatchCondition How to know when to stop reading. This is necessary because the caching mechanism keeps channels' ttys open for faster performance.
     * @param dataExtractionPattern What data to extract from the response
     * @return
     * @throws InterruptedException
     */
    String send(NetworkTarget networkTargetUserPass,
                String message,
                Function<String, Function<Pattern, Boolean>> bufferMatchCondition,
                Pattern dataExtractionPattern) throws InterruptedException;



}
