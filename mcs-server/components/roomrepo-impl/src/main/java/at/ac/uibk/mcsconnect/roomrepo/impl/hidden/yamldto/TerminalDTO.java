package at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto;


import java.io.Serializable;

/** Identifier for this terminal is the key in TerminalsDTO */
public class TerminalDTO implements Serializable {

    private String name;
    private String target;

    public TerminalDTO(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
