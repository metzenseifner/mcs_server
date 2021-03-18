package at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto;

import java.io.Serializable;


public class RecorderDTO implements Serializable {
    private String name;
    private String type; // TODO Currently unused because there is only one type of recorder
    /** Where String in submap is "target" */
    private NetworkTargetDTO networkTarget;

    public RecorderDTO() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NetworkTargetDTO getNetworkTarget() {
        return networkTarget;
    }

    public void setNetworkTarget(NetworkTargetDTO networkTarget) {
        this.networkTarget = networkTarget;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
