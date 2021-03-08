package at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto;


import java.util.Map;

public class RoomsDTO {
    /** Where String is the room identifier */
    private Map<String, RoomDTO> rooms;

    public RoomsDTO() {
    }

    public Map<String, RoomDTO> getRooms() {
        return rooms;
    }

    public void setRooms(Map<String, RoomDTO> rooms) {
        this.rooms = rooms;
    }
}
