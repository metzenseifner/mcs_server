package at.ac.uibk.mcsconnect.http.impl.hidden.filter;

import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.http.impl.hidden.TerminalCheckRequired;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.person.api.UserFactory;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static at.ac.uibk.mcsconnect.http.impl.hidden.filter.Utility.getHeaderValue;
import static at.ac.uibk.mcsconnect.http.impl.hidden.filter.Utility.getHeaders;

/**
 * Validates user and sets {@link User} context before passing the request
 * on to the transport layer.
 */
//@PreMatching
@TerminalCheckRequired
@Provider // TODO: Make testable by moving most of the algorithm into a static function
@Priority(Priorities.AUTHORIZATION)
public class UserResolverFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserResolverFilter.class);

    @Reference
    private static UserFactory userFactory;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        LOGGER.trace(String.format("%s called.", this));

        MultivaluedMap<String, String> headers = getHeaders(requestContext);

        // func headers -> User
        Result<String> rRemoteUser = getHeaderValue("remote_user", headers);// remote_user is qualified (e-mail); take up until @
        Result<String> rDisplayName = getHeaderValue("display_name", headers);
        Result<String> rEMailAddress = getHeaderValue("remote_user", headers);

        Result<User> rUser = rRemoteUser
                .flatMap(id -> rDisplayName
                        .flatMap(name -> rEMailAddress
                                .map(email -> userFactory.create(
                                        id.split("@")[0],
                                        name,
                                        email))));
        // TODO fail with requestContext.abortWith(Response.serverError());
        rUser.forEachOrFail(user -> requestContext.setProperty("mcsUser", user)).forEach(msg -> LOGGER.warn(String.format("Error detecting user: %s", msg))); // Properties injectable with
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
