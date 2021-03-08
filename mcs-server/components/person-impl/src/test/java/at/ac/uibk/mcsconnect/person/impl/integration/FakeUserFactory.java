package at.ac.uibk.mcsconnect.person.impl.integration;

import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.person.api.UserFactory;

public class FakeUserFactory implements UserFactory {
    @Override
    public User create(String id, String name, String email) {
        return new FakeUser(id, name, email);
    }
}
