package at.ac.uibk.mcsconnect.person.impl;

import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.person.api.UserFactory;
import at.ac.uibk.mcsconnect.person.impl.hidden.UserImpl;
import org.osgi.service.component.annotations.Component;

@Component(
        name = "at.ac.uibk.mcsconnect.person.impl.UserFactory",
        immediate = true
)
public class UserFactoryImpl implements UserFactory {

    public User create(String id, String name, String email) {
        return new UserImpl(id, name, email);
    }
}
