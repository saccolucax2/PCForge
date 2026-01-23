package it.unisannio.authorization.presentation;

import it.unisannio.authorization.application.UserService;
import it.unisannio.authorization.data.Roles;
import it.unisannio.authorization.data.User;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final LocalDate birthDate = LocalDate.parse("17/05/1990", formatter);

    @BeforeEach
    void setUp() { }

    // ------------------ TEST authenticateUserJson ------------------
    @Test
    void testAuthenticateUserSuccess() {
        User user = new User("john", "John", "Doe", "john@example.com", "Password1!", Set.of(Roles.USER), birthDate, new HashSet<>());
        when(userService.authenticateUser("john", "Password1!")).thenReturn(true);
        when(userService.getUserRolesAsString("john")).thenReturn(List.of("USER", "ADMIN"));

        Response response = userController.authenticateUserJson(user);

        assertEquals(200, response.getStatus());
        String json = (String) response.getEntity();
        assertTrue(json.contains("\"username\":\"john\""));
        assertTrue(json.contains("\"roles\":[\"USER\",\"ADMIN\"]"));
    }

    @Test
    void testAuthenticateUserUnauthorized() {
        User user = new User("john", "John", "Doe", "john@example.com", "WrongPass2!", Set.of(Roles.USER), birthDate, new HashSet<>());
        when(userService.authenticateUser("john", "WrongPass2!")).thenReturn(false);

        Response response = userController.authenticateUserJson(user);

        assertEquals(401, response.getStatus());
    }

    // ------------------ TEST createUser ------------------
    @Test
    void testCreateUserSuccess() {
        User user = new User("jane", "Jane", "Doe", "jane@example.com", "StrongPass1!", Set.of(Roles.USER), birthDate, new HashSet<>());
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getAbsolutePath()).thenReturn(URI.create("http://localhost/users"));
        when(userService.createUser(user)).thenReturn("jane");

        Response response = userController.createUser(user, uriInfo);

        assertEquals(201, response.getStatus());
        assertTrue(response.getLocation().toString().endsWith("/jane"));
    }

    @Test
    void testCreateUserConflict() {
        User user = new User("jane", "Jane", "Doe", "jane@example.com", "StrongPass1!", Set.of(Roles.USER), birthDate, new HashSet<>());
        UriInfo uriInfo = mock(UriInfo.class);
        when(userService.createUser(user)).thenThrow(new IllegalArgumentException("Username already exists"));

        Response response = userController.createUser(user, uriInfo);

        assertEquals(409, response.getStatus());
        assertEquals("Username already exists", response.getEntity());
    }

    @Test
    void testCreateUserInvalidPassword() {
        User user = new User("mark", "Mark", "Doe", "mark@example.com", "weak", Set.of(Roles.USER), birthDate, new HashSet<>());
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getAbsolutePath()).thenReturn(URI.create("http://localhost/users"));
        lenient().when(userService.createUser(user))
                .thenThrow(new IllegalArgumentException("Invalid Password: must be at least 8 characters long, must contain at least one digit, one lowercase letter and one uppercase letter"));

        Response response = userController.createUser(user, uriInfo);

        assertEquals(409, response.getStatus());
        assertTrue(((String) response.getEntity()).toLowerCase().contains("invalid password"));
    }

    // ------------------ TEST getUser ------------------
    @Test
    void testGetUserFound() {
        User user = new User("john", "John", "Doe", "john@example.com", "Password1!", Set.of(Roles.USER), birthDate, new HashSet<>());
        when(userService.getUser("john")).thenReturn(user);

        Response response = userController.getUser("john");

        assertEquals(200, response.getStatus());
        assertEquals(user, response.getEntity());
    }

    @Test
    void testGetUserNotFound() {
        when(userService.getUser("john")).thenReturn(null);

        Response response = userController.getUser("john");

        assertEquals(404, response.getStatus());
    }

    // ------------------ TEST getAllUsers ------------------
    @Test
    void testGetAllUsersFound() {
        List<User> users = List.of(
                new User("john", "John", "Doe", "john@example.com", "Passw0rd!", Set.of(Roles.USER), birthDate, new HashSet<>()),
                new User("jane", "Jane", "Doe", "jane@example.com", "MyPass1@", Set.of(Roles.USER), birthDate, new HashSet<>())
        );
        when(userService.getAllUsers()).thenReturn(users);

        Response response = userController.getAllUsers();

        assertEquals(200, response.getStatus());
        assertEquals(users, response.getEntity());
    }

    @Test
    void testGetAllUsersNotFound() {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        Response response = userController.getAllUsers();

        assertEquals(404, response.getStatus());
    }

    // ------------------ TEST deleteUser ------------------
    @Test
    void testDeleteUserSuccess() {
        when(userService.deleteUser("john")).thenReturn(true);

        Response response = userController.deleteUser("john");

        assertEquals(204, response.getStatus());
    }

    @Test
    void testDeleteUserNotFound() {
        when(userService.deleteUser("john")).thenReturn(false);

        Response response = userController.deleteUser("john");

        assertEquals(404, response.getStatus());
    }

    // ------------------ TEST getUsersByRole ------------------
    @Test
    void testGetUsersByRole() {
        List<User> users = List.of(
                new User("john", "John", "Doe", "john@example.com", "Passw0rd!", Set.of(Roles.USER), birthDate, new HashSet<>()),
                new User("jane", "Jane", "Doe", "jane@example.com", "MyPass1@", Set.of(Roles.USER), birthDate, new HashSet<>())
        );
        when(userService.findAllByRole("ADMIN")).thenReturn(users);

        Response response = userController.getUsersByRole("ADMIN");

        assertEquals(200, response.getStatus());
        assertEquals(users, response.getEntity());
    }

    // ------------------ TEST updateUser ------------------
    @Test
    void testUpdateUser() {
        User user = new User("john", "John", "Doe", "john@example.com", "NewPass1!", Set.of(Roles.USER), birthDate, new HashSet<>());
        User updatedUser = new User("john", "John", "Doe", "john@example.com", "NewPass1!", Set.of(Roles.USER), birthDate, new HashSet<>());

        when(userService.updateUser("john", user)).thenReturn(updatedUser);

        Response response = userController.updateUser("john", user);

        assertEquals(200, response.getStatus());
        assertEquals(updatedUser, response.getEntity());
    }

    @Test
    void testUpdateUserInvalidPassword() {
        User user = new User("john", "John", "Doe", "john@example.com", "123", Set.of(Roles.USER), birthDate, new HashSet<>());

        when(userService.updateUser("john", user))
                .thenThrow(new IllegalArgumentException("Invalid Password"));

        Response response = userController.updateUser("john", user);

        assertEquals(409, response.getStatus());
        assertTrue(((String) response.getEntity()).toLowerCase().contains("invalid password"));
    }
}