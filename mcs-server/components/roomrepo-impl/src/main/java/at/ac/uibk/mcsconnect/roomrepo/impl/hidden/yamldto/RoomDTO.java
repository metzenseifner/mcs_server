package at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto;


import java.util.Map;

/** Room identifier is key for this value in RoomsDTO.rooms */
public class RoomDTO {

    private String name;
    /** Where String is useless name of set */
    private Map<String, RecorderDTO> recorders;
    /** Where String is useless name of set */
    private Map<String, TerminalDTO> terminals;

    public RoomDTO() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, RecorderDTO> getRecorders() {
        return recorders;
    }

    public void setRecorders(Map<String, RecorderDTO> recorders) {
        this.recorders = recorders;
    }

    public Map<String, TerminalDTO> getTerminals() {
        return terminals;
    }

    public void setTerminals(Map<String, TerminalDTO> terminals) {
        this.terminals = terminals;
    }
}
