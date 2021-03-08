package at.ac.uibk.mcsconnect.common.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

public interface NetworkTarget {

    @JsonView(Views.Public.class)
    String getHost();

    @JsonView(Views.Public.class)
    int getPort();

    @JsonIgnore
    String getUsername();

    @JsonIgnore
    String getPassword();

}
