package it.unisannio.authorization.application;

import it.unisannio.authorization.data.Roles;
import it.unisannio.authorization.data.User;
import it.unisannio.authorization.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository mockRepository;

    private UserService userService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(mockRepository);
    }

    @Test
    void testCreateUserSuccess() {
        LocalDate birthDate = LocalDate.parse("17/05/2000", formatter);
        User user = new User(
                "john", "John", "Doe", "john@example.com", "Password1!",
                Set.of(Roles.USER), birthDate);
        user.setGeneratedBuilds(new HashSet<>());

        when(mockRepository.findUserOrNull("john")).thenReturn(null);
        when(mockRepository.createUser(any(User.class))).thenReturn(user);

        String username = userService.createUser(user);

        assertEquals("john", username);
        assertTrue(encoder.matches("Password1!", user.getPassword()));
        assertEquals(Set.of(Roles.USER), user.getRoles());
        assertEquals(birthDate, user.getBirthDate());
        assertNotNull(user.getGeneratedBuilds());
        verify(mockRepository).createUser(any(User.class));
    }

    @Test
    void testCreateUserWithFutureBirthDateShouldFail() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        User user = new User(
                "john", "John", "Doe", "john@example.com", "Password1!",
                Set.of(Roles.USER), futureDate
        );

        when(mockRepository.findUserOrNull("john")).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(user));

        assertTrue(exception.getMessage().contains("Invalid birth date: must be at least 13 years old"));
        verify(mockRepository, never()).createUser(any());
    }

    @Test
    void testCreateUserTooYoungShouldFail() {
        LocalDate tooRecent = LocalDate.now().minusYears(10);
        User user = new User(
                "young", "Baby", "Doe", "baby@example.com", "Password1!",
                Set.of(Roles.USER), tooRecent
        );

        when(mockRepository.findUserOrNull("young")).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(user));

        assertTrue(exception.getMessage().contains("Invalid birth date: must be at least 13 years old"));
        verify(mockRepository, never()).createUser(any());
    }

    @Test
    void testCreateUserAlreadyExists() {
        LocalDate birthDate = LocalDate.parse("01/01/1990", formatter);
        User existingUser = new User(
                "john", "John", "Doe", "john@example.com", "Password1!",
                Set.of(Roles.USER), birthDate
        );
        when(mockRepository.findUserOrNull("john")).thenReturn(existingUser);

        User newUser = new User(
                "john", "Johnny", "Doe", "johnny@example.com", "Password1!",
                Set.of(Roles.USER), birthDate
        );

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(newUser));

        verify(mockRepository, never()).createUser(any());
    }

    @Test
    void testCreateUserInvalidPassword() {
        LocalDate birthDate = LocalDate.parse("17/05/2000", formatter);
        User user = new User(
                "john", "John", "Doe", "john@example.com", "weak",
                Set.of(Roles.USER), birthDate
        );

        when(mockRepository.findUserOrNull("john")).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(user));

        assertTrue(exception.getMessage().toLowerCase().contains("invalid password"));
        verify(mockRepository, never()).createUser(any());
    }

    @Test
    void testAuthenticateUserSuccess() {
        LocalDate birthDate = LocalDate.parse("17/05/1990", formatter);
        User user = new User(
                "john", "John", "Doe", "john@example.com", encoder.encode("Password1!"),
                Set.of(Roles.USER), birthDate
        );
        when(mockRepository.findUser("john")).thenReturn(user);

        assertTrue(userService.authenticateUser("john", "Password1!"));
    }

    @Test
    void testAuthenticateUserFail() {
        when(mockRepository.findUser("john")).thenReturn(null);
        assertFalse(userService.authenticateUser("john", "Password1!"));
    }

    @Test
    void testGetAllUsers() {
        LocalDate birthDate1 = LocalDate.parse("01/01/1995", formatter);
        LocalDate birthDate2 = LocalDate.parse("01/01/1996", formatter);
        User user1 = new User("john", "John", "Doe", "john@example.com", "Passw0rd!", Set.of(Roles.USER), birthDate1);
        User user2 = new User("jane", "Jane", "Doe", "jane@example.com", "MyPass1@", Set.of(Roles.USER), birthDate2);

        when(mockRepository.findall()).thenReturn(List.of(user1, user2));

        List<User> users = userService.getAllUsers();
        assertEquals(2, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
    }

    // ✅ Aggiornato per gestire password già criptate o da criptare
    @Test
    void testUpdateUserWithPasswordEncoding() {
        LocalDate birthDate = LocalDate.parse("17/05/1990", formatter);

        // Utente esistente (ha password vecchia già cifrata)
        User existing = new User("john", "John", "Doe", "john@example.com",
                encoder.encode("OldPass1!"), Set.of(Roles.USER), birthDate);

        // Utente aggiornato (password nuova in chiaro)
        User update = new User("john", "John", "Doe", "john@example.com",
                "NewPass2@", Set.of(Roles.USER), birthDate);

        when(mockRepository.findUser("john")).thenReturn(existing);
        when(mockRepository.updateUser(eq("john"), any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(1)); // restituisce l’utente aggiornato

        // Esegui l'update tramite il servizio
        User updatedUser = userService.updateUser("john", update);

        // ✅ Verifica che la password sia stata cifrata correttamente
        assertTrue(
                encoder.matches("NewPass2@", updatedUser.getPassword()),
                "La password non è stata codificata correttamente"
        );

        // ✅ Verifica che la data di nascita e i ruoli siano rimasti coerenti
        assertEquals(birthDate, updatedUser.getBirthDate());
        assertEquals(Set.of(Roles.USER), updatedUser.getRoles());

        // ✅ Verifica che il repository sia stato effettivamente chiamato
        verify(mockRepository).updateUser(eq("john"), any(User.class));
    }


    @Test
    void testUpdateUserWithAlreadyEncodedPassword() {
        LocalDate birthDate = LocalDate.parse("17/05/1990", formatter);

        // Password già codificata
        String encodedPassword = encoder.encode("SafePass1!");

        // Utente esistente e aggiornamento con la stessa password codificata
        User existing = new User("john", "John", "Doe", "john@example.com",
                encodedPassword, Set.of(Roles.USER), birthDate);
        User update = new User("john", "John", "Doe", "john@example.com",
                encodedPassword, Set.of(Roles.USER), birthDate);

        when(mockRepository.findUser("john")).thenReturn(existing);
        when(mockRepository.updateUser(eq("john"), any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(1)); // restituisce l’utente aggiornato

        // Esegui l'update
        User updatedUser = userService.updateUser("john", update);

        // ✅ La password non deve essere ricodificata
        assertEquals(
                encodedPassword,
                updatedUser.getPassword(),
                "La password non doveva essere modificata"
        );

        // ✅ Verifica che il repository sia stato chiamato correttamente
        verify(mockRepository).updateUser(eq("john"), any(User.class));
    }


    @Test
    void testFindAllByRole() {
        LocalDate birthDate = LocalDate.parse("17/05/2000", formatter);
        User user = new User("john", "John", "Doe", "john@example.com", "Passw0rd!", Set.of(Roles.USER), birthDate);
        when(mockRepository.findAllByRole("USER")).thenReturn(List.of(user));

        List<User> users = userService.findAllByRole("USER");
        assertEquals(1, users.size());
        assertEquals("john", users.getFirst().getUsername());
    }

    @Test
    void testDeleteUser() {
        when(mockRepository.deleteUser("john")).thenReturn(true);
        assertTrue(userService.deleteUser("john"));
    }
}