package at.ac.uibk.mcsconnect.sshsessionmanager.impl.integration;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.sshsessionmanager.api.SshSessionManagerService;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public class FakeSshSessionManagerService implements SshSessionManagerService {

    public List<String> calls;

    @Override
    public String send(NetworkTarget networkTargetUserPass, String message, Function<String, Function<Pattern, Boolean>> bufferMatchCondition, Pattern dataExtractionPattern) throws InterruptedException {
        String call = String.format("Message(%s %s, %s, %s)", networkTargetUserPass, message, bufferMatchCondition, dataExtractionPattern);
        calls.add(call);
        return call;
    }

    public List<String> getCalls() {
        return calls;
    };
}
