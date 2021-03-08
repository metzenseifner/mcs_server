package at.ac.uibk.mcsconnect.bookingrepo.impl.hidden;

public enum EsbRolesEnum {

    VORTRAGENDER("V"),
    STUDENT("S");

    private String id;

    EsbRolesEnum(String id) {
        this.id = id;
    }

    public String toString() {
        return this.id;
    }
}
