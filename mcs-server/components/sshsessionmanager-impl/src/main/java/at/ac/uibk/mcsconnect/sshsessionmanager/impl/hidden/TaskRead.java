package at.ac.uibk.mcsconnect.sshsessionmanager.impl.hidden;

import at.ac.uibk.mcsconnect.sshsessionmanager.impl.SshChannelShellLockable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.regex.Pattern;

import static at.ac.uibk.mcsconnect.common.api.StrUtils.stringBuilderToHexString;
import static at.ac.uibk.mcsconnect.common.api.StrUtils.swapNonPrintable;


/**
 * Owned by {@link SshChannelShellLockable}.
 * <p>
 * Encapsulate the read (blocking IO) functionality of {@link SshChannelShellLockable}
 * in a thread so that it is possible to interrupt it. In other words, kill it if it exceeds a time limit.
 */
public class TaskRead implements Callable<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRead.class);

    private final BufferedInputStream in;
    private final Function<String, Function<Pattern, Boolean>> bufferCondition;
    private final Pattern correspondencePattern;

    public TaskRead(BufferedInputStream in, Function<String, Function<Pattern, Boolean>> bufferCondition, Pattern correspondencePattern) {
        this.in = in;
        this.bufferCondition = bufferCondition;
        this.correspondencePattern = correspondencePattern;
    }

    public String call() {
        StringBuilder sb = new StringBuilder(1024);
        int codePoint;
        BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")), 1024);

        try {
            while (true) {
                if ((codePoint = br.read()) != -1) {
                    sb.appendCodePoint(codePoint);
                    LOGGER.trace(String.format("Read loop: %s", swapNonPrintable(sb.toString())));
                    if (bufferContainsString(sb.toString())) {
                        break;
                    }
                } else {
                    break;
                }
            }
            LOGGER.debug(String.format("Response in hex: %s", stringBuilderToHexString.apply(sb)));
            return sb.toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private final Boolean bufferContainsString(final String str) {
        return bufferCondition.apply(str).apply(correspondencePattern);
    }

    public String toString() {
        return String.format("TaskRead of pattern %s", correspondencePattern.toString());
    }

    public void cancel() {
        LOGGER.warn(String.format("%s cancelled. There was possible a communication error with a recorder.", this));
    }
}
