package at.ac.uibk.mcsconnect.http.impl.hidden;

import at.ac.uibk.mcsconnect.common.api.Views;
import com.fasterxml.jackson.annotation.JsonView;

import javax.ws.rs.core.Response;

public class ExceptionJson extends Exception {

    @JsonView(Views.Public.class)
    private final Response.Status statuscode;
    @JsonView(Views.Public.class)
    private final String message;

    public ExceptionJson(Response.Status httpStatusCode, Exception e) {
        this.statuscode = httpStatusCode;
        this.message = e.getMessage();
    }

    public ExceptionJson(Response.Status httpStatusCode, String msg) {
        this.statuscode = httpStatusCode;
        this.message = msg;
    }

    public Response.Status getStatuscode() {
        return statuscode;
    }

    public String getMessage() {
        return message;
    }
}
