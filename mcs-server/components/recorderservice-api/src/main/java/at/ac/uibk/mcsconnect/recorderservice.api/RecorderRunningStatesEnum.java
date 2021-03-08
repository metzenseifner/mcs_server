package at.ac.uibk.mcsconnect.recorderservice.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes recorder running state and associated behavior.
 * <p>
 * Not to be confused with current implementation of record<strong>ing</strong>
 * running state, which should be an enum in future versions.
 *
 * <h1>{@link RecorderRunningStatesEnum#UNKNOWN}</h1>
 * <ul>
 *     <li>implies state not yet determined</li>
 *     <li>assumed to be recording so that it tries to load settings from persistent memory</li>
 * </ul>
 *
 */
public enum RecorderRunningStatesEnum {

    // constants
    STOPPED("stopped", 0, false, SisProtocol.Command.STOPRECORDING),
    RECORDING("recording", 1, true, SisProtocol.Command.STARTRECORDING),
    PAUSED("paused", 2, true, SisProtocol.Command.PAUSERECORDING),
    UNKNOWN("unknown", -1, true, SisProtocol.Command.STOPRECORDING); // TODO: I used to have Effect of nothing here to avoid MCS stopping a recording it does not know about

    // private fields
    private static final Map<Integer, RecorderRunningStatesEnum> intToStateMap = new HashMap<>();
    private String stateName;
    private int stateInt;
    private boolean recording;
    public SisProtocol.Command command;

    // resolve integers to constant representations
    static {
        for (RecorderRunningStatesEnum s : values()) intToStateMap.put(s.stateInt, s);
    }

    RecorderRunningStatesEnum(String stateName, int stateInt, boolean recording, SisProtocol.Command command) {
        this.stateName = stateName;
        this.stateInt = stateInt;
        this.recording = recording;
        this.command = command;
    }

    // accessors
    public String getStateName() { return stateName;}
    public int getStateInt() { return stateInt; }
    public boolean isRecording() { return recording; }


    public static RecorderRunningStatesEnum of(String s) {
        return valueOf(s);
    }

    /**
     * Return enum from int.
     *
     * This is necessary because the SMP represents states as
     * integers.
     *
     */
    public static RecorderRunningStatesEnum of(Integer i) {
        RecorderRunningStatesEnum result = intToStateMap.get(i);
        if (result == null)
            throw new IllegalArgumentException(String.format("Invalid recorder running state integer: %s", i));
        return result;
    }
}