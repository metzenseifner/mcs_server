package at.ac.uibk.mcsconnect.person.impl.integration;

import at.ac.uibk.mcsconnect.person.api.User;

public class FakeUser implements User {

    private final String id;
    private final String name;
    private final String email;

    public FakeUser(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    };

    @Override
    public String getUserId() {
        return null;
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }
    @Override
    public String toString() {
        return String.format("%s(%s, %s)", this.getClass().getSimpleName(), this.id, this.name);
    }
}
