package at.ac.uibk.mcsconnect.http.impl.hidden.mapper;

import at.ac.uibk.mcsconnect.http.impl.hidden.ExceptionJson;
import org.osgi.service.component.annotations.Component;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Adapts excepts to JSON format for more helpful HTTP responses.
 */
@Component(
        //service = MessageBodyWriter.class,
        property = {
            "service.exported.configs=org.apache.cxf.rs", //
             "org.apache.cxf.rs.provider=true", //
        }
)
@Provider //Mapping = nearest superclass + highest priority
public class ExceptionResponseMapper implements ExceptionMapper<ExceptionJson> {

    private Response.Status status;

    public ExceptionResponseMapper(Response.Status statuscode) {
        this.status = statuscode;
    }


    @Override
    public Response toResponse(ExceptionJson exception) {
        return Response.status(exception.getStatuscode()).entity(exception.getMessage()).build();
    }


    //@Override
    //public Response toResponse(final Exception e) {
    //    return Response.status(Response.Status.BAD_REQUEST)
    //            .entity(new ExceptionJson(status, e))
    //            .type(MediaType.APPLICATION_JSON).build();
    //}
}