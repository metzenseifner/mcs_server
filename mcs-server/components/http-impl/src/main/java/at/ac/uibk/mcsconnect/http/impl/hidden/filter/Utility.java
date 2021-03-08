package at.ac.uibk.mcsconnect.http.impl.hidden.filter;

import at.ac.uibk.mcsconnect.functional.common.Result;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;

public class Utility {

    public static MultivaluedMap<String, String> getHeaders(ContainerRequestContext requestContext) {
        return requestContext.getHeaders();
    }

    public static Result<String> getHeaderValue(String key, MultivaluedMap<String, String> headers) {
        Result<MultivaluedMap<String, String>> rHeaders = Result.of(headers);
        return rHeaders.flatMap(h -> {
            String value = headers.getFirst(key);
            if (value == null) {
                return Result.failure(new RuntimeException(String.format("Missing %s header in headers: %s", key, headers)));
            } else {
                return Result.success(value);
            }
        });
    }

}
