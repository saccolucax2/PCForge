package it.unisannio.authorization.persistence;

import it.unisannio.authorization.data.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository {
    String USERNAME = "username";
    String NAME = "name";
    String SURNAME= "surname";
    String EMAIL = "email";
    String PASSWORD = "password";
    String BIRTHDATE = "birthDate";
    String DB = "accounting";
    String COLLECTION = "users";
    String BUILDS = "generatedBuilds";

    User createUser(User user);
    User findUser(String username);
    User updateUser(String username, User user);
    boolean deleteUser(String username);
    User findUserOrNull(String username);
    List<User> findall();
    List<User> findAllByRole(String role);
    boolean deleteAll();
    void addGeneratedBuild(String username, Long buildId);
    void removeGeneratedBuild(String username, Long buildId);

}