package at.ac.uibk.mcsconnect.roomrepo.impl;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;
import at.ac.uibk.mcsconnect.roomrepo.api.TerminalFactory;
import at.ac.uibk.mcsconnect.roomrepo.impl.hidden.TerminalImpl;
import org.osgi.service.component.annotations.Component;

@Component(
        name = "at.ac.uibk.mcsconnect.roomservice.impl.TerminalFactoryImpl"
)
public class TerminalFactoryImpl implements TerminalFactory {

    @Override
    public Terminal create(String id, NetworkTarget networkTarget) {
        return new TerminalImpl(id, networkTarget, "");
    }

    @Override
    public Terminal create(String id, String name, NetworkTarget networkTarget) {
        return new TerminalImpl(id, networkTarget, name);
    }
}
