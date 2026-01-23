package it.unisannio.authorization.persistence;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import it.unisannio.authorization.data.User;
import it.unisannio.authorization.exception.UserNotFoundException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryMongoTest {

    @Mock
    private MongoClient mongoClient;

    @Mock
    private MongoDatabase mongoDatabase;

    @Mock
    private MongoCollection<Document> collection;

    private UserRepositoryMongo repository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @BeforeEach
    void setUp() {
        doReturn(mongoDatabase).when(mongoClient).getDatabase(anyString());
        doReturn(collection).when(mongoDatabase).getCollection(anyString());
        repository = new UserRepositoryMongo(mongoClient);
    }

    @Test
    void testCreateUserSuccess() {
        LocalDate birthDate = LocalDate.parse("17/05/2000", formatter);
        User user = new User("john", "John", "Doe", "john@example.com", "pass123", new HashSet<>(), birthDate, new HashSet<>());

        InsertOneResult result = mock(InsertOneResult.class);
        doReturn(true).when(result).wasAcknowledged();
        doReturn(result).when(collection).insertOne(any(Document.class));

        User created = repository.createUser(user);

        assertEquals(user, created);
        verify(collection, times(1)).insertOne(any(Document.class));
    }

    @Test
    void testFindUserNotFound() {
        FindIterable<Document> iterable = mock(FindIterable.class);
        doReturn(iterable).when(collection).find(any(Bson.class));
        doReturn(iterable).when(iterable).map(any());
        doReturn(null).when(iterable).first();

        assertThrows(UserNotFoundException.class, () -> repository.findUser("john"));
    }

    @Test
    void testFindUserOrNullReturnsNull() {
        FindIterable<Document> iterable = mock(FindIterable.class);
        doReturn(iterable).when(collection).find(any(Bson.class));
        doReturn(iterable).when(iterable).map(any());
        doReturn(null).when(iterable).first();

        User user = repository.findUserOrNull("john");
        assertNull(user);
    }

    @Test
    void testDeleteUserSuccess() {
        Document doc = new Document();
        doReturn(doc).when(collection).findOneAndDelete(any(Bson.class));

        boolean result = repository.deleteUser("john");
        assertTrue(result);
    }

    @Test
    void testDeleteUserFail() {
        doReturn(null).when(collection).findOneAndDelete(any(Bson.class));

        boolean result = repository.deleteUser("john");
        assertFalse(result);
    }

    @Test
    void testUpdateUserNotFound() {
        // Simula find() che ritorna null -> findUser lancerà UserNotFoundException
        FindIterable<Document> iterable = mock(FindIterable.class);
        doReturn(iterable).when(collection).find(any(Bson.class));
        doReturn(iterable).when(iterable).map(any());
        doReturn(null).when(iterable).first();

        User user = new User("john", "John", "Doe", "john@example.com", "pass123", new HashSet<>(), LocalDate.now(), new HashSet<>());

        assertThrows(UserNotFoundException.class, () -> repository.updateUser("john", user));
    }

    @Test
    void testFindAllReturnsUsers() {
        FindIterable<Document> iterable = mock(FindIterable.class);
        doReturn(iterable).when(collection).find();
        doReturn(iterable).when(iterable).map(any());
        doReturn(List.of(
                new User("alice", "Alice", "Wonder", "alice@example.com", "pass123", new HashSet<>(), LocalDate.now(), new HashSet<>())
        )).when(iterable).into(any(List.class));

        List<User> users = repository.findall();
        assertEquals(1, users.size());
    }

    @Test
    void testFindAllByRoleReturnsUsers() {
        FindIterable<Document> iterable = mock(FindIterable.class);
        doReturn(iterable).when(collection).find(any(Document.class));
        doReturn(iterable).when(iterable).map(any());
        doReturn(List.of(
                new User("admin", "Admin", "User", "admin@example.com", "pass123", new HashSet<>(), LocalDate.now(), new HashSet<>())
        )).when(iterable).into(any(List.class));

        List<User> users = repository.findAllByRole("admin");
        assertEquals(1, users.size());
    }

    @Test
    void testDeleteAllSuccess() {
        DeleteResult deleteResult = mock(DeleteResult.class);
        doReturn(true).when(deleteResult).wasAcknowledged();
        doReturn(deleteResult).when(collection).deleteMany(any(Document.class));

        boolean result = repository.deleteAll();
        assertTrue(result);
    }

}