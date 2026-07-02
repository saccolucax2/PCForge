package it.unisannio.authorization.persistence.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import it.unisannio.authorization.data.Roles;
import it.unisannio.authorization.data.User;

class MapperTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @SuppressWarnings("unchecked")
    @Test
    void testUserToDocumentMapper() {
        LocalDate birthDate = LocalDate.of(1995, 3, 21);
        User user = new User(
                "john",
                "John",
                "Doe",
                "john@example.com",
                "pass123",
                Set.of(Roles.USER, Roles.ADMIN),
                birthDate,
                new HashSet<>() // generatedBuilds vuoto
        );

        UserToDocumentMapper mapper = new UserToDocumentMapper();
        Document doc = mapper.apply(user);

        // Controllo campi base
        assertEquals("john", doc.getString("username"));
        assertEquals("John", doc.getString("name"));
        assertEquals("Doe", doc.getString("surname"));
        assertEquals("john@example.com", doc.getString("email"));
        assertEquals("pass123", doc.getString("password"));

        // Controllo ruoli
        assertTrue(((java.util.List<String>) doc.get("roles")).containsAll(Set.of("USER", "ADMIN")));

        // Controllo data di nascita
        assertEquals(birthDate.format(FORMATTER), doc.getString("birthDate"));
    }

    @Test
    void testDocumentToUserMapper() {
        String birthDateStr = "21/03/1995";
        Document doc = new Document("username", "john")
                .append("name", "John")
                .append("surname", "Doe")
                .append("email", "john@example.com")
                .append("password", "pass123")
                .append("roles", java.util.List.of("USER", "ADMIN"))
                .append("birthDate", birthDateStr)
                .append("generatedBuilds", java.util.List.of()); // lista vuota

        DocumentToUserMapper mapper = new DocumentToUserMapper();
        User user = mapper.apply(doc);

        // Controllo campi base
        assertEquals("john", user.getUsername());
        assertEquals("John", user.getName());
        assertEquals("Doe", user.getSurname());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("pass123", user.getPassword());

        // Controllo ruoli
        assertTrue(user.getRoles().containsAll(Set.of(Roles.USER, Roles.ADMIN)));

        // Controllo data di nascita
        assertEquals(LocalDate.parse(birthDateStr, FORMATTER), user.getBirthDate());

        // Controllo generatedBuilds
        assertNotNull(user.getGeneratedBuilds());
        assertTrue(user.getGeneratedBuilds().isEmpty());
    }

    @Test
    void testRoundTripUserToDocumentAndBack() {
        LocalDate birthDate = LocalDate.of(1990, 12, 15);
        User original = new User(
                "alice",
                "Alice",
                "Wonder",
                "alice@example.com",
                "Password1!",
                Set.of(Roles.USER),
                birthDate,
                new HashSet<>()
        );

        // User → Document
        UserToDocumentMapper toDoc = new UserToDocumentMapper();
        Document doc = toDoc.apply(original);

        // Document → User
        DocumentToUserMapper toUser = new DocumentToUserMapper();
        User mapped = toUser.apply(doc);

        // Controllo che tutti i campi siano uguali
        assertEquals(original.getUsername(), mapped.getUsername());
        assertEquals(original.getName(), mapped.getName());
        assertEquals(original.getSurname(), mapped.getSurname());
        assertEquals(original.getEmail(), mapped.getEmail());
        assertEquals(original.getPassword(), mapped.getPassword());
        assertEquals(original.getRoles(), mapped.getRoles());
        assertEquals(original.getBirthDate(), mapped.getBirthDate());
        assertEquals(original.getGeneratedBuilds(), mapped.getGeneratedBuilds());
    }
}
