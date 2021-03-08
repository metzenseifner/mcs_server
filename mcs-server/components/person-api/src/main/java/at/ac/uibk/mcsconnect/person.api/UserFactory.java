package at.ac.uibk.mcsconnect.person.api;

public interface UserFactory {

    User create(String id, String name, String email);

}
