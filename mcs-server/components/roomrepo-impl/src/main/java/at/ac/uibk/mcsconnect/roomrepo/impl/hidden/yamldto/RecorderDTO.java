package at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto;

public class RecorderDTO {
    private String name;
    private String type; // TODO Currently unused because there is only one type of recorder
    /** Where String in submap is "target" */
    private NetworkTargetDTO target;

    public RecorderDTO() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NetworkTargetDTO getTarget() {
        return target;
    }

    public void setTarget(NetworkTargetDTO target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
