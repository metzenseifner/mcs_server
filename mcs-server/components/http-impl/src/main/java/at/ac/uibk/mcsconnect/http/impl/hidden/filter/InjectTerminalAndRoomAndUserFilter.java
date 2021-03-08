package at.ac.uibk.mcsconnect.http.impl.hidden.filter;

import at.ac.uibk.mcsconnect.functional.common.Executable;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

/**
 * This filter circumvents the headers normally set by shibd. It simply
 * sets the properties used in downstream operations to the appropriate test objects:
 *
 * terminal
 * room
 * user
 */
public class InjectTerminalAndRoomAndUserFilter implements ContainerRequestFilter {

    private final Terminal terminal;
    private final Room room;
    private final User user;

    public InjectTerminalAndRoomAndUserFilter(Terminal terminal, Room room, User user) {
        this.terminal = terminal;
        this.room = room;
        this.user = user;
    }

    /**
     * Covers functionality of {@link EntityResolverFilter} and {@link UserResolverFilter}.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        System.out.println(String.format("%s.filter() called with headers:\n%s\nProperty Names:\n%s", this, requestContext.getHeaders(), requestContext.getPropertyNames()));
        createProgram(requestContext).exec();
        System.out.println(String.format("%s.filter() properties after processing filter: %s\nmcsTerminal: %s\nmcsRoom: %s\nmcsUser: %s",
                this,
                requestContext.getPropertyNames(),
                requestContext.getProperty("mcsTerminal"),
                requestContext.getProperty("mcsRoom"),
                requestContext.getProperty("mcsUser")));
    }

    public Executable createProgram(ContainerRequestContext requestContext) {
        return () -> {
            requestContext.setProperty("mcsTerminal", terminal);
            requestContext.setProperty("mcsRoom", room);
            requestContext.setProperty("mcsUser", user);
        };
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
