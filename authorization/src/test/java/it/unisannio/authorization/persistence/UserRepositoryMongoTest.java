package it.unisannio.authorization.persistence;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;

import it.unisannio.authorization.data.User;

@ExtendWith(MockitoExtension.class)
class UserRepositoryMongoTest {

    @Mock
    private MongoCollection<Document> collection;

    private UserRepositoryMongo repository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");


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
        @SuppressWarnings("unchecked")
        FindIterable<Document> iterable = mock(FindIterable.class);
        doReturn(iterable).when(collection).find(any(Bson.class));
        doReturn(iterable).when(iterable).map(any());
        doReturn(null).when(iterable).first();
    }

    @Test
    void testFindUserOrNullReturnsNull() {
        @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unused")
    @Test
    void testUpdateUserNotFound() {
        // Simula find() che ritorna null -> findUser lancerà UserNotFoundException
        @SuppressWarnings("unchecked")
        FindIterable<Document> iterable = mock(FindIterable.class);
        doReturn(iterable).when(collection).find(any(Bson.class));
        doReturn(iterable).when(iterable).map(any());
        doReturn(null).when(iterable).first();

        User user = new User("john", "John", "Doe", "john@example.com", "pass123", new HashSet<>(), LocalDate.now(), new HashSet<>());
    }

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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