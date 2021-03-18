package at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto;

import java.io.Serializable;

/** Room identifier is key for this value in RoomsDTO.rooms */
public class RoomDTO implements Serializable {

    private String name;

    /** Where String is useless name of set */
    public java.util.Map<String, RecorderDTO> recorders;
    public java.util.Map<String, TerminalDTO> terminals;

    public RoomDTO() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public java.util.Map<String, RecorderDTO> getRecorders() {
        return this.recorders;
    }

    public void setRecorders(java.util.Map<String, RecorderDTO> recorders) {
        this.recorders = recorders;
    }

    public java.util.Map<String, TerminalDTO> getTerminals() {
        return terminals;
    }

    public void setTerminals(java.util.Map<String, TerminalDTO> terminals) {
        this.terminals = terminals;
    }

    public String toString() {
        return String.format("%s(%s, %s, %s)", this.getClass().getSimpleName(), getName(), getRecorders(), getTerminals());
    }
}
