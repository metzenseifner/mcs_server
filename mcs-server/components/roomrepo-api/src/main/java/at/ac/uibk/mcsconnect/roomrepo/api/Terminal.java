package at.ac.uibk.mcsconnect.roomrepo.api;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.common.api.Views;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonView;

public interface Terminal {

    //@JsonView(Views.Public.class)
    //void setNetworkTarget(NetworkTarget networkTarget);

    @JsonView(Views.Public.class)
    NetworkTarget getNetworkTarget();

    String getName();

    //@JsonView(Views.Public.class)
    //@JsonBackReference
    //    //child
    //void setRoom(Room room);

    //@JsonView(Views.Public.class)
    //@JsonBackReference //child
    //Room getRoom();

    @JsonView(Views.Public.class)
    String getId();

    //@JsonView(Views.Public.class)
    //void setTerminalId(String terminalId);
}