package at.ac.uibk.mcsconnect.person.impl.hidden;

import at.ac.uibk.mcsconnect.person.api.User;

public class UserImpl implements User {

    private final String id;
    private final String name;
    private final String email;

    public UserImpl(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    };

    @Override
    public String getUserId() {
        return this.id;
    }

    @Override
    public String getEmail() {
        return this.email;
    }

    @Override
    public String getDisplayName() {
        return this.name;
    }

    @Override
    public String toString() {
        return String.format("User(%s, %s)", getUserId(), getDisplayName());
    }
}
