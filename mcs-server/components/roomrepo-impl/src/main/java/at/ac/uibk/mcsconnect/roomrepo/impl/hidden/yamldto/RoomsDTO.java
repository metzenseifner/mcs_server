package at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto;


import java.io.Serializable;

public class RoomsDTO implements Serializable {

    /** Where String is the room identifier */
    private java.util.Map<String, RoomDTO> rooms;

    public RoomsDTO() {
    }

    public java.util.Map<String, RoomDTO> getRooms() {
        return rooms;
    }

    public void setRooms(final java.util.Map<String, RoomDTO> rooms) {
        this.rooms = rooms;
    }
}
